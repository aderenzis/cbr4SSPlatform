package cbr

import edu.giisco.SoaML.metamodel.Interface


data class Case(val problem: Interface, var solution: String = "") {
    val solutionIsDefined get() = solution != ""
}
