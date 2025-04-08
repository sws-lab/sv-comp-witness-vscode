package witnesses.data.yaml

import kotlinx.serialization.Serializable

@Serializable
data class Constraint(
    val value : String,
    val format: String? = null
)
