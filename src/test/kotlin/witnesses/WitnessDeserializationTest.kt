package witnesses

import c.CInvariantAst
import c.collectMapping
import org.junit.jupiter.api.Test
import witnesses.WitnessReader.addConjunctionDecompositionMaps
import witnesses.WitnessReader.readWitnessFromYaml
import witnesses.data.yaml.ContentElement
import witnesses.data.yaml.Invariant
import witnesses.data.yaml.Location
import witnesses.data.yaml.Witness
import kotlin.test.assertEquals

object WitnessDeserializationTest {

    @Test
    fun test_correctness_witness_deserialization() {
        val witnessFileContent =
            this::class.java.classLoader.getResource("exampleWitnesses/cpa-safe-program-example.witness.yml")
                ?.readText()
        if (witnessFileContent != null) {
            val actual = readWitnessFromYaml(listOf(witnessFileContent))
            val contentList: List<ContentElement> = listOf(
                ContentElement(
                    Invariant(
                        type = "loop_invariant",
                        location = Location(
                            file_name = "./safe-program-example.c",
                            file_hash = null,
                            line = 17,
                            column = 9,
                            function = "main"
                        ),
                        value = "s <= i*255 && 0 <= i && i <= 255 && n <= 255",
                        format = "c_expression",
                        decomposedConjunctionMap = null
                    ),
                    null
                )
            )
            val expected = listOf(Witness("invariant_set", null, contentList))
            assertEquals(actual, expected)
        }
    }

    @Test
    fun test_adding_mapping_to_witness_after_deserialization() {
        val witnessFileContent =
            this::class.java.classLoader.getResource("exampleWitnesses/cpa-safe-program-example.witness.yml")
                ?.readText()
        if (witnessFileContent != null) {
            val actual = addConjunctionDecompositionMaps(readWitnessFromYaml(listOf(witnessFileContent)))
            val contentList: List<ContentElement> = listOf(
                ContentElement(
                    Invariant(
                        type = "loop_invariant",
                        location = Location(
                            file_name = "./safe-program-example.c",
                            file_hash = null,
                            line = 17,
                            column = 9,
                            function = "main"
                        ),
                        value = "s <= i*255 && 0 <= i && i <= 255 && n <= 255",
                        format = "c_expression",
                        decomposedConjunctionMap =
                            collectMapping(CInvariantAst.createAst("s <= i*255 && 0 <= i && i <= 255 && n <= 255"))
                    ),
                    null
                )
            )
            val expected = listOf(Witness("invariant_set", null, contentList))
            assertEquals(actual, expected)
        }
    }
}