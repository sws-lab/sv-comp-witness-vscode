package c

import c.CInvariantEvaluator.collectMapping
import c.invariantAST.Node.Companion.variable
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

object CInvariantMapperTest {

    fun create_mapping(test: String): VariableMapping = collectMapping(CInvariantAst.createAst(test))

    @Test
    fun test_mapping_only_conjunctions() {
        val actual = create_mapping("(((1 <= counter) && (q == 0)) && (A == r))")
        val expected: VariableMapping = mutableMapOf()
        expected[variable("counter")] = mutableListOf("1<=counter")
        expected[variable("q")] = mutableListOf("q==0")
        expected[variable("A")] = mutableListOf("A==r")
        expected[variable("r")] = mutableListOf("A==r")
        assertEquals(expected, actual)
    }

    @Test
    fun test_mapping_inner_disjunction() {
        val actual = create_mapping("(((1 <= counter) || (q == 0)) && (A == r))")
        val expected: VariableMapping = mutableMapOf()
        expected[variable("counter")] = mutableListOf("(1<=counter)||(q==0)")
        expected[variable("q")] = mutableListOf("(1<=counter)||(q==0)")
        expected[variable("A")] = mutableListOf("A==r")
        expected[variable("r")] = mutableListOf("A==r")
        assertEquals(expected, actual)
    }

    @Test
    fun test_mapping_top_level_disjunction() {
        val invariant = "(B == (1U) && b == (1U) && q == (0U)) || (B == (1U) && b == (2U) && q == (0U))"
        val invariantNoSpaces = "(B==(1U)&&b==(1U)&&q==(0U))||(B==(1U)&&b==(2U)&&q==(0U))"
        val actual = create_mapping(invariant)
        val expected: VariableMapping = mutableMapOf()
        expected[variable("B")] = mutableListOf(invariantNoSpaces)
        expected[variable("b")] = mutableListOf(invariantNoSpaces)
        expected[variable("q")] = mutableListOf(invariantNoSpaces)
        assertEquals(expected, actual)
    }

}