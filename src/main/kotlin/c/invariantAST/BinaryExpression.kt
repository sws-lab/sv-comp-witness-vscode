package c.invariantAST

data class BinaryExpression(val left: Expression, val op: BinaryOp, val right: Expression, val str: String) :
    Expression() {
    override fun <T> accept(visitor: InvariantAstVisitor<T>) = visitor.visit(this)

    override fun normalize(): Expression {
        val l = left.normalize()
        val r = right.normalize()

        // Commutative ops: order operands consistently
        if (op.name in setOf("==", "!=", "+", "*", "&", "|")) {
            if (l is Const && r !is Const || r !is Const && r.hashCode() < l.hashCode()) {
                return BinaryExpression(r, op, l, getStr(r, op, l))
            }
        }

        // Relational ops: move variable to left, constant to right (if reversed)
        if (l is Const && r !is Const) {
            val (newOp, newLeft, newRight) = when (op.name) {
                "<" -> Triple(BinaryOp(">"), r, l)
                "<=" -> Triple(BinaryOp(">="), r, l)
                ">" -> Triple(BinaryOp("<"), r, l)
                ">=" -> Triple(BinaryOp("<="), r, l)
                else -> Triple(op, l, r) // fallback
            }
            return BinaryExpression(newLeft, newOp, newRight, getStr(newLeft, newOp, newRight))
        }

        return BinaryExpression(l, op, r, getStr(l, op, r))
    }

    override fun toCode() = getStr(left, op, right)

    override fun toValue() = str

    private fun getStr(left: Expression, op: Op, right: Expression): String {
        val (parentPrec, parentAssoc) = opPrecedence(op)

        val lCode = left.toCode()
        val rCode = right.toCode()

        val lStr =
            if (left is BinaryExpression && (opPrecedence(left.op).precedence > parentPrec || (opPrecedence(left.op).precedence == parentPrec && parentAssoc == Assoc.RIGHT)))
                "($lCode)"
            else
                lCode

        val rStr = when (right) {
            is BinaryExpression -> {
                val rPrec = opPrecedence(right.op).precedence
                // TODO: [] hack
                if (rPrec > parentPrec && op.name != "[]" || (rPrec == parentPrec && parentAssoc == Assoc.RIGHT)) "($rCode)" else rCode
            }
            else -> rCode
        }

        return when (op.name) {
            ".", "->" -> "$lStr${op.name}$rStr"
            "[]" -> "$lStr[$rStr]"
            else -> "$lStr ${op.name} $rStr"
        }
    }
}