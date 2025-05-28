package witnesses

import fmweckserver.AnalyzeMessageParams
import fmweckserver.FmWeckClient
import org.eclipse.lsp4j.CodeLens
import org.eclipse.lsp4j.Command
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import witnesses.WitnessReader.readWitnessFromYaml
import witnesses.data.yaml.Witness
import kotlin.test.Test
import kotlin.test.assertEquals

object InvariantToCodeLensesTest {

    private val witnessFiles = listOf("b.16.cpachecker.yml", "b.16.goblint.yml", "b.16.uautomizer.yml")
    private fun readResources(name: String) = this::class.java.classLoader.getResource("exampleWitnesses/$name")?.readText()

    val expectedCodeLenses = listOf(
        CodeLens(
            Range(Position(11, 8), Position(11, 8)),
            Command("x > 0", "showInvariantInfo", listOf("CPAchecker: 0 < x, Goblint: 1 <= x, Automizer: 1 <= x")),
            null
        ),
        CodeLens(
            Range(Position(11, 8), Position(11, 8)),
            Command(
                "x <= 2147483647", "showInvariantInfo", listOf("Automizer: x <= 2147483647")
            ),
            null
        ),
        CodeLens(
            Range(Position(11, 8), Position(11, 8)),
            Command("c == 0", "showInvariantInfo", listOf("Goblint: c == 0")),
            null
        ),
        CodeLens(
            Range(Position(11, 8), Position(11, 8)),
            Command(
                "y <= 2147483647", "showInvariantInfo", listOf("Automizer: y <= 2147483647")
            ),
            null
        )
    )

    @Test
    fun test_b16_witnesses_to_codeLenses() {
        val lenses = mutableListOf<CodeLens>()
        val witnesses = mutableListOf<Witness>()
        val message = AnalyzeMessageParams(
            "", "", "no_overflow", listOf("cpachecker", "goblint", "uautomizer"),
            "b.16.c", "examples/b.16.c"
        )
        val analysisManager = AnalysisManager(FmWeckClient("host", 1000))
        witnessFiles.forEach {
            val witnessStrings = readResources(it)
            if (witnessStrings != null) witnesses.addAll(readWitnessFromYaml(listOf(witnessStrings)))
        }
        lenses.addAll(analysisManager.convert(witnesses, message))
        assertEquals(expectedCodeLenses, lenses)
    }

}