package c

import c.invariantAST.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

object CInvariantAstNormalizationTest {

    @Test
    fun test_double_negation() {
        assertEquals(Var("x"), CInvariantAst.createAst("!!x").normalize())
        assertEquals(
            BinaryExpression(Var("a"), BinaryOp("=="), Var("b"), "a == b"),
            CInvariantAst.createAst("!!(a == b)").normalize()
        )

    }

    @Test
    fun test_logical_negation_of_relational_operators() {
        assertEquals(
            BinaryExpression(Var("x"), BinaryOp(">="), Const("1", null), "x >= 1"),
            CInvariantAst.createAst("!(x < 1)").normalize()
        )
        assertEquals(
            BinaryExpression(Var("y"), BinaryOp(">"), Const("42", null), "y > 42"),
            CInvariantAst.createAst("!(y <= 42)").normalize()
        )
        assertEquals(
            BinaryExpression(Var("a"), BinaryOp("!="), Var("b"), "a != b"),
            CInvariantAst.createAst("!(a == b)").normalize()
        )
        assertEquals(
            BinaryExpression(Var("a"), BinaryOp("=="), Var("b"), "a == b"),
            CInvariantAst.createAst("!(a != b)").normalize()
        )
        assertEquals(
            BinaryExpression(Var("a"), BinaryOp("=="), Var("b"), "a == b"),
            CInvariantAst.createAst("!(b != a)").normalize()
        )
        assertEquals(
            BinaryExpression(Var("x"), BinaryOp("<"), Const("1", null), "x < 1"),
            CInvariantAst.createAst("!(1 <= x)").normalize()
        )
    }

    @Test
    fun test_canonical_operand_ordering() {
        assertEquals(
            BinaryExpression(Var("x"), BinaryOp(">"), Const("1", null), "x > 1"),
            CInvariantAst.createAst("1<x").normalize()
        )
        assertEquals(
            BinaryExpression(Var("y"), BinaryOp(">="), Const("2", null), "y >= 2"),
            CInvariantAst.createAst("2 <= y").normalize()
        )
        assertEquals(
            BinaryExpression(Var("z"), BinaryOp("<"), Const("3", null), "z < 3"),
            CInvariantAst.createAst("3 > z").normalize()
        )
        assertEquals(
            BinaryExpression(Var("a"), BinaryOp("<="), Const("4", null), "a <= 4"),
            CInvariantAst.createAst("4 >= a").normalize()
        )
        assertEquals(
            BinaryExpression(Var("b"), BinaryOp("<="), Var("a"), "b <= a"),
            CInvariantAst.createAst("b <= a").normalize()
        ) // TODO?
    }

    @Test
    fun test_redundant_wrapping() {
        assertEquals(Var("x"), CInvariantAst.createAst("(x)").normalize())
        assertEquals(Var("y"), CInvariantAst.createAst("((y))").normalize())
        assertEquals(
            BinaryExpression(Var("x"), BinaryOp("=="), Const("0", null), "x == 0"),
            CInvariantAst.createAst("x == (0)").normalize()
        )
        assertEquals(
            BinaryExpression(Var("x"), BinaryOp("=="), Const("0", null), "x == 0"),
            CInvariantAst.createAst("(0) == (x)").normalize()
        )
        assertEquals(
            BinaryExpression(Var("a"), BinaryOp("<="), Const("4", null), "a <= 4"),
            CInvariantAst.createAst("4 >= ((a))").normalize()
        )
    }

    @Test
    fun test_dots_and_arrows() {
        assertEquals(
            BinaryExpression(
                BinaryExpression(
                    BinaryExpression(Var("queue"), BinaryOp("->"), Var("container"), "queue->container"),
                    BinaryOp("."),
                    Var("alloc"),
                    "queue->container.alloc"
                ),
                BinaryOp("=="),
                Const("0", null),
                "queue->container.alloc == 0"
            ),
            CInvariantAst.createAst("queue->container.alloc == 0").normalize()
        )
    }

