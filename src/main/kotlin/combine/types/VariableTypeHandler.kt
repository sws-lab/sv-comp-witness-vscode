package combine.types

import combine.ksmt.CType
import kotlinx.serialization.json.Json
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

typealias VariableTypeMap = Map<String, Map<Int, Map<Int, List<VariableType>>>>
typealias TypeEnv = Map<Int, Map<String, CType>>

object VariableTypeHandler {

    private val userDir = System.getProperty("user.dir")
    private val cpacheckerBasePath = run {
        val submodulePath = Paths.get(userDir).resolve("sv-comp-witness-vscode").resolve("build/cpachecker")
        val directPath = Paths.get(userDir).resolve("build/cpachecker")

        if (Files.isDirectory(submodulePath)) submodulePath
        else if (Files.isDirectory(directPath)) directPath
        else throw IllegalStateException("CPAchecker base directory not found.")
    }

    private val log: Logger = LogManager.getLogger(VariableTypeHandler::class.java)

    fun startCPAChecker(programFileName: String, outputFileName: String): Process {

        // cli usage: bin/cpachecker --config config/generateCFA.properties ./tmp.c --option cfa.pathForExportingVariablesInScopeWithTheirType=out.json
        // val resourcePath = Paths.get(userDir).resolve("lib/cpachecker-native")
        val cpacheckerBinPath = cpacheckerBasePath.resolve("bin/cpachecker").toString()
        val configPath = cpacheckerBasePath.resolve("config/generateCFA.properties").toString()
        val processBuilder = ProcessBuilder(
            cpacheckerBinPath,
            "--config",
            configPath,
            "--option",
            "cfa.pathForExportingVariablesInScopeWithTheirType=$outputFileName",
            programFileName
        )
        processBuilder.redirectErrorStream(true)
        userDir.let { processBuilder.directory(File(it)) }
        val process = processBuilder.start()
        /* val outputGobbler = Thread {
            BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    println(line) // Print each line to your Kotlin app's stdout (which goes to terminal)
                }
            }
        }
        outputGobbler.start()*/
        return process
    }

    fun serializeTypesForProgram(typesJsonString: String): VariableTypeMap {
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
        val errorText = cpaCheckerProcess.errorReader().readText()
        log.error(errorText)
        TODO("Proper error handling (getVariableTypesForProgram): $errorText")
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
                        vt.type
                            ?.let { CType.fromSimpleType(it) }
                            ?.let { type -> vt.name to type }
                    }.toMap()
            }
    }
}
