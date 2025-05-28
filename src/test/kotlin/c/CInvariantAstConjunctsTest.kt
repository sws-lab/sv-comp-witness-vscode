package c

import c.invariantAST.Node
import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.test.Test

object CInvariantAstConjunctsTest {

    private fun extractConjuncts(value: String): Set<Node> = collectConjunctAsts(CInvariantAst.createAst(value))

    @Test
    fun test_single_expression() {
        val expr = "x == 1"
        assertEquals(setOf(CInvariantAst.createAst(expr)), extractConjuncts(expr))
    }

    @Test
    fun test_flat_conjunction() {
        val expr = "x == 1 && y < 2 && z != 3"
        val expected = setOf(
            CInvariantAst.createAst("x == 1"),
            CInvariantAst.createAst("y < 2"),
            CInvariantAst.createAst("z != 3")
        )
        assertEquals(expected, extractConjuncts(expr))
    }

    @Test
    fun test_nested_conjunction() {
        val expr = "(x == 1 && y < 2) && z != 3"
        val expected = setOf(
            CInvariantAst.createAst("x == 1"),
            CInvariantAst.createAst("y < 2"),
            CInvariantAst.createAst("z != 3")
        )
        assertEquals(expected, extractConjuncts(expr))
    }

    @Test
    fun test_with_inner_disjunction() {
        val expr = "(x == 1 || y == 2) && z != 3"
        val expected = setOf(
            CInvariantAst.createAst("(x == 1 || y == 2)"),
            CInvariantAst.createAst("z != 3")
        )
        assertEquals(expected, extractConjuncts(expr))
    }

    @Test
    fun test_top_level_disjunction() {
        val expr = "(x == 1 && y == 2) || (a == 3 && b == 4)"
        assertEquals(setOf(CInvariantAst.createAst(expr)), extractConjuncts(expr))
    }

    @Test
    fun test_with_unary_expression() {
        val expr = "!(x < 5) && y == 3"
        val expected = setOf(
            CInvariantAst.createAst("!(x < 5)"),
            CInvariantAst.createAst("y == 3")
        )
        assertEquals(expected, extractConjuncts(expr))
    }

    @Test
    fun test_top_level_unary_expression() {
        val expr = "!(x < 5 && y == 3)"
        assertEquals(setOf(CInvariantAst.createAst(expr)), extractConjuncts(expr))
    }
}