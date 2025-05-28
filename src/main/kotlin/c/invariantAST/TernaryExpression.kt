package c.invariantAST

data class TernaryExpression(val fst: Expression, val snd: Expression, val thrd: Expression, val str: String) :
    Expression() {
    override fun <T> accept(visitor: InvariantAstVisitor<T>) = visitor.visit(this)

    override fun normalize(): Expression {
        return TernaryExpression(
            fst.normalize(),
            snd.normalize(),
            thrd.normalize(),
            str
        )
    }

    override fun toCode() = "${fst.toCode()} ? ${snd.toCode()} : ${thrd.toCode()}"

    override fun toValue() = str

}