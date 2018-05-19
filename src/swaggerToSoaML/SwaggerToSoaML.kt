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
import io.swagger.models.properties.*
import java.io.File
import java.util.ArrayList


class SwaggerToSoaML(val path: String, private var api: Swagger? = null) {

    val integerType = SimpleType(SimpleType.INTEGER)
    val longType = SimpleType(SimpleType.LONG)
    val floatType = SimpleType(SimpleType.FLOAT)
    val doubleType = SimpleType(SimpleType.DOUBLE)
    val stringType = SimpleType(SimpleType.STRING)
    val byteType = SimpleType(SimpleType.BASE64)
    val binaryType = SimpleType(SimpleType.BYTE)
    val booleanType = SimpleType(SimpleType.BOOLEAN)
    val dateType = SimpleType(SimpleType.DATE)
    val dateTimeType = SimpleType(SimpleType.DATE_TIME)
    val passwordType = SimpleType(SimpleType.STRING)
    val fileType = SimpleType(SimpleType.BASE64_BINARY)

    val pendingComplexTypes = mutableListOf<String>()

    fun getType(property: Property, key: String = ""): Type {
        return when (property) {
            is io.swagger.models.properties.ArrayProperty -> {
                val arrayType = getType(property.items, "$key.items")
                ArrayType(arrayType)
            }
            is io.swagger.models.properties.ObjectProperty -> {
                val attributes = mutableListOf<Attribute>()
                property.properties?.forEach { property ->
                    val type = getType(property.value, property.key)
                    val atribute = Attribute(property.key, type)
                    attributes.add(atribute)
                }
                ComplexType(property.name, attributes as ArrayList<Attribute>?)
            }
            is io.swagger.models.properties.MapProperty -> {
                val addittionalProperties = property.additionalProperties
                val additionalPropertyType = getType(addittionalProperties, "$key.additionalProperties")
                val atribute = Attribute(addittionalProperties.title, additionalPropertyType)
                ComplexType(property.name, ArrayList(listOf(atribute)))
            }
            is RefProperty -> {
                val schemaName = property.simpleRef
                getComplexType(schemaName)
            }
            is UntypedProperty -> throw IllegalArgumentException("Property '$key' MUST define a 'type'")
            else -> getSimpleType(property.type, property.format)
        }
    }

    fun getSimpleType(type: String, format: String?, schema: ModelImpl? = null, properties: Property? = null): Type {
        return when (type) {
            "integer" ->
                when (format) {
                    "int64" -> longType
                    else -> integerType
                }
            "number" ->
                when (format) {
                    "double" -> doubleType
                    else -> floatType
                }
            "boolean" -> booleanType
            "string" ->
                when (format) {
                    "byte", "base64" -> byteType
                    "binary" -> binaryType
                    "date" -> dateType
                    "date-time" -> dateTimeType
                    "password" -> passwordType
                    else -> stringType
                }
            "file" -> fileType
            "object" -> {
                val attributes = mutableListOf<Attribute>()
                schema?.properties?.forEach { property ->
                    val type = getType(property.value, property.key)
                    val atribute = Attribute(property.key, type)
                    attributes.add(atribute)
                }
                ComplexType(schema?.name, attributes as ArrayList<Attribute>?)
            }
            "array" -> {
                val arrayType = getType(properties!!)
                ArrayType(arrayType)
            }
            else -> throw IllegalArgumentException("Type '${type}' is not supported")
        }
    }


    fun getComplexType(name: String): ComplexType {
        if (pendingComplexTypes.contains(name)) {
            throw IllegalArgumentException("Cyclic reference in '${name}' reference")
        }
        pendingComplexTypes.add(name)
        val attributes = mutableListOf<Attribute>()
        val schemaDef = this.api!!.definitions[name]!!
        schemaDef.properties?.forEach { property ->
            val type = getType(property.value, property.key)
            val atribute = Attribute(property.key, type)
            attributes.add(atribute)
        }
        if (schemaDef is ModelImpl && schemaDef.additionalProperties != null) {
            val type = getType(schemaDef.additionalProperties, "$name.additionalProperties")
            val atribute = Attribute(schemaDef.additionalProperties.title, type)
            attributes.add(atribute)
        }
        pendingComplexTypes.remove(name)
        return ComplexType(name, attributes as ArrayList<Attribute>?)
    }

