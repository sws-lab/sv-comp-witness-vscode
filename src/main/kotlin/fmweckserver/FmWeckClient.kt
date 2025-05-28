package fmweckserver

import com.google.protobuf.ByteString
import fm_weck.generated.FmWeckRemoteGrpc
import fm_weck.generated.FmWeckService
import io.grpc.netty.NettyChannelBuilder
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import java.io.IOException
import java.net.URI

class FmWeckClient(host: String, port: Int) {
    private val channel = NettyChannelBuilder.forAddress(host, port)
        .usePlaintext()
        .build()

    // TODO: replace blockingStub with futureStub after waitOnRequest gets fixed to respond with a unary (not iterator) response
    private val futureStub = FmWeckRemoteGrpc.newFutureStub(channel)
    private val blockingStub = FmWeckRemoteGrpc.newBlockingStub(channel)

    private val log: Logger = LogManager.getLogger(FmWeckClient::class.java)

    private fun getTool(tool: String): FmWeckService.ToolType =
        FmWeckService.ToolType.newBuilder()
            .setToolId(tool)
            .build()

    private fun getTool(tool: String, version: String): FmWeckService.ToolType =
        FmWeckService.ToolType.newBuilder()
            .setToolId(tool)
            .setToolVersion(version)
            .build()

    private fun getProperty(property: String): FmWeckService.Property =
        FmWeckService.Property.newBuilder()
            .setPropertyId(property)
            .build()

    fun startRun(message: AnalyzeMessageParams, tool: Tool): FmWeckService.RunID? {
        val request =
            try {
                FmWeckService.RunRequest.newBuilder()
                    .setProperty(getProperty(message.property))
                    .setTool(if (tool.version == null) getTool(tool.name) else getTool(tool.name, tool.version))
                    .setDataModel(message.dataModel)
                    .setCProgram(ByteString.copyFrom(File(URI.create(message.fileUri)).readBytes()))
                    .build()
            } catch (e: IOException) {
                throw java.lang.RuntimeException(e)
                TODO("proper error handling")
            }

        log.info("Start run request: tool=${request.tool.toolId}, property=${request.property.propertyId}, data model=${request.dataModel}")

        // Send request and get response
        val response: FmWeckService.RunID = blockingStub.startRun(request)
        log.info("Received response: run_id=${response.runId}")
        return response
    }

    fun waitOnRun(runId: FmWeckService.RunID?): List<String> {
        // Create a WaitParameters request
        val waitRequest = FmWeckService.WaitParameters.newBuilder()
            .setRunId(runId)
            .setTimeout(60) // TODO: Adjust timeout if needed
            .build()
        log.info("Wait on run request: timeout=${waitRequest.timeout}, run_id=${waitRequest.runId.runId}")

        // Fetch the output using a blocking call
        log.info("Waiting for results...")

        val waitRunResult: FmWeckService.WaitRunResult = blockingStub.waitOnRun(waitRequest)
        val runResult: FmWeckService.RunResult = waitRunResult.getRunResult()

        // TODO: handle other outcomes (timeout, error)
        if (runResult.success) {
            log.info("RunResult was successful")
            val fileQueryRequest = FmWeckService.FileQuery.newBuilder()
                .setRunId(runId)
                .addAllNamePatterns(listOf("*.yml"))
                .build()
            log.info("Wait on file query request: name_patterns=${fileQueryRequest.namePatternsList}, run_id=${fileQueryRequest.runId.runId}")
            val fileQueryResult = blockingStub.queryFiles(fileQueryRequest)
            return fileQueryResult.asSequence().map { file ->
                val fileContent = file.file.toStringUtf8()
                log.debug("Received file: ${file.name}")
                log.debug("File content: $fileContent")
                fileContent
            }.toList()
            // TODO: notify user about not receiving any files
        } else {
            // TODO: notify user about failed analysis
            log.error("Run was unsuccessful! $runResult")
            TODO("proper error handling")
        }
    }
}