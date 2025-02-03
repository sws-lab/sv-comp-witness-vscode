package witnesses;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lsp.SVLanguageServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.lsp4j.*;
import witnesses.data.Content;
import witnesses.data.Invariant;
import witnesses.data.Location;
import witnesses.data.Witness;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class WitnessHandler {

    private static final Logger log = LogManager.getLogger(WitnessHandler.class);

    private List<Witness> readWitness(String path) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        List<Witness> witnesses = objectMapper.readValue(new File(path), typeFactory.constructCollectionType(List.class, Witness.class));

        log.debug(witnesses);

        return witnesses;

    }

    private void convertWitness(SVLanguageServer languageServer, List<Witness> witnesses) throws URISyntaxException {
        for (Witness witness : witnesses) {
            for (Content content : witness.content()) {
                Invariant invariant = content.invariant();
                Location location = invariant.location();
                Range range = new Range();
                // Position is zero-based as opposed to witnesses, where min value is 1
                range.setStart(new Position(location.line() - 1, location.column() - 1));
                range.setEnd(new Position(location.line() - 1, location.column() - 1));
                Command command = new Command(invariant.value(), "");
                CodeLens codeLens = new CodeLens(range, command, null);
                //InlayHint inlayHint = new InlayHint(new Position(location.line(), location.column()), Either.forLeft(invariant.value() + " "));
                // TODO: fragile URI stuff
                // TODO: hardcoded values
                //languageServer.addInlayHint(new URI("file://" + Path.of("").toAbsolutePath() + "/examples/safe-program-example.c"), inlayHint);
                languageServer.addCodeLens(new URI("file://" + Path.of("").toAbsolutePath() + "/examples/standard_strcpy_original-2.i"), codeLens);
            }
        }
    }

    private List<Witness> readWitnessesFromDirectory(String directoryPath) throws IOException {
        File directory = new File(directoryPath);
        List<Witness> allWitnesses = new ArrayList<>();
        if (directory.exists() && directory.isDirectory()) {
            File[] yamlFiles = directory.listFiles((dir, name) -> name.endsWith(".yml"));
            if (yamlFiles != null) {
                for (File yamlFile : yamlFiles) {
                    allWitnesses.addAll(readWitness(yamlFile.getAbsolutePath()));
                }
            }
        } else {
            throw new IOException("Invalid directory path: " + directoryPath);
        }
        return allWitnesses;
    }

    public void readAndConvertWitnesses(SVLanguageServer languageServer, String directoryPath) throws IOException, URISyntaxException {
        List<Witness> witnesses = readWitnessesFromDirectory(directoryPath);
        convertWitness(languageServer, witnesses);
    }

}
