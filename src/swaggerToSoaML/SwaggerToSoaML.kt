package swaggerToSoaML

import edu.giisco.SoaML.metamodel.*
import edu.giisco.SoaML.metamodel.Response
import io.swagger.models.*
import io.swagger.models.Operation
import io.swagger.parser.SwaggerParser;
import io.swagger.models.parameters.BodyParameter
import io.swagger.models.parameters.HeaderParameter
import io.swagger.models.parameters.PathParameter
import io.swagger.models.parameters.QueryParameter
import io.swagger.models.properties.ArrayProperty
import io.swagger.models.properties.DoubleProperty
import io.swagger.models.properties.Property
import io.swagger.models.properties.RefProperty
import java.io.File
import java.util.ArrayList

//
//class SwaggerToSoaML {
//}

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

fun getType(property: Property, api: Swagger): Type {
    return when (property.type) {
        "array" -> {
            val arrayType = getType((property as io.swagger.models.properties.ArrayProperty).items, api)
            ArrayType(arrayType)
        }

        "object" -> {
            if (property is io.swagger.models.properties.ObjectProperty) {
                val attributes = mutableListOf<Attribute>()
                property.properties?.forEach { property ->
                    val type = getType(property.value, api)
                    val atribute = Attribute(property.key, type)
                    attributes.add(atribute)
                }
                ComplexType(property.name, attributes as ArrayList<Attribute>?)
            } else {
                val schemaName =
                    ((property as io.swagger.models.properties.MapProperty).additionalProperties as RefProperty).simpleRef
                getComplexType(schemaName, api)
            }
        }
        "ref" -> {
            val schemaName = (property as RefProperty).simpleRef
            getComplexType(schemaName, api)
        }
        else -> {
            getSimpleType(property.type, property.format, null)
        }
    }
}

fun getSimpleType(type: String, format: String?, schema: ModelImpl?): Type {
    return when (type) {
        "integer" -> {
            if (format == "int32")
                integerType
            else
                longType
        }
        "number" -> {
            if (format == "float")
                floatType
            else
                doubleType
        }
        "string" -> stringType
        // TODO: FIX OTHER TYPES
        "byte" -> byteType
        "boolean" -> booleanType
        "date" -> dateType
        "date-time" -> dateTimeType
sta        "password" -> passwordType
        "object" -> {
            val attributes = mutableListOf<Attribute>()
            schema?.properties?.forEach { property ->
                val type = getSimpleType(property.value.type, property.value.format, schema)
                val atribute = Attribute(property.key, type)
                attributes.add(atribute)
            }
            ComplexType(schema?.name, attributes as ArrayList<Attribute>?)
        }
        else -> {
            throw IllegalArgumentException("Simple Type not found")
        }
    }
}


fun getComplexType(name: String, api: Swagger): ComplexType {
    val attributes = mutableListOf<Attribute>()
    val schemaDef = api.definitions[name]!!
    schemaDef.properties?.forEach { property ->
        val type = getType(property.value, api)
        val atribute = Attribute(property.key, type)
        attributes.add(atribute)
    }
    if (schemaDef is ModelImpl && schemaDef.additionalProperties != null) {
        val type = getType(schemaDef.additionalProperties, api)
        val atribute = Attribute(schemaDef.additionalProperties.title, type)
        attributes.add(atribute)
    }

    return ComplexType(name, attributes as ArrayList<Attribute>?)
}


fun getOperation(operation: Operation, api: Swagger): edu.giisco.SoaML.metamodel.Operation {
    val parameters = mutableListOf<Parameter>()
    operation.parameters.forEach { parameter ->
        val type = when(parameter){
            is HeaderParameter -> getSimpleType(parameter.type, parameter.format, null)
            is QueryParameter -> getSimpleType(parameter.type, parameter.format, null)
            is PathParameter -> getSimpleType(parameter.type, parameter.format, null)
            is BodyParameter -> {
                val schemaName = (parameter.schema as RefModel).simpleRef
                getComplexType(schemaName, api)
            }
            else -> {
                stringType
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
        val type: Type? = when (responseSchema) {
            is RefModel -> {
                val schemaName = responseSchema.simpleRef
                getComplexType(schemaName, api)
            }
            is ArrayModel -> {
                val arrayType = getType(responseSchema.items, api)
                ArrayType(arrayType)
            }
            is ModelImpl -> {
                getSimpleType(responseSchema.type, responseSchema.format, responseSchema)
            }
            null -> null
            else -> {
                // response sin schema?
                throw IllegalArgumentException("Response Type not found")
            }
        }
        if (response.key.startsWith("2")) {
            parameters.clear()
            if (type != null) {
                val apiParameter = Parameter(response.key, type)
                parameters.add(apiParameter)
            }
            output = Output("response", parameters)
            apiResponse = Response("response", type)
        } else {
            val fault = Fault(response.key, type)
            faults.add(fault)
        }

    }
    val soaMLOperation = edu.giisco.SoaML.metamodel.Operation("das", input, output, ArrayList(faults), apiResponse)
    return soaMLOperation
}

fun getInterface(jsonPath: String): Interface {
    val swagger = SwaggerParser().read(jsonPath)
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
    return Interface(swagger.info.title, ArrayList(operations))
}

fun main(args: Array<String>) {
    //    Pasar como argumento el path hacia los servicios
    val jsonPath = args[0]
    val interfaces = mutableListOf<Interface>()
    var failed = 0
    var index =0
    File(jsonPath).walk().filter { it.extension == "json" }.forEach{ file ->
        try{
            index ++
            println("${failed} de ${index} ${file.absolutePath}")
            val swaggerInterface = getInterface(file.absolutePath)
            interfaces.add(swaggerInterface)
        }
        catch(e:Exception){
            failed ++

        }
    }
    println("${failed} de ${index} Interfaces Created")
}
