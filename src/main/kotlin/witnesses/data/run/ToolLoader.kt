package witnesses.data.run

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.decodeFromStream
import java.io.IOException

object ToolLoader {
    val tools: List<Tool> = try {
        val serializer = Yaml(
            serializersModule = Yaml.default.serializersModule,
            configuration = Yaml.default.configuration.copy(
                strictMode = false,
                codePointLimit = Int.MAX_VALUE,
            )
        )
        serializer.decodeFromStream<List<Tool>>(
            ToolLoader::class.java.getClassLoader().getResourceAsStream("tools.yml")!!
        )
    } catch (e: IOException) {
        e.printStackTrace()
        TODO("proper error handling")
    }
}