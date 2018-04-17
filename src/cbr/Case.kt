package cbr

import com.mongodb.client.MongoCollection
import compatibilityUtils.InterfacesCompatibilityChecker
import edu.giisco.SoaML.metamodel.*
import edu.uncoma.fai.WsdlToSoaML.parser.WsdlToSoaML

import org.litote.kmongo.*
import java.io.File

val checker = InterfacesCompatibilityChecker()

data class Case(val problem: Interface, var solution: String = "") {
    val solutionIsDefined get() = solution != ""

    fun getDistance(anotherCase: Case): Double {
        checker.serviceInterface = anotherCase.problem
        checker.requiredInterface = problem
        checker.run()
        return checker.getAdaptabilityGap()
    }
}

fun getSampleCases(path: String): MutableList<Case> {
    val cases = mutableListOf<Case>()
    File(path).walk().filter { it.extension == "wsdl2" }.forEach {
        val soaMLInterface = WsdlToSoaML.createSoaMLInterface(it.absolutePath)
        val case = Case(soaMLInterface, soaMLInterface.name)
        cases.add(case)
    }
    return cases
}

fun findSimilarity(referenceCase: Case, k: Int): List<Case> {
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

fun findSolution(referenceCase: Case): String {
    /**
     * Find the solution to [referenceCase]
     * In order to do it, it perform a knn search and return the most common solution.
     * @return the proper solution
     */
    val similarCases = findSimilarity(referenceCase, 10)
//    similarCases[1].solution = similarCases[0].solution
    return similarCases.groupBy { it.solution }.toList()
        .sortedWith(compareByDescending({ it.second.size })).first().first
}

fun main(args: Array<String>) {
//    Pasar como argumento el path hacia los servicios
    val path = args[0]
    val cases = getSampleCases(path)
    val caseCollection = getCaseCollection()
    caseCollection.drop()
    caseCollection.insertMany(cases)
    println("Insert succefull now there are ${caseCollection.count()} Cases in the KB")
    val currencyCase = createExampleCase()
    val example = caseCollection.findOne() ?: currencyCase
    val solution = findSolution(example)
    println("\nThe similar case is:")
    println(solution)

}

fun createExampleCase(): Case {
    println("Creating Case")
    val currencyType = SimpleType(SimpleType.STRING)
    val currencyParameter = Parameter("currency", currencyType)

    val arrayCurrencyType = ArrayType(currencyType)
    // Sin nombre falla
    arrayCurrencyType.name = "listofcriptos"
    val currencyArrayParameter = Parameter("listOfCurrencies", arrayCurrencyType)

    val currencyFromAtribute = Attribute("from", currencyType)
    val currencyToAtribute = Attribute("to", currencyType)
    val toFromCurrency = ComplexType("toFromCurrency", ArrayList(listOf(currencyFromAtribute, currencyToAtribute)))
    val currencyComplexParameter = Parameter("toFromCurrency", toFromCurrency)

    val listOfParameters = listOf(currencyParameter, currencyArrayParameter, currencyComplexParameter)
    val listOfParameters2 = listOf(currencyParameter)
    val fromCurrency = Input("fromCurrency", ArrayList(listOfParameters))
    val fromCurrency2 = Input("fromCurrency2", ArrayList(listOfParameters2))
    val toCurrency = Output("toCurrency", ArrayList(listOfParameters))

    val currencyFaults = ArrayList<Fault>()
    val currencyResponse = Response("currencyResponse", currencyType)
    val convertCurrency = Operation("convertCurrency", fromCurrency, toCurrency, currencyFaults, currencyResponse)
    val convertCurrency2 = Operation("GetMeMoney", fromCurrency2, toCurrency, currencyFaults, currencyResponse)
    val currencyConvertor = Interface("CurrencyConvertor", ArrayList(listOf(convertCurrency)))
    val currencyConvertor2 = Interface("CurrencyConvertor2", ArrayList(listOf(convertCurrency2)))
    val case1 = Case(currencyConvertor, "http://www.webservicex.net/CurrencyConvertor.asmx?WSDL")
    val case2 = Case(currencyConvertor2)
    println("Case created")

//    val distance = case1.getDistance(case2)
//    println("Distance: $distance")
//    val distance2 = case2.getDistance(case1)
//    println("Distance2: $distance2")
//    val caseCollection = getCaseCollection()
//    caseCollection.drop()
//    caseCollection.insertOne(case1)
//    println("Insert succefull now there are ${caseCollection.count()} Cases in the KB")
//    val caseRetrieved = caseCollection.findOne("{'problem.name':'CurrencyConvertor'}")
//    println("Retrieved Case from db: \n $caseRetrieved")
    return case1
}

fun getCaseCollection(): MongoCollection<Case> {
    println("Connecting to Mongo")
    val client = KMongo.createClient("172.17.0.2")
    val database = client.getDatabase("test")
    val caseCollection = database.getCollection<Case>()
    println("Connection with Mongo established.")
    return caseCollection
}
