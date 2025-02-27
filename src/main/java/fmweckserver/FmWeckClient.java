package fmweckserver;

import com.google.protobuf.ByteString;
import fm_weck.generated.FmWeckRemoteGrpc;
import fm_weck.generated.FmWeckService;
import io.grpc.ManagedChannel;
import io.grpc.netty.NettyChannelBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import witnesses.data.Tool;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

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

    public FmWeckService.RunID startRun(URI fileUri, Tool tool) {
        // Prepare the request
        FmWeckService.RunInfo request;
        try {
            if (tool.version() == null)
                // TODO: hardcoded values
                request = FmWeckService.RunInfo.newBuilder()
                        .setTool(getTool(tool.name()))
                        .setProperty(getProperty("no-overflow"))
                        .setDataModel("LP64")
                        .setCProgram(ByteString.copyFrom(Files.readAllBytes(Path.of(fileUri))))
                        .build();
            else
                request = FmWeckService.RunInfo.newBuilder()
                        .setTool(getTool(tool.name(), tool.version()))
                        .setProperty(getProperty("no-overflow"))
                        .setDataModel("LP64")
                        .setCProgram(ByteString.copyFrom(Files.readAllBytes(Path.of(fileUri))))
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
                .setTimelimit(60)  // Adjust timeout if needed
                .build();
        log.info("Wait on run request: " + waitRequest);

        // Fetch the output using a blocking call
        log.info("Waiting for results...");

        try {
            Iterator<FmWeckService.File> it = blockingStub.waitOnRun(waitRequest);

            if (!it.hasNext()) {
                log.warn("No files received from server!");
            }

            while (it.hasNext()) {
                FmWeckService.File file = it.next();
                log.info("Received file: " + file.getName());
                log.debug("File content: " + file.getFile().toStringUtf8());
                if (file.getName().equals("witness.yml")) {
                    return file.getFile().toStringUtf8();
                }
            }
        } catch (Exception e) {
            log.error("Error while fetching results: " + e.getMessage(), e);
        }
        return null;
    }

}

