package witnesses.data.yaml

import com.fasterxml.jackson.annotation.JsonIgnore

data class Location(
    val file_name: String,
    @JsonIgnore
    val file_hash: String?,
    val line: Int,
    val column: Int?,
    val function: String?
)
