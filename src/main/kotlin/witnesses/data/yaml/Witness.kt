package witnesses.data.yaml

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnore

@JvmRecord
data class Witness(
        @JsonAlias("entry_type")
        val entry_type: String?,

        @JsonIgnore
        val metadata: MetaData?,

        val content: List<Content>
)
