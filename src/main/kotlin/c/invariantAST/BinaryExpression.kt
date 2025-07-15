package c.invariantAST

data class BinaryExpression(val left: Expression, val op: String, val right: Expression, val str: String) :
    Expression() {
    override fun <T> accept(visitor: InvariantAstVisitor<T>) = visitor.visit(this)

    override fun normalize(): Expression {
        val l = left.normalize()
        val r = right.normalize()

        // Commutative ops: order operands consistently
        if (op in setOf("==", "!=", "+", "*", "&", "|", "&&", "||")) {
            if (l is Const && r !is Const || r !is Const && r.hashCode() < l.hashCode()) {
                return BinaryExpression(r, op, l, "${r.toCode()} $op ${l.toCode()}")
            }
        }

        // Relational ops: move variable to left, constant to right (if reversed)
        if (l is Const && r !is Const) {
            val (newOp, newLeft, newRight) = when (op) {
                "<" -> Triple(">", r, l)
                "<=" -> Triple(">=", r, l)
                ">" -> Triple("<", r, l)
                ">=" -> Triple("<=", r, l)
                else -> Triple(op, l, r) // fallback
            }
            return BinaryExpression(newLeft, newOp, newRight, getStr(newLeft, newOp, newRight))
        }

        return BinaryExpression(l, op, r, getStr(l, op, r))
    }

    override fun toCode() = getStr(left, op, right)

    override fun toValue() = str

    private fun getStr(left: Expression, op: String, right: Expression) =
        when (op) {
            // no spaces around . and -> operations
            "." -> "${left.toCode()}$op${right.toCode()}"
            "->" -> "${left.toCode()}$op${right.toCode()}"
            "[]" -> "${left.toCode()}[${right.toCode()}]"
            else -> "${left.toCode()} $op ${right.toCode()}"
        }
}