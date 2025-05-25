package combine.types

import kotlinx.serialization.Serializable

@Serializable
data class VariableType(
    val name: String,
    val simpleType: String? = null,
)
