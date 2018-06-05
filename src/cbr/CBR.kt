package cbr

import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import edu.giisco.SoaML.metamodel.Interface
import io.github.cdimascio.dotenv.dotenv
import org.litote.kmongo.KMongo
import org.litote.kmongo.MongoOperator
import org.litote.kmongo.filter
import org.litote.kmongo.getCollection
import schemas.Case
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


private val dotenv = dotenv { directory = "./" }
private val verbose = dotenv["VERBOSE"] == "true"


data class RetrievedCase(val problem: Interface, val solutions: List<Pair<Double, String>>)

data class LearnedCase(val case: Case, val distance: Double)

fun findSimilarity(referenceCase: Case, k: Int): List<Pair<Double, String>> {
    val casesByDistance = mutableListOf<Pair<Double, String>>()
    val caseCollection = getCaseCollection()
    val casesCount = caseCollection.count()
    caseCollection.find()
        // Saco el caso problematico (consume mucha ram y procesamiento
        .filter("{'problem.name':{${MongoOperator.ne}: 'basichoroscopeandnumerology.wsdl2'}}")
        .filter("{'solution':{${MongoOperator.ne}: 'Bitbucket - bitbucket.org-2.0-swagger.json'}}")
        .forEachIndexed { index, case ->
            if (verbose)
                println("\n $index/$casesCount.Comparing to ${case.solution}")
            try {
                val distance = referenceCase.getDistance(case)
                casesByDistance.add(Pair(distance, case.solution))
            } catch (e: Exception) {
                if (verbose)
                    println("\n${case.solution} failed, exception $e.")
            }
        }
    val top = minOf(k, casesByDistance.size)
    return casesByDistance.sortedWith(compareBy({ it.first })).slice(0 until top)
}

fun findSolutions(referenceCase: Case): List<Pair<Double, String>> {
    /**
     * Find the solution to [referenceCase]
     * In order to do it, it perform a knn search and return the most common solution.
     * @return the proper solution
     */
    val k = dotenv["K"]?.toInt() ?: 10
    val similarCases = findSimilarity(referenceCase, k)
    return similarCases
}

fun findSolution(referenceCase: Case, solutions: List<Pair<Double, String>>): Pair<Case, Double> {
    // TODO: Define how to get the solution
    val solution = solutions[0]
    referenceCase.solution = solution.second
    return Pair(referenceCase, solution.first)
}

fun main(args: Array<String>) {
    val queryCollection = getQueryCollection()
    val (retrievedCasesCollection, retrievedCasesCollectionName) = getRetrievedCasesCollection()
    val learnedCasesCollection = getLearnedCasesCollection()
    val caseCollection = getCaseCollection()
    val distanceThreshold = dotenv["DISTANCE_THRESHOLD"]?.toDouble()
    queryCollection.find().forEach {
        if (verbose) {
            println("\n-------------------------------------------------------------------------------------------------")
            println("Searching for solutions of ${it.problem.name}")
        }
        val solutions = findSolutions(it)
        val retrievedCase = RetrievedCase(it.problem, solutions)
        retrievedCasesCollection.insertOne(retrievedCase)

        if (verbose) {
            println("\n-------------------------------------------------------------------------------------------------")
            println("Solutions of ${it.problem.name} were found")
        }
        val (solutionCase, distance) = findSolution(it, solutions)
        println("\n-------------------------------------------------------------------------------------------------")
        println("Solutions of ${solutionCase.problem.name} is ${solutionCase.solution}, distance: $distance")
        if (distanceThreshold != null && distance < distanceThreshold) {
            if (verbose)
                println("Learned a new Case!")
            caseCollection.insertOne(solutionCase)
            learnedCasesCollection.insertOne(LearnedCase(solutionCase, distance))
        }
    }
    println("Experiment done, see  '$retrievedCasesCollectionName'")
}


fun getDatabase(name: String): MongoDatabase {
    if (verbose)
        println("Connecting to Mongo")
    val host = dotenv["DATABASE_HOST"] ?: "127.0.0.1"
    val port = dotenv["DATABASE_PORT"]?.toInt() ?: 27017
    val client = KMongo.createClient(host = host, port = port)
    return client.getDatabase(name)
}


fun getQueryCollection(): MongoCollection<Case> {
    val databaseName = dotenv["DATABASE_QUERIES_NAME"] ?: "queries"
    val database = getDatabase(databaseName)
    val caseCollection = database.getCollection<Case>()
    if (verbose)
        println("Connection with Mongo established.")
    return caseCollection
}


fun getRetrievedCasesCollection(): Pair<MongoCollection<RetrievedCase>, String> {
    val databaseNamePrefix = dotenv["DATABASE_RESULTS_PREFIX"] ?: "retrieved_cases"
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss")
    val timestamp = LocalDateTime.now().format(formatter)
    val databaseName = "${databaseNamePrefix}_$timestamp"
    val database = getDatabase(databaseName)
    val caseCollection = database.getCollection<RetrievedCase>()
    if (verbose)
        println("Connection with Mongo established.")
    return Pair(caseCollection, databaseName)
}

fun getLearnedCasesCollection(): MongoCollection<LearnedCase> {
    val databaseNamePrefix = dotenv["DATABASE_LEARNED_PREFIX"] ?: "learned"
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss")
    val timestamp = LocalDateTime.now().format(formatter)
    val databaseName = "${databaseNamePrefix}_$timestamp"
    val database = getDatabase(databaseName)
    val caseCollection = database.getCollection<LearnedCase>()
    if (verbose)
        println("Connection with Mongo established.")
    return caseCollection
}


fun getCaseCollection(): MongoCollection<Case> {
    val databaseName = dotenv["DATABASE_KB_NAME"] ?: "KB"
    val database = schemas.getDatabase(databaseName)
    val caseCollection = database.getCollection<Case>()
    if (verbose)
        println("Connection with Mongo established.")
    return caseCollection
}

