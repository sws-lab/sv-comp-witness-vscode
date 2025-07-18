package c

import c.invariantAST.BinaryOp
import c.invariantAST.CastOp
import c.invariantAST.Node
import c.invariantAST.Node.Companion.binary
import c.invariantAST.Node.Companion.constant
import c.invariantAST.Node.Companion.ternary
import c.invariantAST.Node.Companion.unary
import c.invariantAST.Node.Companion.variable
import c.invariantAST.UnaryOp
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.test.assertNotEquals

object CInvariantAstTest {

    private val `x LT 0` = binary(variable("x"), BinaryOp("<"), constant("0", null), "x < 0")
    private val `y LT 0` = binary(variable("y"), BinaryOp("<"), constant("0", null), "y < 0")
    private val `i EQ 0` = binary(variable("i"), BinaryOp("=="), constant("0", null), "i == 0")
    private val `j EQ 0` = binary(variable("j"), BinaryOp("=="), constant("0", null), "j == 0")

    @Test
    fun test_variable_constant() {
        legal("x", variable("x"))
        legal("xyz", variable("xyz"))
        legal("x1", variable("x1"))
        legal("1", constant("1", null))
        legal("0.0", constant("0.0", null))
        legal("1L", constant("1", "L"))
        legal("1u", constant("1", "u"))
        legal("'c'", constant("'c'", null))
    }

    @Test
    fun testIntegerConstantSplitting() {
        assertEquals(constant("123", null), splitIntegerConstantAndSuffix("123"))
        assertEquals(constant("42", "U"), splitIntegerConstantAndSuffix("42U"))
        assertEquals(constant("10", "L"), splitIntegerConstantAndSuffix("10L"))
        assertEquals(constant("255", "ULL"), splitIntegerConstantAndSuffix("255ULL"))
        assertEquals(constant("0xFA", "uL"), splitIntegerConstantAndSuffix("0xFAuL"))
        assertEquals(constant("0b101", "LL"), splitIntegerConstantAndSuffix("0b101LL"))
        assertEquals(constant("789", "lU"), splitIntegerConstantAndSuffix("789lU"))
        assertEquals(constant("0o177", null), splitIntegerConstantAndSuffix("0o177"))
        assertEquals(constant("123.45F", null), splitIntegerConstantAndSuffix("123.45F"))
    }

    @Test
    fun test_comparison() {
        legal("x < y", binary(variable("x"), BinaryOp("<"), variable("y"), "x < y"))
        legal("x <= y", binary(variable("x"), BinaryOp("<="), variable("y"), "x <= y"))
        legal("x > y", binary(variable("x"), BinaryOp(">"), variable("y"), "x > y"))
        legal("x >= y", binary(variable("x"), BinaryOp(">="), variable("y"), "x >= y"))
        legalNonEqual("x < y", binary(variable("x"), BinaryOp("<="), variable("y"), "x < y"))
        legal("x == 0", binary(variable("x"), BinaryOp("=="), constant("0", null), "x == 0"))
        legal("0 != y", binary(constant("0", null), BinaryOp("!="), variable("y"), "0 != y"))
    }

    @Test
    fun test_logical() {
        legal("x && y", binary(variable("x"), BinaryOp("&&"), variable("y"), "x && y"))
        legal("x || y", binary(variable("x"), BinaryOp("||"), variable("y"), "x || y"))
        legal("x < 0 && y < 0", binary(`x LT 0`, BinaryOp("&&"), `y LT 0`, "x < 0 && y < 0"))
        legal("x < 0 || y < 0", binary(`x LT 0`, BinaryOp("||"), `y LT 0`, "x < 0 || y < 0"))
        legal("x & y", binary(variable("x"), BinaryOp("&"), variable("y"), "x & y"))
        legal("x ^ y", binary(variable("x"), BinaryOp("^"), variable("y"), "x ^ y"))
        legal("x | y", binary(variable("x"), BinaryOp("|"), variable("y"), "x | y"))
    }

    @Test
    fun test_arithmetic() {
        legal("x * y", binary(variable("x"), BinaryOp("*"), variable("y"), "x * y"))
        legal("x / y", binary(variable("x"), BinaryOp("/"), variable("y"), "x / y"))
        legal("x % y", binary(variable("x"), BinaryOp("%"), variable("y"), "x % y"))
    }

