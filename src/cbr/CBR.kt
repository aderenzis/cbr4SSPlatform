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
import selection.K
import selection.findSolution
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


private val dotenv = dotenv { directory = "./" }
private val verbose = dotenv["VERBOSE"] == "true"
private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss")


data class RetrievedCase(
    val problem: Interface,
    val solutions: List<Pair<Double, String>>,
    val selectedSolution: Pair<Double, String>,
    val hasLearned: Boolean
) {
    val created get() = LocalDateTime.now().format(dateFormatter)
}

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
                if (casesByDistance.size < k || distance < casesByDistance.last().first) {
                    casesByDistance.add(Pair(distance, case.solution))
                    casesByDistance.sortWith(compareBy({ it.first }))
                    if (index >= k)
                        casesByDistance.removeAt(k)
                }
            } catch (e: Exception) {
                if (verbose)
                    println("\n${case.solution} failed, exception $e.")
            }
        }
    return casesByDistance
}

fun findSolutions(referenceCase: Case): List<Pair<Double, String>> {
    /**
     * Find the solution to [referenceCase]
     * In order to do it, it perform a knn search and return the most common solution.
     * @return the proper solution
     */
    val similarCases = findSimilarity(referenceCase, K)
    return similarCases
}

fun main(args: Array<String>) {
    val queryCollection = getQueryCollection()
    val (retrievedCasesCollection, retrievedCasesCollectionName) = getRetrievedCasesCollection()
    val learnedCasesCollection = getLearnedCasesCollection()
    val caseCollection = getCaseCollection()
    val distanceThreshold = dotenv["DISTANCE_THRESHOLD"]?.toDouble()
    queryCollection.find().forEach {
        try {
            if (verbose) {
                println("\n-------------------------------------------------------------------------------------------------")
                println("Searching for solutions of ${it.problem.name}")
            }
            val solutions = findSolutions(it)

            if (solutions.isEmpty()) {
                if (verbose) {
                    println("\n-------------------------------------------------------------------------------------------------")
                    println("No Solutions of ${it.problem.name} were found")
                }
            } else {
                if (verbose) {
                    println("\n-------------------------------------------------------------------------------------------------")
                    println("Solutions of ${it.problem.name} were found")
                }
                val (solutionCase, distance) = findSolution(it, solutions)
                println("\n-------------------------------------------------------------------------------------------------")
                println("Solutions of ${it.problem.name} is ${solutionCase.solution}, distance: $distance")

                val learned = distanceThreshold != null && distance < distanceThreshold
                if (learned) {
                    if (verbose)
                        println("Learned a new Case!")
                    caseCollection.insertOne(solutionCase)
                    learnedCasesCollection.insertOne(LearnedCase(solutionCase, distance))
                }

                val retrievedCase = RetrievedCase(it.problem, solutions, Pair(distance, solutionCase.solution), learned)
                retrievedCasesCollection.insertOne(retrievedCase)
            }
        } catch (e: Exception) {
            if (verbose)
                println("\n${it.problem.name} failed, exception $e.")
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
    val timestamp = LocalDateTime.now().format(dateFormatter)
    val databaseName = dotenv["DATABASE_RESULTS_NAME"] ?: "${databaseNamePrefix}_$timestamp"
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
