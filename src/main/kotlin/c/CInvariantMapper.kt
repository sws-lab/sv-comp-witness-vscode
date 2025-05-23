package c

import c.invariantAST.*

fun collectConjunctAsts(invariantASt: Expression): Set<Expression> = object : InvariantAstVisitor<Set<Expression>>() {

    override fun visit(variable: Var): Set<Expression> = setOf(variable)

    override fun visit(constant: Const): Set<Expression> = setOf(constant)

    override fun visit(binop: BinaryExpression): Set<Expression> =
        if (binop.op == "&&") visit(binop.left) + visit(binop.right) else setOf(binop)

    override fun visit(unop: UnaryExpression): Set<Expression> = setOf(unop)

    override fun visit(ternop: TernaryExpression): Set<Expression> = setOf(ternop)

}.visit(invariantASt)

