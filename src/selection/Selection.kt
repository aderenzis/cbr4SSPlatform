package selection

import cbr.getCaseCollection
import org.litote.kmongo.findOne
import schemas.Case

enum class SelectionRule {
    KNN, WKNN, DWKNN
}

private val dotenv = io.github.cdimascio.dotenv.dotenv { directory = "./" }
private val selectionRule = when (dotenv["SELECTION_RULE"]) {
    "KNN", null -> SelectionRule.KNN
    "WKNN" -> SelectionRule.WKNN
    "DWKNN" -> SelectionRule.DWKNN
    else -> throw IllegalArgumentException("'SELECTION_RULE' must be one of KNN, WKNN or DWKNN")
}
val K = dotenv["K"]?.toInt() ?: 10


typealias weightFunctionType = (Double, Double, Double) -> Double

val wknnWeight: weightFunctionType = { dk, d1, di -> (dk - di) / (dk - d1) }

val dwknnWeight: weightFunctionType = { dk, d1, di -> ((dk - di) / (dk - d1)) * ((dk + d1) / (dk + di)) }


fun knn(solutions: List<Pair<Double, String>>): Pair<Double, String> {
    // Agrupo por solución, casteo a lista, y ordeno por cantidad de casos que tienen la misma solución
    val solutionsByClass = solutions.groupBy { it.second }.toList().sortedWith(compareByDescending { it.second.size })
    // Devuelvo el primer caso de la primer solución
    return solutionsByClass[0].second[0]
}


fun wknn(solutions: List<Pair<Double, String>>, weightFunction: weightFunctionType): Pair<Double, String> {
    val weightedSolutions = mutableListOf<Pair<Double, String>>()
    val d1 = solutions[0].first
    val dk = solutions[K - 1].first
    solutions.forEach {
        val weight = weightFunction(dk, d1, it.first)
        weightedSolutions.add(Pair(weight, it.second))
    }
    val solutionsByClass = weightedSolutions.groupBy { it.second }.map { Pair(it.value.map { it.first }.sum(), it.key) }
        .sortedWith(compareByDescending { it.first })
    val solution = solutionsByClass[0].second
    return solutions.first { it.second == solution }

}


fun findSolution(referenceCase: Case, solutions: List<Pair<Double, String>>): Pair<Case, Double> {
    val solutions = solutions.sortedWith(compareBy { it.first })
    val solution = when (K) {
        1 -> solutions[0]
        else -> when (selectionRule) {
            SelectionRule.KNN -> knn(solutions)
            SelectionRule.WKNN -> wknn(solutions, wknnWeight)
            SelectionRule.DWKNN -> wknn(solutions, dwknnWeight)
        }
    }
    referenceCase.solution = solution.second
    return Pair(referenceCase, solution.first)
}


fun main(args: Array<String>) {
    val solutions = arrayListOf(
        Pair(0.0, "IntuneResourceManagementClient - azure.com-arm-intune-2015-01-14-preview-swagger.json"),
        Pair(0.0805185185185187, "ExaVault - exavault.com-1.0.0-swagger.json"),
        Pair(0.08076719576719, "IntuneResourceManagementClient - azure.com-arm-intune-2015-01-14-preview-swagger.json"),
        Pair(0.08129629629629632, "ExaVault - exavault.com-1.0.0-swagger.json"),
        Pair(0.838099128540305, "iotHubClient - azure.com-arm-iothub-2016-02-03-swagger.json"),
        Pair(0.893915343915344, "Google Identity Toolkit - googleapis.com-identitytoolkit-v3-swagger.json"),
        Pair(0.8977160493827162, "AWS OpsWorks - amazonaws.com-opsworks-2013-02-18-swagger.json"),
        Pair(0.8979276895943564, "AWS Identity and Access Management - amazonaws.com-iam-2010-05-08-swagger.json"),
        Pair(0.8993386243386244, "Google Analytics - googleapis.com-analytics-v3-swagger.json"),
        Pair(0.8998611111111112, "BC Data Catalogue - gov.bc.ca-bcdc-3.0.1-swagger.json")
    )
    val case = getCaseCollection().findOne()!!
    val knn = findSolution(case, solutions)
    println("KNN: $knn")
}
