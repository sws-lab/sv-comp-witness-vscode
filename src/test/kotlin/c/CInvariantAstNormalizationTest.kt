package c

import c.invariantAST.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

object CInvariantAstNormalizationTest {

    @Test
    fun test_double_negation() {
        assertEquals(Var("x"), CInvariantAst.createAst("!!x").normalize())
        assertEquals(
            BinaryExpression(Var("a"), "==", Var("b"), "a == b"),
            CInvariantAst.createAst("!!(a == b)").normalize()
        )

    }

    @Test
    fun test_logical_negation_f_relational_operators() {
        assertEquals(
            BinaryExpression(Var("x"), ">=", Const("1", null), "x >= 1"),
            CInvariantAst.createAst("!(x < 1)").normalize()
        )
        assertEquals(
            BinaryExpression(Var("y"), ">", Const("42", null), "y > 42"),
            CInvariantAst.createAst("!(y <= 42)").normalize()
        )
        assertEquals(
            BinaryExpression(Var("a"), "!=", Var("b"), "a != b"),
            CInvariantAst.createAst("!(a == b)").normalize()
        )
        assertEquals(
            BinaryExpression(Var("a"), "==", Var("b"), "a == b"),
            CInvariantAst.createAst("!(a != b)").normalize()
        )
        assertEquals(
            BinaryExpression(Var("a"), "==", Var("b"), "a == b"),
            CInvariantAst.createAst("!(b != a)").normalize()
        )
        assertEquals(
            BinaryExpression(Var("x"), "<", Const("1", null), "x < 1"),
            CInvariantAst.createAst("!(1 <= x)").normalize()
        )
    }

    @Test
    fun test_canonical_operand_ordering() {
        assertEquals(
            BinaryExpression(Var("x"), ">", Const("1", null), "x > 1"),
            CInvariantAst.createAst("1<x").normalize()
        )
        assertEquals(
            BinaryExpression(Var("y"), ">=", Const("2", null), "y >= 2"),
            CInvariantAst.createAst("2 <= y").normalize()
        )
        assertEquals(
            BinaryExpression(Var("z"), "<", Const("3", null), "z < 3"),
            CInvariantAst.createAst("3 > z").normalize()
        )
        assertEquals(
            BinaryExpression(Var("a"), "<=", Const("4", null), "a <= 4"),
            CInvariantAst.createAst("4 >= a").normalize()
        )
        assertEquals(
            BinaryExpression(Var("b"), "<=", Var("a"), "b <= a"),
            CInvariantAst.createAst("b <= a").normalize()
        ) // TODO?
    }

    @Test
    fun test_redundant_wrapping() {
        assertEquals(Var("x"), CInvariantAst.createAst("(x)").normalize())
        assertEquals(Var("y"), CInvariantAst.createAst("((y))").normalize())
        assertEquals(
            BinaryExpression(Var("x"), "==", Const("0", null), "x == 0"),
            CInvariantAst.createAst("x == (0)").normalize()
        )
        assertEquals(
            BinaryExpression(Var("x"), "==", Const("0", null), "x == 0"),
            CInvariantAst.createAst("(0) == (x)").normalize()
        )
        assertEquals(
            BinaryExpression(Var("a"), "<=", Const("4", null), "a <= 4"),
            CInvariantAst.createAst("4 >= ((a))").normalize()
        )
    }

    @Test
    fun test_tool_invariants() {
        assertEquals(
            BinaryExpression(
                BinaryExpression(
                    UnaryExpression(Type("(long long)"), Var("y"), "(long long)y"),
                    "+",
                    BinaryExpression(
                        UnaryExpression(Type("(long long)"), Var("x"), "(long long)x"),
                        "+",
                        Const("4294967296", "LL"),
                        "(long long)x + 4294967296LL"
                    ),
                    "(long long)y + (long long)x + 4294967296LL",
                ),
                ">=",
                Const("0", "LL"),
                "(long long)y + (long long)x + 4294967296LL >= 0LL"
            ),
            CInvariantAst.createAst("(4294967296LL + (long long )x) + (long long )y >= 0LL").normalize()
        )

    }


}