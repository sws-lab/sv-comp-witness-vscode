package witnesses

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
    private fun readResources(name: String) =
        this::class.java.classLoader.getResource("exampleWitnesses/$name")?.readText()

    val expectedCodeLenses = listOf(
        CodeLens(
            Range(Position(11, 8), Position(11, 8)),
            Command("( 0 < x )", "showInvariantInfo", listOf("CPAchecker")),
            null
        ),
        CodeLens(
            Range(Position(11, 8), Position(11, 8)),
            Command("1 <= x", "showInvariantInfo", listOf("Goblint")),
            null
        ),
        CodeLens(
            Range(Position(11, 8), Position(11, 8)),
            Command("0 == c", "showInvariantInfo", listOf("Goblint")),
            null
        ),
        CodeLens(
            Range(Position(11, 8), Position(11, 8)),
            Command("c == 0", "showInvariantInfo", listOf("Goblint")),
            null
        ),
        CodeLens(
            Range(Position(11, 8), Position(11, 8)),
            Command(
                "(((x <= 2147483647) && (1 <= x)) && (y <= 2147483647))", "showInvariantInfo", listOf("Automizer")
            ),
            null
        ),
    )

    @Test
    fun test_b16_witnesses_to_codeLenses() {
        val lenses = mutableListOf<CodeLens>()
        val witnesses = mutableListOf<Witness>()
        val analysisManager = AnalysisManager(FmWeckClient("host", 1000))
        witnessFiles.forEach {
            val witnessStrings = readResources(it)
            if (witnessStrings != null) witnesses.addAll(readWitnessFromYaml(listOf(witnessStrings)))
        }
        lenses.addAll(analysisManager.convert(witnesses))
        assertEquals(expectedCodeLenses, lenses)
    }

}