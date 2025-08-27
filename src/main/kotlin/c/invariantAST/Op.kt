package c.invariantAST

sealed class Op : Expression() {
    abstract val name: String
    override fun <T> accept(visitor: InvariantAstVisitor<T>) = visitor.visit(this)
    override fun normalize() = this
    override fun toCode() = name
    override fun toValue() = name
    override fun toString() = name
    override fun nodeOpStr() = name
}
