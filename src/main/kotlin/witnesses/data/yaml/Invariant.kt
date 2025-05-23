package witnesses.data.yaml

import kotlinx.serialization.Serializable

@Serializable
data class Invariant(
    val type: String,
    val location: Location,
    val value: String,
    val format: String
)
