package c.invariantAST

enum class Assoc { LEFT, RIGHT }

data class OpPrec(val precedence: Int, val assoc: Assoc)

// based on: https://en.cppreference.com/w/c/language/operator_precedence.html
fun opPrecedence(op: Op): OpPrec = when (op) {
    is UnaryOp, is CastOp -> OpPrec(2, Assoc.RIGHT)  // Unary + casts + sizeof

    is BinaryOp -> when (op.name) {
        ".", "->", "[]" -> OpPrec(1, Assoc.LEFT)

        "*", "/", "%" -> OpPrec(3, Assoc.LEFT)
        "+", "-" -> OpPrec(4, Assoc.LEFT)
        "<<", ">>" -> OpPrec(5, Assoc.LEFT)
        "<", "<=", ">", ">=" -> OpPrec(6, Assoc.LEFT)
        "==", "!=" -> OpPrec(7, Assoc.LEFT)
        "&" -> OpPrec(8, Assoc.LEFT)
        "^" -> OpPrec(9, Assoc.LEFT)
        "|" -> OpPrec(10, Assoc.LEFT)
        "&&" -> OpPrec(11, Assoc.LEFT)
        "||" -> OpPrec(12, Assoc.LEFT)

        "=", "+=", "-=", "*=", "/=", "%=", "&=", "|=", "^=", "<<=", ">>=" -> OpPrec(14, Assoc.RIGHT)
        else -> OpPrec(15, Assoc.LEFT) // unknown: lowest precedence
    }
}