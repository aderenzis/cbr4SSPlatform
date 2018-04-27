package swaggerToSoaML

import edu.giisco.SoaML.metamodel.*
import io.swagger.models.Operation
import io.swagger.models.RefModel
import io.swagger.parser.SwaggerParser;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter
import io.swagger.models.properties.ArrayProperty
import io.swagger.models.properties.RefProperty
import java.util.ArrayList

//
//class SwaggerToSoaML {
//}

val METHODS = listOf<String>("get", "put", "post", "delete", "patch")
val integerType = SimpleType(SimpleType.INTEGER)
val longType = SimpleType(SimpleType.LONG)
val floatType = SimpleType(SimpleType.FLOAT)
val doubleType = SimpleType(SimpleType.DOUBLE)
val stringType = SimpleType(SimpleType.STRING)
val byteType = SimpleType(SimpleType.BYTE)
val booleanType = SimpleType(SimpleType.BOOLEAN)
val dateType = SimpleType(SimpleType.DATE)
val dateTimeType = SimpleType(SimpleType.DATE_TIME)
val passwordType = SimpleType(SimpleType.STRING)

fun getComplexType(name: String, api: Swagger): ComplexType {
    val attributes = mutableListOf<Attribute>()
    val schemaDef = api.definitions[name]!!
    schemaDef.properties.forEach { property ->
        val type = when (property.value.type) {
            "integer" -> {
                if (property.value.format == "int32")
                    integerType
                else
                    longType
            }
            "number" -> {
                if (property.value.format == "float")
                    floatType
                else
                    doubleType
            }
            "string" -> stringType
            "byte" -> byteType
            "boolean" -> booleanType
            "date" -> dateType
            "date-time" -> dateTimeType
            "password" -> passwordType
            "array" ->{
                stringType
                // TODO Do arrayType
            }
            else -> {
                val schemaName = (property.value as RefProperty).simpleRef
                getComplexType(schemaName, api)
            }
        }
        val atribute = Attribute(property.key, type)
        attributes.add(atribute)
    }
    return ComplexType(name, attributes as ArrayList<Attribute>?)
}


fun getOperation(operation: Operation, api: Swagger): edu.giisco.SoaML.metamodel.Operation {
    val parameters = mutableListOf<Parameter>()
    operation.parameters.forEach { parameter ->
        val type = when (parameter.`in`) {
            "path", "query" -> stringType
            else -> {
                val schemaName = ((parameter as BodyParameter).schema as RefModel).simpleRef
                getComplexType(schemaName, api)
            }
        }
        val apiParameter = Parameter(parameter.name, type)
        parameters.add(apiParameter)
    }
    val input = Input("input", parameters as ArrayList<Parameter>)
    var output: Output? = null
    var apiResponse: Response? = null
    val faults = mutableListOf<Fault>()
    operation.responses.forEach { response ->
        val responseSchema = response.value.responseSchema
        val type = when(responseSchema) {
            is RefModel -> {
                val schemaName = responseSchema.simpleRef
                getComplexType(schemaName, api)
            }
            else -> {
                stringType
            }
        }
        if (response.key.startsWith("2")) {
            parameters.clear()
            val apiParameter = Parameter(response.key, type)
            output = Output("response", ArrayList(listOf(apiParameter)))
            apiResponse = Response("respoonse", type)
        } else {
            val fault = Fault(response.key, type)
            faults.add(fault)
        }

    }
    val soaMLOperation = edu.giisco.SoaML.metamodel.Operation("das", input, output, ArrayList(faults), apiResponse)
    return soaMLOperation
}

fun main(args: Array<String>) {
    val swagger =
        SwaggerParser().read("/home/rapkyt/Project/Tesis/Resources/Experiments/dataset/SwaggerDataset/money-transfer-api (copia).json")
    val operations = mutableListOf<edu.giisco.SoaML.metamodel.Operation>()
    swagger.paths.forEach { path ->
        val pathValues =
            listOf<Operation?>(path.value.get, path.value.post, path.value.put, path.value.delete, path.value.patch)
        for (method in pathValues) {
            val operation = method?.let { getOperation(it, swagger) }
            if (operation != null)
                operations.add(operation)
        }
    }
    val swaggerInterface= Interface("das", ArrayList(operations))
}