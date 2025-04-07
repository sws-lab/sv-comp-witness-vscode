package c.invariantAST

data class TernaryExpression(val fst: Expression, val snd: Expression, val thrd: Expression, val str: String) :
    Expression() {
    override fun <T> accept(visitor: InvariantAstVisitor<T>) = visitor.visit(this)
}