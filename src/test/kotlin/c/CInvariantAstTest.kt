package c

import c.invariantAST.Node
import c.invariantAST.Node.Companion.binary
import c.invariantAST.Node.Companion.constant
import c.invariantAST.Node.Companion.ternary
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
        legal("x && y", binary(variable("x"), "&&", variable("y"), "x&&y"))
        legal("x || y", binary(variable("x"), "||", variable("y"), "x||y"))
        legal("x < 0 && y < 0", binary(`x LT 0`, "&&", `y LT 0`, "x<0&&y<0"))
        legal("x < 0 || y < 0", binary(`x LT 0`, "||", `y LT 0`, "x<0||y<0"))
        legal("x & y", binary(variable("x"), "&", variable("y"), "x&y"))
        legal("x ^ y", binary(variable("x"), "^", variable("y"), "x^y"))
        legal("x | y", binary(variable("x"), "|", variable("y"), "x|y"))
    }

    @Test
    fun test_arithmetic() {
        legal("x * y", binary(variable("x"), "*", variable("y"), "x*y"))
        legal("x / y", binary(variable("x"), "/", variable("y"), "x/y"))
        legal("x % y", binary(variable("x"), "%", variable("y"), "x%y"))
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
    fun test_ternary() {
        legal(
            "x > 0 ? 1 : 0",
            ternary(
                binary(variable("x"), ">", constant("0"), "x>0"),
                constant("1"),
                constant("0"),
                "x>0?1:0"
            )
        )
    }

    @Test
    fun test_cast_types() {
        // TODO properly
        CInvariantAst.createAst("((__int128) 2 * a)")
        CInvariantAst.createAst("(unsigned __int128) 1")
    }

    @Test
    fun test_all_other_operations() {
        // TODO properly
        CInvariantAst.createAst("((unsigned __int128) 1 << 64)")
        CInvariantAst.createAst("((k % ((unsigned __int128) 1 << 64)) + ((unsigned __int128) 1 << 64))")
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

    @Test
    fun test_parsing_real_tool_invariants() {
        // utaipan.2024-12-05_21-21-41.files/SV-COMP25_no-overflow/ArraysOfVariableLength6.yml/witness.yml
        CInvariantAst.createAst(
            "((((i >= 0) ? (i / 4294967296) : ((i / 4294967296) - 1)) <= 0) && (0 <= (i + 2147483648)))"
        )
        // utaipan.2024-12-05_21-21-41.files/SV-COMP25_unreach-call/sqrt1-ll_valuebound50.yml/witness.yml
        CInvariantAst.createAst(
            "((t == (1 + ((__int128) 2 * a))) && (((((__int128) a * a) + 1) + ((__int128) 2 * a)) == s))"
        )
        // utaipan.2024-12-05_21-21-41.files/SV-COMP25_no-overflow/geo2-ll_unwindbound1.yml/witness.yml
        CInvariantAst.createAst(
            "((((((((((y == z) " +
                    "&& (0 <= (k + 2147483648))) " +
                    "&& (k <= 2147483647)) " +
                    "&& (2 <= ((k >= 0) ? (k % ((unsigned __int128) 1 << 64)) : ((k % ((unsigned __int128) 1 << 64)) + ((unsigned __int128) 1 << 64))))) " +
                    "&& (0 <= ((__int128) 2147483647 + x))) " +
                    "&& (x == ((long long) z + 1))) " +
                    "&& (counter == 1)) " +
                    "&& (y <= 2147483647)) " +
                    "&& (2 == c)) || ((((((((0 <= (k + 2147483648)) " +
                    "&& (z <= 2147483647)) " +
                    "&& (0 <= (z + 2147483648))) " +
                    "&& (c == 1)) " +
                    "&& (k <= 2147483647)) " +
                    "&& (x == 1)) " +
                    "&& (y == 1)) " +
                    "&& (counter == 0)))"
        )
        // utaipan.2024-12-05_21-21-41.files/SV-COMP25_unreach-call/bin-suffix-5.yml/witness.yml
        CInvariantAst.createAst(
            "(5 == (x & 5))"
        )
        // utaipan.2024-12-05_21-21-41.files/SV-COMP25_unreach-call/double_req_bl_1092b.yml/witness.yml
        CInvariantAst.createAst(
            "(0 == (i1 | i0))"
        )
        // utaipan.2024-12-05_21-21-41.files/SV-COMP25_unreach-call/hardness_floatingpointinfluence_no-floats_file-9.yml/witness.yml
        CInvariantAst.createAst(
            "(((((var_1_12 <= 16) || ((64 == var_1_9) " +
                    "&& (var_1_11 == var_1_10))) " +
                    "&& (var_1_14 == var_1_12)) " +
                    "&& ((var_1_12 == last_1_var_1_12) || (((last_1_var_1_1 + last_1_var_1_12) % 4294967296) < 10))) || (((((10 == last_1_var_1_1) " +
                    "&& (var_1_1 == 10)) " +
                    "&& (1 == var_1_12)) " +
                    "&&!((10 << var_1_12) < (var_1_14 * var_1_3))) " +
                    "&& (last_1_var_1_12 == 1)))"
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