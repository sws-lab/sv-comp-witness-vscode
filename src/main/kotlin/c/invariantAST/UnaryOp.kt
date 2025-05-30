package c.invariantAST

sealed class UnaryOp : Expression() {
    abstract val name: String
    override fun <T> accept(visitor: InvariantAstVisitor<T>) = visitor.visit(this)
    override fun normalize() = this
    override fun toCode() = name
    override fun toValue() = name
}