    @Test
    fun test_preserving_parentheses() {
        // And vs Or
        assertEquals("(A || B) && C", CInvariantAst.createAst("(A || B) && C").normalize().toCode())
        assertEquals("A || B && C", CInvariantAst.createAst("A || (B && C)").normalize().toCode())
        // Bitwise AND vs Equality
        assertEquals("A == (B & C)", CInvariantAst.createAst("A == (B & C)").normalize().toCode())
        assertEquals("C & A == B", CInvariantAst.createAst("(A == B) & C").normalize().toCode())
        // Relational vs Additive
        assertEquals("A < B + C", CInvariantAst.createAst("A < (B + C)").normalize().toCode())
        assertEquals("C + (A < B)", CInvariantAst.createAst("(A < B) + C").normalize().toCode())
        // Additive vs Multiplicative
        assertEquals("A + B * C", CInvariantAst.createAst("A + B * C").normalize().toCode())
        assertEquals("C * (A + B)", CInvariantAst.createAst("(A + B) * C").normalize().toCode())
        // Bitwise OR vs Logical AND
        assertEquals("A && B | C", CInvariantAst.createAst("A && (B | C)").normalize().toCode())
        assertEquals("C | (A && B)", CInvariantAst.createAst("(A && B) | C").normalize().toCode())
        // Equality vs Logical OR
        assertEquals("A == B || C", CInvariantAst.createAst("(A == B) || C").normalize().toCode())
        assertEquals("A == (B || C)", CInvariantAst.createAst("A == (B || C)").normalize().toCode())
    }

    @Test
    fun test_tool_invariants() {
        assertEquals(
            BinaryExpression(
                BinaryExpression(
                    UnaryExpression(CastOp("(long long)"), Var("y"), "(long long)y"),
                    BinaryOp("+"),
                    BinaryExpression(
                        UnaryExpression(CastOp("(long long)"), Var("x"), "(long long)x"),
                        BinaryOp("+"),
                        Const("4294967296", "LL"),
                        "(long long)x + 4294967296LL"
                    ),
                    "(long long)y + (long long)x + 4294967296LL",
                ),
                BinaryOp(">="),
                Const("0", "LL"),
                "(long long)y + (long long)x + 4294967296LL >= 0LL"
            ),
            CInvariantAst.createAst("(4294967296LL + (long long )x) + (long long )y >= 0LL").normalize()
        )

        assertEquals(
            BinaryExpression(
                BinaryExpression(
                    BinaryExpression(Var("uts"), BinaryOp("->"), Var("version"), "uts->version"),
                    BinaryOp("[]"),
                    BinaryExpression(
                        UnaryExpression(
                            UnaryOp("sizeof"),
                            BinaryExpression(
                                Var("uts"),
                                BinaryOp("->"),
                                Var("version"),
                                "uts->version"
                            ),
                            "sizeof(uts->version)",
                        ),
                        BinaryOp("-"),
                        Const("1", "UL"),
                        "sizeof(uts->version) - 1UL"
                    ),
                    "uts->version[sizeof(uts->version) - 1UL]",
                ),
                BinaryOp("=="),
                UnaryExpression(CastOp("(char)"), Const("0", null), "(char)0"),
                "uts->version[sizeof(uts->version) - 1UL] == (char)0"
            ),
            CInvariantAst.createAst("(char)0 == uts->version[sizeof(uts->version) - 1UL]").normalize()
        )

    }

    @Test
    fun test_strings() {
        assertEquals(
            "&user->hh", CInvariantAst.createAst("& user->hh").normalize().toCode()
        )
        assertEquals(
            "&lock->local_queues[cluster] == local_queue",
            CInvariantAst.createAst("local_queue == & lock->local_queues[cluster]").normalize().toCode()
        )
    }


}