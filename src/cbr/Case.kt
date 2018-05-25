package cbr

import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import edu.giisco.SoaML.metamodel.Interface

import org.litote.kmongo.*
import java.time.Instant
import java.time.format.DateTimeFormatter


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
    return casesByDistance.sortedWith(compareBy({ it.first })).slice(0 until top).map { it.second }
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
    queryCollection.find().forEach {
        val solutions = findSolutions(it)
        val retrievedCase = RetrievedCase(it, solutions)
        retrievedCases.add(retrievedCase)
    }
    val (retrievedCasesCollection, retrievedCasesCollectionName) = getRetrievedCasesCollection()
    retrievedCasesCollection.insertMany(retrievedCases)
    println("Experiment done, see  '$retrievedCasesCollectionName'")
}


fun getDatabase(name: String): MongoDatabase {
    println("Connecting to Mongo")
    val client = KMongo.createClient("172.17.0.2")
    return client.getDatabase(name)
}


fun getQueryCollection(): MongoCollection<Case> {
    val database = getDatabase("queries")
    val caseCollection = database.getCollection<Case>()
    println("Connection with Mongo established.")
    return caseCollection
}


fun getRetrievedCasesCollection(): Pair<MongoCollection<RetrievedCase>, String> {
    val database_name = "retrieved_cases_${DateTimeFormatter.ISO_INSTANT.format(Instant.now())}"
    val database = getDatabase(database_name)
    val caseCollection = database.getCollection<RetrievedCase>()
    println("Connection with Mongo established.")
    return Pair(caseCollection, database_name)
}