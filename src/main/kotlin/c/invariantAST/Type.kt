package c.invariantAST

data class Type(val name: String) : Expression() {
    override fun <T> accept(visitor: InvariantAstVisitor<T>) = visitor.visit(this)
    override fun normalize() = this
    override fun toCode() = name
    override fun toValue() = name
}