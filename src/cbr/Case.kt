package cbr

import com.mongodb.client.MongoCollection
import compatibilityUtils.InterfacesCompatibilityChecker
import edu.giisco.SoaML.metamodel.*
import edu.uncoma.fai.WsdlToSoaML.parser.WsdlToSoaML

import org.litote.kmongo.*
import java.io.File


data class Case(val problem: Interface, var solution: String = "") {
    val solutionIsDefined get() = solution != ""

    fun getDistance(anotherCase: Case): Double {
        val checker = InterfacesCompatibilityChecker()
        checker.serviceInterface = anotherCase.problem
        checker.requiredInterface = problem
        checker.run()
        return checker.adaptabilityGap
    }
}

fun getSampleCases(path: String): MutableList<Case> {
    val cases = mutableListOf<Case>()
    File(path).walk().filter { it.extension == "wsdl2" }.forEach {
        val soaMLInterface = WsdlToSoaML.createSoaMLInterface(it.absolutePath)
        val case = Case(soaMLInterface)
        cases.add(case)
    }
    return cases
}

fun findSimilarity(referenceCase: Case, k: Int): List<Case> {
    val casesByDistance = mutableListOf<Pair<Double, Case>>()
    val caseCollection = getCaseCollection()
    caseCollection.find().forEachIndexed { index, anotherCase ->
        // TODO: Porque no se puede calcular la distancia con mas casos?
        if (index < 26) {
            val distance = referenceCase.getDistance(anotherCase)
            casesByDistance.add(Pair(distance, anotherCase))
        }

    }
    val top = minOf(k, casesByDistance.size)
    return casesByDistance.sortedWith(compareBy({ it.first })).slice(0 until top).map { it.second }
}

fun main(args: Array<String>) {
    val path = "/home/rapkyt/Project/Tesis/Resources/Experiments/dataset/WsdlDataset/originales"
    val cases = getSampleCases(path)
    val caseCollection = getCaseCollection()
    caseCollection.drop()
    caseCollection.insertMany(cases)
    println("Insert succefull now there are ${caseCollection.count()} Cases in the KB")
    val currencyCase = createExampleCase()
    val example = caseCollection.findOne() ?: currencyCase
    val kCases = findSimilarity(example, 10)
    println("\nThe similar cases are:")
    println(kCases)

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
