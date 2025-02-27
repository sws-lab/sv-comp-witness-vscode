package witnesses;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import witnesses.data.Content;
import witnesses.data.Invariant;
import witnesses.data.Location;
import witnesses.data.Witness;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class WitnessReader {

    private static final Logger log = LogManager.getLogger(WitnessReader.class);

    private List<Witness> readWitness(String path) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        List<Witness> witnesses = objectMapper.readValue(new File(path), typeFactory.constructCollectionType(List.class, Witness.class));

        log.debug(witnesses);

        return witnesses;

    }

    private List<CodeLens> convertWitnessToCodeLenses(List<Witness> witnesses) {
        List<CodeLens> codeLenses = new ArrayList<>();
        for (Witness witness : witnesses) {
            // TODO: nullable content field
            for (Content content : witness.content()) {
                Invariant invariant = content.invariant();
                Location location = invariant.location();
                Range range = new Range();
                // Position is zero-based as opposed to witnesses, where min value is 1
                range.setStart(new Position(location.line() - 1, location.column() - 1));
                range.setEnd(new Position(location.line() - 1, location.column() - 1));
                Command command = new Command(invariant.value(), "");
                CodeLens codeLens = new CodeLens(range, command, null);
                codeLenses.add(codeLens);
            }
        }
        return codeLenses;
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

    public static List<Witness> filterWitnesses(List<Witness> witnesses) {
        List<Witness> filteredWitnesses = new ArrayList<>();
        Set<String> uniqueInvariants = new HashSet<>();
        for (Witness witness : witnesses) {
            List<Content> filteredContent = witness.content().stream()
                    .filter(content -> {
                        Invariant invariant = content.invariant();
                        Location location = invariant.location();
                        String fileName = Paths.get(location.file_name()).getFileName().toString();
                        String key = fileName + ":" + location.line() + ":" + location.column() + ":" + invariant.value();

                        // Skip if the value is "1" or if we've already seen this (duplicate)
                        return !invariant.value().equals("1") && uniqueInvariants.add(key);
                    })
                    .collect(Collectors.toList());
            // Only add witness if it has remaining content
            if (!filteredContent.isEmpty()) {
                filteredWitnesses.add(new Witness(witness.entry_type(), witness.metadata(), filteredContent));
            }
        }
        return filteredWitnesses;
    }

    public List<CodeLens> readAndConvertWitnesses(String directoryPath) throws IOException {
        log.info("Read witnesses and convert them to code lenses");
        List<Witness> witnesses = filterWitnesses(readWitnessesFromDirectory(directoryPath));
        return convertWitnessToCodeLenses(witnesses);
    }

    public List<CodeLens> readAndConvertWitness(String witness) throws IOException {
        log.info("Read witnesses and convert them to code lenses");
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        List<Witness> witnesses = objectMapper.readValue(witness, typeFactory.constructCollectionType(List.class, Witness.class));
        return convertWitnessToCodeLenses(witnesses);
    }

}
