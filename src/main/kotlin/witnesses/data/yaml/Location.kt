package witnesses.data.yaml

import com.fasterxml.jackson.annotation.JsonIgnore
import kotlinx.serialization.Serializable

@Serializable

data class Location(
    val file_name: String,
    @JsonIgnore
    val file_hash: String? = null,
    val line: Int,
    val column: Int? = null,
    val function: String? = null
)
