package witnesses;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import witnesses.data.Tool;

import java.io.IOException;
import java.util.List;

public class ToolLoader {

    public static List<Tool> getTools() {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        try {
            return objectMapper.readValue(
                    ToolLoader.class.getClassLoader().getResourceAsStream("tools.yml"),
                    typeFactory.constructCollectionType(List.class, Tool.class));
        } catch (IOException e) {
            // TODO: proper error handling
            throw new RuntimeException(e);
        }
    }

}
