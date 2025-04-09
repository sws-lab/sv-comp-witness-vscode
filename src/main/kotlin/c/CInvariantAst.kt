package c

import InvariantCBaseVisitor
import InvariantCLexer
import InvariantCParser
import c.invariantAST.*
import org.antlr.v4.runtime.*

object CInvariantAst {
    fun createAst(program: String): Node {
        val lexer = InvariantCLexer(CharStreams.fromString(program)).apply {
            removeErrorListeners()
            //addErrorListener(ExceptionErrorListener())
        }

        val parser = InvariantCParser(CommonTokenStream(lexer)).apply {
            removeErrorListeners()
            errorHandler = BailErrorStrategy()
        }

        val tree = parser.invariant()
        return ExpressionVisitor().visit(tree)
    }

}

private class ExpressionVisitor : InvariantCBaseVisitor<Expression>() {
    override fun visitInvariant(ctx: InvariantCParser.InvariantContext) =
        visitExpression(ctx.expression())

    override fun visitExpression(ctx: InvariantCParser.ExpressionContext) =
        visitConditionalExpression(ctx.conditionalExpression())

    override fun visitPostfixExpression(ctx: InvariantCParser.PostfixExpressionContext): Expression {
        var node = visit(ctx.primaryExpression())
        for (exp in ctx.postfixSecondExpression()) {
            when (exp) {
                is InvariantCParser.SqBracketContext ->
                    node = BinaryExpression(node, "[]", visit(exp.expression()), ctx.text)

                is InvariantCParser.DotArrowContext ->
                    node = BinaryExpression(node, exp.op.text, Var(exp.Identifier().text), ctx.text)
            }
        }
        return node
    }

    override fun visitUnaryExpression(ctx: InvariantCParser.UnaryExpressionContext): Expression {
        // TODO: chatGPT generated code: do better
        // Start by evaluating the "core" of the unary expression
        val baseExpr: Expression = when {
            ctx.postfixExpression() != null -> {
                visit(ctx.postfixExpression())
            }

            ctx.castExpression() != null -> {
                val unaryOp = ctx.unaryOp.text
                val expr = visit(ctx.castExpression())
                UnaryExpression(unaryOp, expr, ctx.text)
            }

            ctx.typeName() != null -> {
                Type(ctx.text)
            }

            ctx.Identifier() != null -> {
                val label = ctx.Identifier().text
                UnaryExpression("&&", Var(label), ctx.text)
            }

            else -> throw RuntimeException("Unexpected unary expression structure: ${ctx.text}")
        }

        // Apply any prefix operators in reverse (closest to base applies first)
        return ctx.op.foldRight(baseExpr) { opToken, acc ->
            val op = opToken.text
            // TODO: concatenate for str
            UnaryExpression(op, acc, op)
        }
    }

    override fun visitCast(ctx: InvariantCParser.CastContext) =
        UnaryExpression(ctx.typeName().text, visit(ctx.castExpression()), ctx.text)

    override fun visitCastbase(ctx: InvariantCParser.CastbaseContext) =
        visit(ctx.unaryExpression())

    override fun visitMultiplicativeExpression(ctx: InvariantCParser.MultiplicativeExpressionContext) =
        visitBinary(ctx.castExpression(), ctx.op, ctx.text)

    override fun visitAdditiveExpression(ctx: InvariantCParser.AdditiveExpressionContext) =
        visitBinary(ctx.multiplicativeExpression(), ctx.op, ctx.text)

    override fun visitShiftExpression(ctx: InvariantCParser.ShiftExpressionContext) =
        visitBinary(ctx.additiveExpression(), ctx.op, ctx.text)

    override fun visitRelationalExpression(ctx: InvariantCParser.RelationalExpressionContext) =
        visitBinary(ctx.shiftExpression(), ctx.op, ctx.text)

    override fun visitEqualityExpression(ctx: InvariantCParser.EqualityExpressionContext) =
        visitBinary(ctx.relationalExpression(), ctx.op, ctx.text)

    override fun visitAndExpression(ctx: InvariantCParser.AndExpressionContext) =
        visitBinary(ctx.equalityExpression(), ctx.op, ctx.text)

    override fun visitExclusiveOrExpression(ctx: InvariantCParser.ExclusiveOrExpressionContext) =
        visitBinary(ctx.andExpression(), ctx.op, ctx.text)

    override fun visitInclusiveOrExpression(ctx: InvariantCParser.InclusiveOrExpressionContext) =
        visitBinary(ctx.exclusiveOrExpression(), ctx.op, ctx.text)

    override fun visitLogicalAndExpression(ctx: InvariantCParser.LogicalAndExpressionContext) =
        visitBinary(ctx.inclusiveOrExpression(), ctx.op, ctx.text)

    override fun visitLogicalOrExpression(ctx: InvariantCParser.LogicalOrExpressionContext) =
        visitBinary(ctx.logicalAndExpression(), ctx.op, ctx.text)

    override fun visitConditionalExpression(ctx: InvariantCParser.ConditionalExpressionContext): Expression {
        val cond = visitLogicalOrExpression(ctx.logicalOrExpression())
        if (ctx.t_exp == null || ctx.f_exp == null)
            return cond
        val trueBranch = visit(ctx.t_exp)
        val falseBranch = visit(ctx.f_exp)
        return TernaryExpression(cond, trueBranch, falseBranch, ctx.text)
    }

    override fun visitIdent(ctx: InvariantCParser.IdentContext) =
        Var(ctx.Identifier().text)

    override fun visitCons(ctx: InvariantCParser.ConsContext) =
        Const(ctx.Constant().text)

    override fun visitString(ctx: InvariantCParser.StringContext) =
        Const(ctx.text)

    override fun visitParens(ctx: InvariantCParser.ParensContext): Expression =
        visit(ctx.expression())

    override fun visitSpecifierQualifierList(ctx: InvariantCParser.SpecifierQualifierListContext) =
        TODO("irrelevant")

    override fun visitTypeName(ctx: InvariantCParser.TypeNameContext) =
        TODO("irrelevant")

    override fun visitPointer(ctx: InvariantCParser.PointerContext): Expression {
        TODO("irrelevant")
    }

    override fun visitTypeQualifierList(ctx: InvariantCParser.TypeQualifierListContext): Expression {
        TODO("irrelevant")
    }

    override fun visitAbstractDeclarator(ctx: InvariantCParser.AbstractDeclaratorContext): Expression {
        TODO("irrelevant")
    }

    private fun visitBinary(ctx: List<ParserRuleContext>, ops: List<Token>, str: String): Expression {
        var node = visit(ctx.first())
        for ((op, exp) in ops.zip(ctx.drop(1))) {
            node = BinaryExpression(node, op.text, visit(exp), str)
        }
        return node
    }
}