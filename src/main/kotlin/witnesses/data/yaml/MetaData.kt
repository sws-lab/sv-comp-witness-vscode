package witnesses.data.yaml

import kotlinx.serialization.Serializable
import witnesses.data.run.Tool

@Serializable
data class MetaData(
    val producer: Tool,
)
