package c.invariantAST

data class BinaryExpression(val left: Expression, val op: String, val right: Expression) : Expression()