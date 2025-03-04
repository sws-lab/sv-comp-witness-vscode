package witnesses.data.run

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import java.io.IOException

object ToolLoader {
    val tools: List<Tool> = try {
        val objectMapper = ObjectMapper(YAMLFactory())
        objectMapper.readValue<List<Tool>>(
            ToolLoader::class.java.getClassLoader().getResourceAsStream("tools.yml"),
            objectMapper.typeFactory.constructCollectionType(List::class.java, Tool::class.java)
        )
    } catch (e: IOException) {
        e.printStackTrace()
        TODO("proper error handling")
    }
}