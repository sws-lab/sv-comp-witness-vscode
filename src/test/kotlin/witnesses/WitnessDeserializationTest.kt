package witnesses

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.decodeFromString
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import witnesses.WitnessReader.readWitnessFromYaml
import witnesses.data.yaml.*
import kotlin.test.assertContains
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
                    ),
                    null
                )
            )
            val expected = listOf(
                Witness(
                    "invariant_set",
                    MetaData(
                        producer = Tool("CPAchecker", "2.2.1-svn"),
                        task = MetaData.Task(listOf("./safe-program-example.c"))
                    ),
                    contentList
                )
            )
            assertEquals(expected, actual)
        }
    }

    @Test
    fun test_adding_mapping_to_witness_after_deserialization() {
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
                    ),
                    null
                )
            )
            val expected = listOf(
                Witness(
                    "invariant_set",
                    MetaData(
                        producer = Tool("CPAchecker", "2.2.1-svn"),
                        task = MetaData.Task(listOf("./safe-program-example.c"))
                    ),
                    contentList
                )
            )
            assertEquals(expected, actual)
        }
    }

    @Test
    fun `kotlinx serialization can decode Constraint from YAML string`() {
        @Language("yaml")
        val yaml = """
            value: 'false'
        """.trimIndent()

        val expected = Constraint(
            value = "false",
            format = null,
        )

        val actual = Yaml.default.decodeFromString<Constraint>(yaml)

        assertEquals(expected, actual)
    }

    @Test
    fun `kotlinx serialization can decode Location from YAML string`() {
        @Language("yaml")
        val yaml = """
            file_name: "./safe-program-example.c"
            line: 17
            column: 9
            function: main
        """.trimIndent()

        val expected = Location(
            file_name = "./safe-program-example.c",
            file_hash = null,
            line = 17,
            column = 9,
            function = "main"
        )

        val actual = Yaml.default.decodeFromString<Location>(yaml)

        assertEquals(expected, actual)
    }

    @Test
    fun `kotlinx serialization can decode Waypoint from YAML string`() {
        @Language("yaml")
        val yaml = """
            type: branching
            action: follow
            constraint:
              value: 'false'
            location:
              file_name: "./safe-program-example.c"
              line: 17
              column: 9
              function: main
        """.trimIndent()

        val expected = Waypoint(
            type = "branching",
            action = "follow",
            constraint = Constraint(
                value = "false",
                format = null,
            ),
            location = Location(
                file_name = "./safe-program-example.c",
                file_hash = null,
                line = 17,
                column = 9,
                function = "main",
            ),
        )

        val actual = Yaml.default.decodeFromString<Waypoint>(yaml)

        assertEquals(expected, actual)
    }

    @Test
    fun `kotlinx serialization can decode Segment from YAML string`() {
        @Language("yaml")
        val yaml = """
            waypoint:
              type: branching
              action: follow
              constraint:
                value: 'false'
              location:
                file_name: "./safe-program-example.c"
                line: 17
                column: 9
                function: main
        """.trimIndent()

        val expected = Segment(
            waypoint = Waypoint(
                type = "branching",
                action = "follow",
                constraint = Constraint(
                    value = "false",
                    format = null,
                ),
                location = Location(
                    file_name = "./safe-program-example.c",
                    file_hash = null,
                    line = 17,
                    column = 9,
                    function = "main",
                ),
            ),
        )

        val actual = Yaml.default.decodeFromString<Segment>(yaml)

        assertEquals(expected, actual)
    }

    @Test
    fun `kotlinx serialization can decode Invariant from YAML string`() {
        @Language("yaml")
        val yaml = """
            type: loop_invariant
            location:
              file_name: "./safe-program-example.c"
              line: 17
              column: 9
              function: main
            value: "s <= i*255 && 0 <= i && i <= 255 && n <= 255"
            format: c_expression
        """.trimIndent()

        val expected = Invariant(
            type = "loop_invariant",
            location = Location(
                file_name = "./safe-program-example.c",
                file_hash = null,
                line = 17,
                column = 9,
                function = "main",
            ),
            value = "s <= i*255 && 0 <= i && i <= 255 && n <= 255",
            format = "c_expression",
        )

        val actual = Yaml.default.decodeFromString<Invariant>(yaml)

        assertEquals(expected, actual)
    }

    @Test
    fun `kotlinx serialization can decode ContentElement from YAML string`() {
        @Language("yaml")
        val yaml = """
            - invariant:
                type: loop_invariant
                location:
                  file_name: "./safe-program-example.c"
                  line: 17
                  column: 9
                  function: main
                value: "s <= i*255 && 0 <= i && i <= 255 && n <= 255"
                format: c_expression
            - segment:
              - waypoint:
                  type: branching
                  action: follow
                  constraint:
                    value: 'false'
                  location:
                    file_name: "./safe-program-example.c"
                    line: 17
                    column: 9
                    function: main
        """.trimIndent()

        val expected = listOf(
            ContentElement(
                invariant = Invariant(
                    type = "loop_invariant",
                    location = Location(
                        file_name = "./safe-program-example.c",
                        line = 17,
                        column = 9,
                        function = "main",
                    ),
                    value = "s <= i*255 && 0 <= i && i <= 255 && n <= 255",
                    format = "c_expression",
                ),
            ),
            ContentElement(
                segment = listOf(
                    Segment(
                        waypoint = Waypoint(
                            type = "branching",
                            action = "follow",
                            constraint = Constraint(value = "false"),
                            location = Location(
                                file_name = "./safe-program-example.c",
                                line = 17,
                                column = 9,
                                function = "main",
                            ),
                        ),
                    ),
                ),
            ),
        )

        val actual = Yaml.default.decodeFromString<List<ContentElement>>(yaml)

        assertEquals(expected, actual)
    }

    @Test
    fun `kotlinx serialization can decode Witness from YAML string`() {
        @Language("yaml")
        val yaml = """
            entry_type: "invariant_set"
            metadata:
              producer:
                name: "CPAchecker"
                version: "4.0"
            content:
            - invariant:
                type: loop_invariant
                location:
                  file_name: "./safe-program-example.c"
                  line: 17
                  column: 9
                  function: main
                value: "s <= i*255 && 0 <= i && i <= 255 && n <= 255"
                format: c_expression
            - segment:
              - waypoint:
                  type: branching
                  action: follow
                  constraint:
                    value: 'false'
                  location:
                    file_name: "./safe-program-example.c"
                    line: 17
                    column: 9
                    function: main
        """.trimIndent()

        val expected = Witness(
            entry_type = "invariant_set",
            metadata = MetaData(
                Tool(
                    name = "CPAchecker",
                    version = "4.0",
                )
            ),
            content = listOf(
                ContentElement(
                    invariant = Invariant(
                        type = "loop_invariant",
                        location = Location(
                            file_name = "./safe-program-example.c",
                            line = 17,
                            column = 9,
                            function = "main",
                        ),
                        value = "s <= i*255 && 0 <= i && i <= 255 && n <= 255",
                        format = "c_expression",
                    ),
                ),
                ContentElement(
                    segment = listOf(
                        Segment(
                            waypoint = Waypoint(
                                type = "branching",
                                action = "follow",
                                constraint = Constraint(value = "false"),
                                location = Location(
                                    file_name = "./safe-program-example.c",
                                    line = 17,
                                    column = 9,
                                    function = "main",
                                ),
                            ),
                        ),
                    ),
                ),
            ),
        )

        val actual = Yaml.default.decodeFromString<Witness>(yaml)

        assertEquals(expected, actual)
    }

    @Test
    fun `kotlinx serialization can decode large YAML files`() {
        val url =
            this::class.java.classLoader.getResource("exampleWitnesses/utaipan-SV-COMP25_unreach-call-Problem04_label58-witness.yml")
        val witnessFileContent = url!!.readText()

        val expectedContentElement = ContentElement(
            segment = listOf(
                Segment(
                    waypoint = Waypoint(
                        type = "target",
                        action = "follow",
                        location = Location(
                            file_name = "/tmp/vcloud_worker_vcloud-master_on_vcloud-master/run_dir_031efefa-b563-4e72-af6d-132025e796d0/sv-benchmarks/c/eca-rers2012/Problem04_label58.c",
                            file_hash = "af7c0d7fd905a9d96e3970a421d873415bffa615dff98c1f636a5c5973f2f4ea",
                            line = 4653,
                            column = 18,
                            function = "calculate_output4",
                        ),
                    ),
                ),
            ),
        )

        val expected =
            listOf(
                Witness(
                    entry_type = "violation_sequence",
                    metadata = MetaData(Tool("Taipan", "0.3.0-dev-d790fec")),
                    content = emptyList()
                )
            )

        val serializer = Yaml(
            serializersModule = Yaml.default.serializersModule,
            configuration = Yaml.default.configuration.copy(
                strictMode = false,
                codePointLimit = Int.MAX_VALUE,
            )
        )

        val actual = serializer.decodeFromString<List<Witness>>(witnessFileContent)

        assertEquals(expected.size, actual.size)

        val expectedWitness = expected.single()
        val actualWitness = actual.single()

        //println(actualWitness.content.size)

        assertEquals(expectedWitness.entry_type, actualWitness.entry_type)
        assertContains(actualWitness.content, expectedContentElement)
    }
}
