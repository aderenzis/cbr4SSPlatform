package cbr

import compatibilityUtils.InterfacesCompatibilityChecker
import edu.giisco.SoaML.metamodel.*
import org.litote.kmongo.*


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

fun main(args: Array<String>) {
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
    val fromCurrency = Input("fromCurrency", ArrayList(listOfParameters))
    val toCurrency = Output("toCurrency", ArrayList(listOfParameters))

    val currencyFaults = ArrayList<Fault>()
    val currencyResponse = Response("currencyResponse", currencyType)
    val convertCurrency = Operation("convertCurrency", fromCurrency, toCurrency, currencyFaults, currencyResponse)
    val currencyConvertor = Interface("CurrencyConvertor", ArrayList(listOf(convertCurrency)))
    val case1 = Case(currencyConvertor, "http://www.webservicex.net/CurrencyConvertor.asmx?WSDL")
    val case2 = Case(currencyConvertor)
    println("Case created")

    val distance = case1.getDistance(case2)
    println("Distance: $distance")
    val distance2 = case2.getDistance(case1)
    println("Distance2: $distance2")

    println("Connecting to Mongo")
    val client = KMongo.createClient("172.17.0.2")
    val database = client.getDatabase("test")
    val caseCollection = database.getCollection<Case>()
    println("Connection with Mongo established.")

    caseCollection.drop()
    caseCollection.insertOne(case1)
    println("Insert succefull now there are ${caseCollection.count()} Cases in the KB")
    val caseRetrieved = caseCollection.findOne()
    println("Retrieved Case from db: \n $caseRetrieved")

}
