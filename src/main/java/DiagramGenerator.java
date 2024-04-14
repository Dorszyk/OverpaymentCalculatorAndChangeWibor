import de.elnarion.util.plantuml.generator.classdiagram.PlantUMLClassDiagramGenerator;
import de.elnarion.util.plantuml.generator.classdiagram.config.PlantUMLClassDiagramConfigBuilder;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

public class DiagramGenerator {

    private static final String STARTUML_TO_REPLACE = "@startuml";
    private static final String STARTUML_REPLACEMENT = """
        @startuml
        !theme vibrant
        skinparam classAttributeIconSize 0
        """;

    public static void main(String[] args) throws IOException {
        String packageName = "pl.inbank.overpayment";
        String umlDiagramFileName = "overpayment.puml";
        Optional<String> diagramAsText = diagramAsText(packageName);
        if (diagramAsText.isPresent()) {
            writeToFile(diagramAsText.get(), umlDiagramFileName);
        }
    }

    private static Optional<String> diagramAsText(String packageName) {
        var config = new PlantUMLClassDiagramConfigBuilder(List.of(packageName))
            .withRemoveFields(true)
            .withHideMethods(true)
            .build();
        var generator = new PlantUMLClassDiagramGenerator(config);
        try {
            return Optional.of(generator.generateDiagramText()
                .replace(STARTUML_TO_REPLACE, STARTUML_REPLACEMENT)
                .replace(packageName + ".", ""));
        } catch (ClassNotFoundException | IOException ex) {
            System.err.printf("Error generating UML diagram, error: [%s]", ex);
        }
        return Optional.empty();
    }

    private static void writeToFile(String diagramAsText, String umlDiagramFileName) throws IOException {
        Path directory = Paths.get(".")
            .toRealPath()
            .resolve(Paths.get("src/main/resources"));
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }
        try (BufferedWriter writer = Files.newBufferedWriter(directory.resolve(umlDiagramFileName))) {
            writer.write(diagramAsText);
        }
    }


}
