package witnesses;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lsp.SVLanguageServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import witnesses.data.Content;
import witnesses.data.Invariant;
import witnesses.data.Location;
import witnesses.data.Witness;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;

public class WitnessHandler {

    private static final Logger log = LogManager.getLogger(WitnessHandler.class);

    private List<Witness> readWitnesses(String path) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        List<Witness> witnesses = objectMapper.readValue(new File(path), typeFactory.constructCollectionType(List.class, Witness.class));

        log.info(witnesses);

        return witnesses;

    }

    private void convertWitnesses(SVLanguageServer languageServer, List<Witness> witnesses) throws URISyntaxException {
        for (Witness witness : witnesses) {
            for (Content content : witness.content()) {
                Invariant invariant = content.invariant();
                Location location = invariant.location();
                Range range = new Range();
                range.setStart(new Position(location.line(), location.column()));
                range.setEnd(new Position(location.line(), location.column()));
                Command command = new Command(invariant.value(), "");
                CodeLens codeLens = new CodeLens(range, command, null);
                //InlayHint inlayHint = new InlayHint(new Position(location.line(), location.column()), Either.forLeft(invariant.value() + " "));
                // TODO: fragile URI stuff
                // TODO: hardcoded values
                //languageServer.addInlayHint(new URI("file://" + Path.of("").toAbsolutePath() + "/examples/safe-program-example.c"), inlayHint);
                languageServer.addCodeLens(new URI("file://" + Path.of("").toAbsolutePath() + "/examples/safe-program-example.c"), codeLens);
            }
        }
    }

    public void readAndConvertWitnesses(SVLanguageServer languageServer, String path) throws IOException, URISyntaxException {
        List<Witness> witnesses = readWitnesses(path);
        convertWitnesses(languageServer, witnesses);
    }

}
