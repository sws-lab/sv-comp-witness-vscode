package c

import c.invariantAST.Node
import c.invariantAST.Node.Companion.binary
import c.invariantAST.Node.Companion.constant
import c.invariantAST.Node.Companion.variable
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CInvariantAstTest {

    private val invariant = "(((i == 0) && (j == 0)) || ((i == 0) && (1 <= j)))"

    @Test
    fun test_variable_constant() {
        legal("x", variable("x"))
        legal("xyz", variable("xyz"))
        legal("x1", variable("x1"))
        legal("1", constant("1"))
        legal("0.0", constant("0.0"))
        legal("1L", constant("1L"))
        legal("1u", constant("1u"))
        legal("'c'", constant("'c'"))
    }

    @Test
    fun test_comparison() {
        legal("x < y", binary(variable("x"), "<", variable("y")))
        legal("x <= y", binary(variable("x"), "<=", variable("y")))
        legal("x > y", binary(variable("x"), ">", variable("y")))
        legal("x >= y", binary(variable("x"), ">=", variable("y")))
    }

    private fun legal(input: String, expectedAst: Node) {
        val actualAst = CInvariantAst.createAst(input)
        assertEquals(expectedAst, actualAst)
    }
}