package c.invariantAST

data class UnaryExpression(val op: String, val exp: Expression) : Expression()