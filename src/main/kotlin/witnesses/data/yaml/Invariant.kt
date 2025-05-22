package witnesses.data.yaml

import c.VariableMapping
import kotlinx.serialization.Serializable
import witnesses.data.run.Tool

@Serializable
data class Invariant(
    val type: String,
    val location: Location,
    val value: String,
    val format: String,
    var decomposedConjunctionMap: VariableMapping? = null,
    var tool: Tool? = null,
    var normValue: String? = null
)
