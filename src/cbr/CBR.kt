package cbr

import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import edu.giisco.SoaML.metamodel.Interface
import io.github.cdimascio.dotenv.dotenv
import org.litote.kmongo.*
import schemas.Case
import java.time.Instant
import java.time.format.DateTimeFormatter


private val dotenv = dotenv { directory = "./" }


data class RetrievedCase(val problem: Interface, val solutions: List<Pair<Double, Case>>)


fun findSimilarity(referenceCase: Case, k: Int): List<Pair<Double, Case>> {
    val casesByDistance = mutableListOf<Pair<Double, Case>>()
    val caseCollection = getCaseCollection()
    caseCollection.find()
        // Saco el caso problematico (consume mucha ram y procesamiento
        .filter("{'problem.name':{${MongoOperator.ne}: 'basichoroscopeandnumerology.wsdl2'}}")
        .forEach {
            val distance = referenceCase.getDistance(it)
            casesByDistance.add(Pair(distance, it))
        }
    val top = minOf(k, casesByDistance.size)
    return casesByDistance.sortedWith(compareBy({ it.first })).slice(0 until top)
}

fun findSolutions(referenceCase: Case): List<Pair<Double, Case>> {
    /**
     * Find the solution to [referenceCase]
     * In order to do it, it perform a knn search and return the most common solution.
     * @return the proper solution
     */
    val similarCases = findSimilarity(referenceCase, 10)
//    similarCases[1].solution = similarCases[0].solution
    return similarCases
}

fun main(args: Array<String>) {
    val retrievedCases = mutableListOf<RetrievedCase>()
    val queryCollection = getQueryCollection()
//TODO: insert Queries
    val casesCOllection = getCaseCollection()
    val example = casesCOllection.findOne()!!
    queryCollection.insertOne(example)
    queryCollection.find().forEach {
        val solutions = findSolutions(it)
        val retrievedCase = RetrievedCase(it.problem, solutions)
        retrievedCases.add(retrievedCase)
    }
    val (retrievedCasesCollection, retrievedCasesCollectionName) = getRetrievedCasesCollection()
    retrievedCasesCollection.insertMany(retrievedCases)
    println("Experiment done, see  '$retrievedCasesCollectionName'")
}


fun getDatabase(name: String): MongoDatabase {
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
    println("Connection with Mongo established.")
    return caseCollection
}


fun getRetrievedCasesCollection(): Pair<MongoCollection<RetrievedCase>, String> {
    val database_name_prefix = dotenv["DATABASE_RESULTS_PREFIX"] ?: "retrieved_cases"
    val database_name = "${database_name_prefix}_${DateTimeFormatter.ISO_INSTANT.format(Instant.now())}"
    val database = getDatabase(database_name)
    val caseCollection = database.getCollection<RetrievedCase>()
    println("Connection with Mongo established.")
    return Pair(caseCollection, database_name)
}


fun getCaseCollection(): MongoCollection<Case> {
    val databaseName = dotenv["DATABASE_KB_NAME"] ?: "KB"
    val database = schemas.getDatabase(databaseName)
    val caseCollection = database.getCollection<Case>()
    println("Connection with Mongo established.")
    return caseCollection
}

