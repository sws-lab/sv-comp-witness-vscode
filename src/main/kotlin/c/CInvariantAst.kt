package c

import InvariantCBaseVisitor
import InvariantCLexer
import InvariantCParser
import c.invariantAST.Const
import c.invariantAST.Node
import c.invariantAST.Statement
import c.invariantAST.Var
import org.antlr.v4.runtime.BailErrorStrategy
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream

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
        //println(tree.toStringTree(parser))
        return StatementVisitor().visit(tree)
    }

}

private class StatementVisitor : InvariantCBaseVisitor<Statement>() {
    override fun visitInvariant(ctx: InvariantCParser.InvariantContext): Statement =
        visitStatements(ctx.statements())

    override fun visitStatements(ctx: InvariantCParser.StatementsContext): Statement =
        visit(ctx.statement())

    override fun visitIdent(ctx: InvariantCParser.IdentContext): Statement =
        Var(ctx.Identifier().text)

    override fun visitCons(ctx: InvariantCParser.ConsContext): Statement =
        Const(ctx.Constant().text)

}