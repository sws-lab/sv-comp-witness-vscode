package combine.types

import combine.sat.CType
import kotlinx.serialization.json.Json
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File

typealias VariableTypeMap = Map<String, Map<Int, Map<Int, List<VariableType>>>>
typealias TypeEnv = Map<Int, Map<String, CType>>

object VariableTypeHandler {

    private val log: Logger = LogManager.getLogger(VariableTypeHandler::class.java)

    private fun startCPAChecker(programFileName: String, outputFileName: String): Process {
        // TODO: currently assumes that cpachecker is available locally: use through fm-weck
        val processBuilder = ProcessBuilder(
            // usage: bin/cpachecker --config config/generateCFA.properties <<C-Program>> --option cfa.variablesInScope=<<OUTPUT-FILE>> --option cfa.exportCfaAsync=false
            "../../../playground/cpachecker/bin/cpachecker",  // TODO: hardcoded path
            "--config", "../../../playground/cpachecker/config/generateCFA.properties", // TODO: hardcoded path
            programFileName, "--option", "cfa.variablesInScope=$outputFileName", "--option", "cfa.exportCfaAsync=false"
        )
        processBuilder.redirectErrorStream(true)
        val process = processBuilder.start()
        //log.info("CPAchecker asked for variable types for program: $programFileName")
        return process
    }

    private fun serializeTypesForProgram(typesJsonString: String): VariableTypeMap {
        val serializer = Json {
            serializersModule = Json.serializersModule
        }
        return serializer.decodeFromString(typesJsonString)
    }

    fun getVariableTypesForProgram(programFileName: String, outputFileName: String): VariableTypeMap {
        val cpaCheckerProcess = startCPAChecker(programFileName, outputFileName)
        if (cpaCheckerProcess.waitFor() == 0) {
            //log.info("CPAchecker completed for $programFileName")
            val typesJsonString = File("./output/$outputFileName").readText()
            val variableTypes = serializeTypesForProgram(typesJsonString)
            //log.info("Variable types map: $variableTypes")
            return variableTypes
        }
        log.error(cpaCheckerProcess.errorReader().readText())
        TODO("Proper error handling")
    }

    fun extractTypeEnvByLocation(data: VariableTypeMap): TypeEnv {
        return data.values
            .flatMap { it.entries }
            .groupBy({ it.key })
            .mapValues { (_, entries) ->
                entries
                    .flatMap { it.value.values }
                    .flatten()
                    .mapNotNull { vt ->
                        vt.simpleType
                            ?.let { CType.fromSimpleType(it) }
                            ?.let { type -> vt.name to type }
                    }.toMap()
            }
    }
}
