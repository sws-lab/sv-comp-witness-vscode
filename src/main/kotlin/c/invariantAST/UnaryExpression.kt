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

        return UnaryExpression(op, inner, getStr(op, inner))
    }

    override fun toCode() = getStr(op, exp)

    override fun toValue() = str

    private fun getStr(op: UnaryOp, exp: Expression) =
        when (op.name) {
            // there must be parentheses around sizeof and _Alignof
            "sizeof" -> "${op.name}(${exp.toCode()})"
            "_Alignof" -> "${op.name}(${exp.toCode()})"
            else -> "${op.name}${exp.toCode()}"
        }

}