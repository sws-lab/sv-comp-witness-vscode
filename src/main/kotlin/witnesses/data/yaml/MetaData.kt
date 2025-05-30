package witnesses.data.yaml

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MetaData(
    val producer: Tool,
    val task: Task = Task(listOf()),
) {
    @Serializable
    data class Task(
        @SerialName("input_files")
        val inputFiles: List<String>,
    )
}
