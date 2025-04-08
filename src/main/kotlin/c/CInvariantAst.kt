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

    override fun visitSqbracket(ctx: InvariantCParser.SqbracketContext) =
        UnaryExpression("[]", visit(ctx.expression()), ctx.text)

    override fun visitDotarrow(ctx: InvariantCParser.DotarrowContext): Expression {
        return UnaryExpression(ctx.op.text, Var(ctx.Identifier().text), ctx.text)
    }

    override fun visitUnary(ctx: InvariantCParser.UnaryContext) =
        UnaryExpression(ctx.op.text, visit(ctx.castExpression()), ctx.text)

    override fun visitUnarybase(ctx: InvariantCParser.UnarybaseContext) =
        visit(ctx.primaryExpression())

    override fun visitPostfix(ctx: InvariantCParser.PostfixContext): Expression {
        if (ctx.postfixExpression() == null || ctx.postfixExpression().isEmpty())
            return visit(ctx.primaryExpression())
        // TODO: how to split the text??
        var node = PostfixExpression(visit(ctx.primaryExpression()), visit(ctx.postfixExpression().first()), ctx.text)
        for (exp in ctx.postfixExpression().drop(1)) {
            node = PostfixExpression(node, visit(exp), ctx.text)
        }
        return node
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