package witnesses.data.yaml

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnore
import kotlinx.serialization.Serializable

@Serializable
data class Witness(
    @JsonAlias("entry_type")
    val entry_type: String? = null,
    @JsonIgnore
    val metadata: MetaData? = null,
    val content: List<ContentElement>
)
