package c

import fmweckserver.FmWeckServer
import kotlinx.serialization.json.Json
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File

typealias VariableTypeMap = Map<String, Map<String, Map<String, List<VariableType>>>>

object VariableTypeHandler {

    private val log: Logger = LogManager.getLogger(FmWeckServer::class.java)

    private fun startCPAChecker(programFileName: String, outputFileName: String): Process {
        val processBuilder = ProcessBuilder(
            // : bin/cpachecker --config config/generateCFA.properties <<C-Program>> --option cfa.variablesInScope=<<OUTPUT-FILE>> --option cfa.exportCfaAsync=false
            "../../../playground/cpachecker/bin/cpachecker",  // TODO: hardcoded path
            "--config",
            "../../../playground/cpachecker/config/generateCFA.properties", // TODO: hardcoded path
            programFileName,
            "--option",
            "cfa.variablesInScope=$outputFileName",
            "--option",
            "cfa.exportCfaAsync=false"
        )
        processBuilder.redirectErrorStream(true)
        try {
            val process = processBuilder.start()
            log.info("CPAChecker asked for variable types for program: $programFileName")
            return process
        } catch (t: Throwable) {
            log.error("Failed to start CPAChecker", t)
            TODO("proper error handling")
        }
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
            log.info("cpachecker completed")
            val typesJsonString = File("./output/$outputFileName").readText()
            val variableTypes = serializeTypesForProgram(typesJsonString)
            log.info("variable types map: $variableTypes")

            return variableTypes
        }
        TODO("Proper error handling")
    }

}