    fun getParameterType(schema: Model?, key: String): Type? {
        return when (schema) {
            is RefModel -> {
                val schemaName = schema.simpleRef
                getComplexType(schemaName)
            }
            is ArrayModel -> {
                val arrayType = getType(schema.items, "$key.items")
                ArrayType(arrayType)
            }
            is ModelImpl -> {
                if (schema.type == null)
                    throw IllegalArgumentException("Schema Type must not be null in '$key'")
                getSimpleType(schema.type, schema.format, schema)
            }
            null -> null
            else -> {
                // response sin schema?
                throw IllegalArgumentException("Parameter Type '${schema}' not found")
            }
        }
    }

    fun getOperation(operation: Operation, path: String): edu.giisco.SoaML.metamodel.Operation {
        val parameters = mutableListOf<Parameter>()
        operation.parameters.forEach { parameter ->
            val type = when (parameter) {
                is HeaderParameter -> getSimpleType(parameter.type, parameter.format, properties = parameter.items)
                is QueryParameter -> getSimpleType(parameter.type, parameter.format, properties = parameter.items)
                is PathParameter -> getSimpleType(parameter.type, parameter.format, properties = parameter.items)
                is BodyParameter ->
                    getParameterType(parameter.schema, "${operation.operationId ?: path}.${parameter.name}")
                else -> stringType
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
            val type: Type? = getParameterType(responseSchema, "${operation.operationId ?: path} - Response: ${response.key}")
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

        val soaMLOperation = edu.giisco.SoaML.metamodel.Operation(path, input, output, ArrayList(faults), apiResponse)
        return soaMLOperation
    }

    fun getInterface(): Interface {
        val swagger = SwaggerParser().read(this.path)
        this.api = swagger
        val operations = mutableListOf<edu.giisco.SoaML.metamodel.Operation>()
        swagger.paths.forEach { path ->
            val pathValues = listOf(
                Pair(path.value.get, "GET"),
                Pair(path.value.post, "POST"),
                Pair(path.value.put, "PUT"),
                Pair(path.value.delete, "DELETE"),
                Pair(path.value.patch, "PATCH")
            )
            pathValues.filter { it.first != null }.forEach { (pathValue, key) ->
                val operation = getOperation(pathValue, "${key}_${path.key}")
                operations.add(operation)
            }
        }
        return Interface(swagger.info.title, ArrayList(operations))
    }
}

fun getInterfaces(jsonPath:String):ArrayList<Interface>{
    val interfaces = mutableListOf<Interface>()
    var failed = mutableSetOf<String>()
    var failedCount = 0
    var index = 0
    // TODO: parametros opcionales?
    File(jsonPath).walk().filter { it.extension == "json" || it.extension == "yaml" }.forEach { file ->
        try {
            index++
            println("${failedCount} of ${index} failed. Now processing ${file.absolutePath}")
            val swaggerToSoaML = SwaggerToSoaML(file.absolutePath)
            val swaggerInterface = swaggerToSoaML.getInterface()
            interfaces.add(swaggerInterface)
        } catch (e: Exception) {
            var message = e.toString()
            if ("java.lang.IllegalArgumentException: Cyclic reference" in message)
                message = "java.lang.IllegalArgumentException: Cyclic reference"
            if ("MUST define a 'type'" in message)
                message = "java.lang.IllegalArgumentException: Property MUST define a 'type'"
            if ("java.lang.IllegalArgumentException: Schema Type must not be null" in message)
                message = "java.lang.IllegalArgumentException: Schema Type must not be null"
            failed.add(message)
            failedCount++
            println(e)
        }
    }
    println("${index - failedCount} of ${index} Interfaces Created, ${failedCount} failed")
    println(failed)
    return ArrayList(interfaces)
}

fun main(args: Array<String>) {
    //    Pasar como argumento el path hacia los servicios
    val jsonPath = args[0]
    getInterfaces(jsonPath)
}
