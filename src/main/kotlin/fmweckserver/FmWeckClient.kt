package fmweckserver

import com.google.protobuf.ByteString
import fm_weck.generated.FmWeckRemoteGrpc
import fm_weck.generated.FmWeckService
import io.grpc.netty.NettyChannelBuilder
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import witnesses.data.run.Tool
import java.io.File
import java.io.IOException
import java.net.URI

class FmWeckClient(host: String?, port: Int) {
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

        log.info("Start run request: $request")

        // Send request and get response
        val response: FmWeckService.RunID? = blockingStub.startRun(request)
        log.info("Received response: $response")
        return response
    }

    fun waitOnRun(runId: FmWeckService.RunID?): String {
        // Create a WaitParameters request
        val waitRequest = FmWeckService.WaitParameters.newBuilder()
            .setRunId(runId)
            .setTimeout(60) // TODO: Adjust timeout if needed
            .build()
        log.info("Wait on run request: $waitRequest")

        // Fetch the output using a blocking call
        log.info("Waiting for results...")

        val waitRunResult: FmWeckService.WaitRunResult = blockingStub.waitOnRun(waitRequest)
        val runResult: FmWeckService.RunResult = waitRunResult.getRunResult()

        // TODO: handle other outcomes (timeout, error)
        if (runResult.success) {
            log.info("RunResult was successful")
            for (file in runResult.filesList) {
                val fileName: String = file.getName()
                log.debug("Received file: $fileName")
                log.debug("File content: " + file.file.toStringUtf8())
                if (fileName == "witness.yml") {
                    return file.file.toStringUtf8()
                } else TODO("unimplemented: cases where no yaml files were received")
            }
            TODO("unimplemented: cases where no files were received")
        } else {
            // TODO: notify user about failed analysis
            log.error("Run was unsuccessful! " + runResult.getOutput())
            TODO("proper error handling")
        }
    }
}