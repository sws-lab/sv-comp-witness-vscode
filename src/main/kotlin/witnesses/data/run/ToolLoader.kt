package witnesses.data.run

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory

object ToolLoader {
    val tools: List<Tool> = try {
        val objectMapper = ObjectMapper(YAMLFactory())
        objectMapper.readValue(
            ToolLoader::class.java.getClassLoader().getResourceAsStream("tools.yml"),
            objectMapper.typeFactory.constructCollectionType(List::class.java, Tool::class.java)
        )
    } catch (e: Throwable) {
        e.printStackTrace()
        TODO("proper error handling")
    }
}