    @Test
    fun test_parentheses() {
        legal("(x)", variable("x"))
        legal("(x) < (y)", binary(variable("x"), BinaryOp("<"), variable("y"), "(x) < (y)"))
        legal("(x < y)", binary(variable("x"), BinaryOp("<"), variable("y"), "x < y"))
        legal("(x < 0) && (y < 0)", binary(`x LT 0`, BinaryOp("&&"), `y LT 0`, "(x < 0) && (y < 0)"))
        legal("(x < 0 || y < 0)", binary(`x LT 0`, BinaryOp("||"), `y LT 0`, "x < 0 || y < 0"))
        legal("((x < 0) && (y < 0))", binary(`x LT 0`, BinaryOp("&&"), `y LT 0`, "(x < 0) && (y < 0)"))
        legal(
            "(x < 0) && ((y < 0) || x > 0)",
            binary(
                `x LT 0`,
                BinaryOp("&&"),
                binary(
                    `y LT 0`,
                    BinaryOp("||"),
                    binary(variable("x"), BinaryOp(">"), constant("0", null), "x > 0"),
                    "(y < 0) || x > 0"
                ),
                "(x < 0) && ((y < 0) || x > 0)"
            )
        )
    }

    @Test
    fun test_ternary() {
        legal(
            "x > 0 ? 1 : 0",
            ternary(
                binary(variable("x"), BinaryOp(">"), constant("0", null), "x > 0"),
                constant("1", null),
                constant("0", null),
                "x > 0 ? 1 : 0"
            )
        )
    }

    @Test
    fun test_cast_types() {
        legal(
            "((__int128) 2 * a)",
            binary(
                unary(
                    CastOp("(__int128)"), constant("2", null), "(__int128) 2"
                ),
                BinaryOp("*"),
                variable("a"),
                "(__int128) 2 * a"
            ),
        )
        legal(
            "(unsigned __int128) 1",
            unary(CastOp("(unsigned __int128)"), constant("1", null), "(unsigned __int128) 1"),
        )
        legal(
            "len == (vuint32_t const   )4U",
            binary(
                variable("len"),
                BinaryOp("=="),
                unary(CastOp("(vuint32_t const)"), constant("4", "U"), "(vuint32_t const   )4U"),
                "len == (vuint32_t const   )4U"
            ),
        )
    }

    @Test
    fun test_all_other_operations() {
        // TODO properly
        CInvariantAst.createAst("((unsigned __int128) 1 << 64)")
        CInvariantAst.createAst("((k % ((unsigned __int128) 1 << 64)) + ((unsigned __int128) 1 << 64))")
    }

    @Test
    fun test_postfix_expressions() {
        legal("pqb.occupied", binary(variable("pqb"), BinaryOp("."), variable("occupied"), "pqb.occupied"))
        legal("qp->occupied", binary(variable("qp"), BinaryOp("->"), variable("occupied"), "qp->occupied"))
        legal(
            "a.b.c", binary(
                binary(variable("a"), BinaryOp("."), variable("b"), "a.b.c"),
                BinaryOp("."),
                variable("c"), "a.b.c"
            )
        )
    }

    @Test
    fun test_pointers() {
        legal("&pqb", unary(UnaryOp("&"), variable("pqb"), "&pqb"))
        legal("(void *)0", unary(CastOp("(void *)"), constant("0", null), "(void *)0"))
        val `((struct aws_array_list ptr)buf)` =
            unary(CastOp("(struct aws_array_list *)"), variable("buf"), "(struct aws_array_list *)buf")
        legal("((struct aws_array_list *)buf)", `((struct aws_array_list ptr)buf)`)
        legal(
            "(((struct aws_array_list *)buf)->alloc)->impl",
            binary(
                binary(
                    `((struct aws_array_list ptr)buf)`,
                    BinaryOp("->"),
                    variable("alloc"),
                    "((struct aws_array_list *)buf)->alloc"
                ),
                BinaryOp("->"),
                variable("impl"),
                "(((struct aws_array_list *)buf)->alloc)->impl",
            )
        )
    }

