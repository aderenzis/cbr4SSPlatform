package cbr

import edu.giisco.SoaML.metamodel.*


data class Case(val problem: Interface, var solution: String = "") {
    val solutionIsDefined get() = solution != ""
}

fun main(args: Array<String>) {
    val currencyType = SimpleType("string")
    val currencyParameter = Parameter("currency", currencyType)
    val fromCurrency = Input("fromCurrency", ArrayList(listOf(currencyParameter)))
    val toCurrency = Output("toCurrency", ArrayList(listOf(currencyParameter)))
    val currencyFaults = ArrayList<Fault>()
    val currencyResponse = Response("currencyResponse", currencyType)
    val convertCurrency = Operation("convertCurrency", fromCurrency, toCurrency, currencyFaults, currencyResponse)
    val currencyConvertor = Interface("CurrencyConvertor", ArrayList(listOf(convertCurrency)))
    val case1 = Case(currencyConvertor, "http://www.webservicex.net/CurrencyConvertor.asmx?WSDL")

}