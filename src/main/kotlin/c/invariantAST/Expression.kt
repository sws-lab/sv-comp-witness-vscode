package c.invariantAST

sealed class Expression : Node() {
    abstract fun normalize(): Expression
    abstract fun toCode(): String
    abstract fun toValue(): String
}