    @Test
    fun test_tool_invariants() {
        legal(
            "(((i == 0) && (j == 0)) || ((i == 0) && (1 <= j)))",
            binary(
                binary(`i EQ 0`, BinaryOp("&&"), `j EQ 0`, "(i == 0) && (j == 0)"),
                BinaryOp("||"),
                binary(
                    `i EQ 0`,
                    BinaryOp("&&"),
                    binary(
                        constant("1", null),
                        BinaryOp("<="),
                        variable("j"),
                        "1 <= j"
                    ),
                    "(i == 0) && (1 <= j)"
                ),
                "((i == 0) && (j == 0)) || ((i == 0) && (1 <= j))"
            )
        )
        legal(
            "(-1LL + (long long )A) + (long long )B >= 0LL",
            binary(
                binary(
                    binary(
                        unary(UnaryOp("-"), constant("1", "LL"), "-1LL"),
                        BinaryOp("+"),
                        unary(CastOp("(long long)"), variable("A"), "(long long )A"),
                        "-1LL + (long long )A"
                    ),
                    BinaryOp("+"),
                    unary(CastOp("(long long)"), variable("B"), "(long long )B"),
                    "(-1LL + (long long )A) + (long long )B"
                ),
                BinaryOp(">="),
                constant("0", "LL"),
                "(-1LL + (long long )A) + (long long )B >= 0LL"
            )
        )
    }

    @Test
    fun test_parsing_goblint_invariants() {
        // goblint.2024-11-29_20-22-51.files/SV-COMP25_no-data-race/13-privatized_68-pfscan_protected_loop_minimal_interval_true.yml/witness.yml
        CInvariantAst.createAst("0 <= pqb.occupied")
        CInvariantAst.createAst("0 <= qp->occupied")
        CInvariantAst.createAst("qp->occupied <= 1000")
        CInvariantAst.createAst("qp == & pqb")
        // goblint.2024-11-29_20-22-51.files/SV-COMP25_no-data-race/28-race_reach_75-tricky_address2_racefree.yml/witness.yml
        CInvariantAst.createAst("p == & a[i]")
        // goblint.2024-11-29_20-22-51.files/SV-COMP25_no-data-race/17_szymanski.yml/witness.yml
        CInvariantAst.createAst("arg == (void *)0")
        // goblint.2024-11-29_20-22-51.files/SV-COMP25_no-data-race/28-race_reach_19-callback_racing.yml/witness.yml
        // CInvariantAst.createAst("callback == (int (*)())(& bar)")
        // goblint.2024-11-29_20-22-51.files/SV-COMP25_no-data-race/arraylock.yml/witness.yml
        CInvariantAst.createAst("lock.flags == & flags[0]")
        CInvariantAst.createAst("lock->flags == & flags[0]")
        CInvariantAst.createAst("len == (vuint32_t const   )4U")
        // goblint.2024-11-29_20-22-51.files/SV-COMP25_no-overflow/aws_priority_queue_pop_harness.yml/witness.yml
        CInvariantAst.createAst("((struct aws_array_list *)buf)->alloc == 0 || (unsigned long )(((struct aws_array_list *)buf)->alloc)->impl == 0UL")
        // goblint.2024-11-29_20-22-51.files/SV-COMP25_no-data-race/hmcslock.yml/witness.yml
        CInvariantAst.createAst("locks_len == (vsize_t )7")
        // goblint.2024-11-29_20-22-51.files/SV-COMP25_no-overflow/comm_3args_ok.yml/witness.yml
        CInvariantAst.createAst("\"[\" == infomap[0].program")
        CInvariantAst.createAst("\"Multi-call invocation\" == infomap[1].node")
        CInvariantAst.createAst("\"sha512sum\" == infomap[5].program")
        // goblint.2024-11-29_20-22-51.files/SV-COMP25_no-overflow/du-1.yml/witness.yml
        CInvariantAst.createAst(
            "(((1ULL <= val && frac <= 10U) && \"%llu.%u%c\" == fmt) && (val <= 17592186044415ULL\n" +
                    "        || val <= 18014398509481983ULL)) || ((0U == frac && \"%llu\" == fmt) && frac\n" +
                    "        == 0U)"
        )
        // goblint.2024-11-29_20-22-51.files/SV-COMP25_no-overflow/cut-1.yml/witness.yml
        CInvariantAst.createAst("linelen == return_value_strlen\$1")
        CInvariantAst.createAst("tmp_if_expr\$7 == (_Bool)0")
        // goblint.2024-11-29_20-22-51.files/SV-COMP25_no-overflow/uname-1.yml/witness.yml
        CInvariantAst.createAst("(char)0 == uts->sysname[sizeof(uts->sysname) - 1UL]")
    }

    @Test
    fun test_parsing_utaipan_invariants() {
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
        CInvariantAst.createAst("(5 == (x & 5))")
        // utaipan.2024-12-05_21-21-41.files/SV-COMP25_unreach-call/double_req_bl_1092b.yml/witness.yml
        CInvariantAst.createAst("(0 == (i1 | i0))")
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