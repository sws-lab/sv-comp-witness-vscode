package c.invariantAST

data class UnaryExpression(val op: UnaryOp, val exp: Expression, val str: String) : Expression() {
    override fun <T> accept(visitor: InvariantAstVisitor<T>) = visitor.visit(this)

    override fun normalize(): Expression {
        val inner = exp.normalize()

        // Double negation elimination: !!x => x
        if (op.name == "!" && inner is UnaryExpression && inner.op.name == "!") {
            return inner.exp
        }

        // !(x < c) => x >= c
        if (op.name == "!" && inner is BinaryExpression) {
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

        return UnaryExpression(op, inner, "${op.name}${inner.toCode()}")
    }

    override fun toCode() = "${op.name}${exp.toCode()}"

    override fun toValue() = str

}