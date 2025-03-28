package c

import c.invariantAST.Node
import c.invariantAST.Node.Companion.binary
import c.invariantAST.Node.Companion.constant
import c.invariantAST.Node.Companion.unary
import c.invariantAST.Node.Companion.variable
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.test.assertNotEquals

object CInvariantAstTest {

    private val `x LT 0` = binary(variable("x"), "<", constant("0"), "x<0")
    private val `y LT 0` = binary(variable("y"), "<", constant("0"), "y<0")
    private val `i EQ 0` = binary(variable("i"), "==", constant("0"), "i==0")
    private val `j EQ 0` = binary(variable("j"), "==", constant("0"), "j==0")

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
        legal("x < y", binary(variable("x"), "<", variable("y"), "x<y"))
        legal("x <= y", binary(variable("x"), "<=", variable("y"), "x<=y"))
        legal("x > y", binary(variable("x"), ">", variable("y"), "x>y"))
        legal("x >= y", binary(variable("x"), ">=", variable("y"), "x>=y"))
        legalNonEqual("x < y", binary(variable("x"), "<=", variable("y"), "x<y"))
        legal("x == 0", binary(variable("x"), "==", constant("0"), "x==0"))
        legal("0 != y", binary(constant("0"), "!=", variable("y"), "0!=y"))
    }

    @Test
    fun test_logical() {
        legal("x < 0 && y < 0", binary(`x LT 0`, "&&", `y LT 0`, "x<0&&y<0"))
        legal("x < 0 || y < 0", binary(`x LT 0`, "||", `y LT 0`, "x<0||y<0"))
    }

    @Test
    fun test_parentheses() {
        legal("(x)", variable("x"))
        legal("(x) < (y)", binary(variable("x"), "<", variable("y"), "(x)<(y)"))
        legal("(x < y)", binary(variable("x"), "<", variable("y"), "x<y"))
        legal("(x < 0) && (y < 0)", binary(`x LT 0`, "&&", `y LT 0`, "(x<0)&&(y<0)"))
        legal("(x < 0 || y < 0)", binary(`x LT 0`, "||", `y LT 0`, "x<0||y<0"))
        legal("((x < 0) && (y < 0))", binary(`x LT 0`, "&&", `y LT 0`, "(x<0)&&(y<0)"))
        legal(
            "(x < 0) && ((y < 0) || x > 0)",
            binary(
                `x LT 0`,
                "&&",
                binary(
                    `y LT 0`,
                    "||",
                    binary(variable("x"), ">", constant("0"), "x>0"),
                    "(y<0)||x>0"
                ),
                "(x<0)&&((y<0)||x>0)"
            )
        )
    }

    @Test
    fun test_tool_invariants() {
        legal(
            "(((i == 0) && (j == 0)) || ((i == 0) && (1 <= j)))",
            binary(
                binary(`i EQ 0`, "&&", `j EQ 0`, "(i==0)&&(j==0)"),
                "||",
                binary(
                    `i EQ 0`,
                    "&&",
                    binary(
                        constant("1"),
                        "<=",
                        variable("j"),
                        "1<=j"
                    ),
                    "(i==0)&&(1<=j)"
                ),
                "((i==0)&&(j==0))||((i==0)&&(1<=j))"
            )
        )
        // TODO: separate typeNames with space: e.g. longlong -> long long
        legal(
            "(-1LL + (long long )A) + (long long )B >= 0LL",
            binary(
                binary(
                    binary(
                        unary("-", constant("1LL"), "-1LL"),
                        "+",
                        unary("longlong", variable("A"), "(longlong)A"),
                        "-1LL+(longlong)A"
                    ),
                    "+",
                    unary("longlong", variable("B"), "(longlong)B"),
                    "(-1LL+(longlong)A)+(longlong)B"
                ),
                ">=",
                constant("0LL"),
                "(-1LL+(longlong)A)+(longlong)B>=0LL"
            )
        )
    }


    private fun legal(input: String, expectedAst: Node) {
        val actualAst = CInvariantAst.createAst(input)
        assertEquals(expectedAst, actualAst)
    }

    private fun legalNonEqual(input: String, nonExpectedAst: Node) {
        val actualAst = CInvariantAst.createAst(input)
        assertNotEquals(nonExpectedAst, actualAst)
    }
}