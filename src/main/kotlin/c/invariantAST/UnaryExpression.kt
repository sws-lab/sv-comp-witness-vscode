package c.invariantAST

data class UnaryExpression(val op: String, val exp: Expression) : Expression() {
    override fun <T> accept(visitor: InvariantAstVisitor<T>) = visitor.visit(this)
}