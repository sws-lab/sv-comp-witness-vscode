package c.invariantAST

data class UnaryExpression(val op: String, val exp: Expression, val str: String) : Expression() {
    override fun <T> accept(visitor: InvariantAstVisitor<T>) = visitor.visit(this)

    override fun normalize(): Expression {
        val inner = exp.normalize()

        // Double negation elimination: !!x => x
        if (op == "!" && inner is UnaryExpression && inner.op == "!") {
            return inner.exp
        }

        // !(x < c) => x >= c
        if (op == "!" && inner is BinaryExpression) {
            val oppositeOp = when (inner.op) {
                "<" -> ">="
                "<=" -> ">"
                ">" -> "<="
                ">=" -> "<"
                "==" -> "!="
                "!=" -> "=="
                else -> null
            }

            if (oppositeOp != null) {
                return BinaryExpression(inner.left, oppositeOp, inner.right, "${inner.left.toCode()} $oppositeOp ${inner.right.toCode()}")
            }
        }

        return UnaryExpression(op, inner, "$op${inner.toCode()}")
    }

    override fun toCode() = "$op${exp.toCode()}"

    override fun toValue() = str

}