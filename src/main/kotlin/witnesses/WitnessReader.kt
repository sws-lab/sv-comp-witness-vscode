package witnesses

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.decodeFromString
import witnesses.data.yaml.Witness

object WitnessReader {
    fun readWitnessFromYaml(witnessStrings: List<String>): List<Witness> {
        val serializer = Yaml(
            serializersModule = Yaml.default.serializersModule,
            configuration = Yaml.default.configuration.copy(
                strictMode = false,
                codePointLimit = Int.MAX_VALUE,
            )
        )
        return witnessStrings.flatMap { witness ->
            serializer.decodeFromString<List<Witness>>(witness)
        }
    }
}