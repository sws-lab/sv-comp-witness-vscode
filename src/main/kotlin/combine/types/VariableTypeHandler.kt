package combine.types

import combine.ksmt.CType
import kotlinx.serialization.json.Json
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import java.nio.file.Paths

typealias VariableTypeMap = Map<String, Map<Int, Map<Int, List<VariableType>>>>
typealias TypeEnv = Map<Int, Map<String, CType>>

object VariableTypeHandler {

    private val log: Logger = LogManager.getLogger(VariableTypeHandler::class.java)

    fun startCPAChecker(programFileName: String, outputFileName: String): Process {
        // Get the path to the CPAchecker binary from resources
        val userDir = System.getProperty("user.dir")


        val resourcePath = Paths.get(userDir).resolve("lib/cpachecker-native")
        val cpacheckerBinPath = resourcePath.resolve("cpachecker").toString()
        val configPath = resourcePath.resolve("config/generateCFA.properties").toString()
        val processBuilder = ProcessBuilder(
            cpacheckerBinPath,
            "--config",
            configPath,
            "--option",
            "cfa.pathForExportingVariablesInScopeWithTheirType=$outputFileName",
            "--option",
            "cfa.exportCfaAsync=false",
            programFileName
        )
        processBuilder.redirectErrorStream(true)
        val process = processBuilder.start()
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
            .groupBy { it.key }
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
