package fmweckserver;

import com.google.protobuf.ByteString;
import fm_weck.generated.FmWeckRemoteGrpc;
import fm_weck.generated.FmWeckService;
import io.grpc.ManagedChannel;
import io.grpc.netty.NettyChannelBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import witnesses.data.run.Tool;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

public class FmWeckClient {
    private final ManagedChannel channel;
    // TODO: replace blockingStub with futureStub after waitOnRequest gets fixed to respond with a unary (not iterator) response
    private final FmWeckRemoteGrpc.FmWeckRemoteFutureStub futureStub;
    private final FmWeckRemoteGrpc.FmWeckRemoteBlockingStub blockingStub;

    private final Logger log = LogManager.getLogger(FmWeckClient.class);

    public FmWeckClient(String host, int port) {
        channel = NettyChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        futureStub = FmWeckRemoteGrpc.newFutureStub(channel);
        blockingStub = FmWeckRemoteGrpc.newBlockingStub(channel);
    }

    private FmWeckService.ToolType getTool(String tool) {
        return FmWeckService.ToolType.newBuilder()
                .setToolId(tool)
                .build();
    }

    private FmWeckService.ToolType getTool(String tool, String version) {
        return FmWeckService.ToolType.newBuilder()
                .setToolId(tool)
                .setToolVersion(version)
                .build();
    }

    private FmWeckService.Property getProperty(String property) {
        return FmWeckService.Property.newBuilder()
                .setPropertyId(property)
                .build();
    }

    public FmWeckService.RunID startRun(AnalyzeMessageParams message, Tool tool) {
        // Prepare the request
        FmWeckService.RunRequest request;
        try {
            Path filePath = Path.of(URI.create(message.fileUri()));
            if (tool.version() == null)
                // TODO: hardcoded values
                request = FmWeckService.RunRequest.newBuilder()
                        .setTool(getTool(tool.name()))
                        .setProperty(getProperty(message.property()))
                        .setDataModel(message.dataModel())
                        .setCProgram(ByteString.copyFrom(Files.readAllBytes(filePath)))
                        .build();
            else
                request = FmWeckService.RunRequest.newBuilder()
                        .setTool(getTool(tool.name(), tool.version()))
                        .setProperty(getProperty(message.property()))
                        .setDataModel(message.dataModel())
                        .setCProgram(ByteString.copyFrom(Files.readAllBytes(filePath)))
                        .build();
        } catch (IOException e) {
            //TODO: proper error handling
            throw new RuntimeException(e);
        }

        log.info("Start run request: " + request);

        // Send request and get response
        FmWeckService.RunID response = blockingStub.startRun(request);
        log.info("Received response: " + response);
        return response;
    }

    public String waitOnRun(FmWeckService.RunID runId) {
        // Create a WaitParameters request
        FmWeckService.WaitParameters waitRequest = FmWeckService.WaitParameters.newBuilder()
                .setRunId(runId)
                .setTimeout(60) // TODO: Adjust timeout if needed
                .build();
        log.info("Wait on run request: " + waitRequest);

        // Fetch the output using a blocking call
        log.info("Waiting for results...");

        try {
            FmWeckService.WaitRunResult waitRunResult = blockingStub.waitOnRun(waitRequest);
            FmWeckService.RunResult runResult = waitRunResult.getRunResult();
            // TODO: handle other outcomes (timeout, error)

            if (runResult.getSuccess()) {
                log.info("RunResult was successful");
                for (FmWeckService.File file : runResult.getFilesList()) {
                    String fileName = file.getName();
                    log.debug("Received file: " + fileName);
                    log.debug("File content: " + file.getFile().toStringUtf8());
                    if (fileName.equals("witness.yml")) {
                        return file.getFile().toStringUtf8();
                    }
                }
                // TODO: handle case where no files were received
            } else {
                // TODO: notify user about failed analysis
                // TODO: proper error handling
                log.error("Run was unsuccessful! " + runResult.getOutput());
                throw new RuntimeException();
            }
        } catch (Exception e) {
            log.error("Error while fetching results: " + e.getMessage(), e);
        }
        return null;
    }

}

