package c.invariantAST

data class BinaryExpression(val left: Expression, val op: String, val right: Expression, val str: String) : Expression() {
    override fun <T> accept(visitor: InvariantAstVisitor<T>) = visitor.visit(this)
}