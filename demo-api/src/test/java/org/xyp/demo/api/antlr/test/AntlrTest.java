package org.xyp.demo.api.antlr.test;

//import org.antlr.v4.runtime.CharStreams;
//import org.antlr.v4.runtime.CommonTokenStream;
//import org.antlr.v4.runtime.tree.ParseTree;
//import org.junit.jupiter.api.Test;
//import org.xyp.demo.api.antlr4.MathGrammerLexer;
//import org.xyp.demo.api.antlr4.MathGrammerParser;

import org.junit.jupiter.api.Test;

import java.io.IOException;

class AntlrTest {
    @Test
    void test1() throws IOException {

//        MathGrammerLexer lexer = new MathGrammerLexer(CharStreams.fromString("(3+9+2-(1+7))"));
//        CommonTokenStream tokens = new CommonTokenStream(lexer);
//        MathGrammerParser parser = new MathGrammerParser(tokens);
//        MathGrammerVisitor visitor = new MathGrammerBaseVisitor();
//        visitor.visitExpression(parser.expression());
//
//
//        ParseTree tree = parser.expression();
//
//        // Print a text-based representation of the parse tree
//        System.out.println(tree.toStringTree(parser));

        // Draw a graphical parse tree (requires Graphviz)
        // You can use an online tool like WebGraphviz (http://www.webgraphviz.com/)
//        TreeViewer view = new TreeViewer(Arrays.asList(parser.getRuleNames()), tree);
//        view.open();

    }

//    class MyVisitor<T> extends AbstractParseTreeVisitor<T> implements MathGrammerVisitor<T> {
//
//
//        /**
//         * {@inheritDoc}
//         *
//         * <p>The default implementation returns the result of calling
//         * {@link #visitChildren} on {@code ctx}.</p>
//         */
//        @Override
//        public T visitExpression(MathGrammerParser.ExpressionContext ctx) {
//            System.out.println("visitExpression " + ctx.getText() + " " + ctx.getChildCount());
//            return visitChildren(ctx);
//        }
//
//        /**
//         * {@inheritDoc}
//         *
//         * <p>The default implementation returns the result of calling
//         * {@link #visitChildren} on {@code ctx}.</p>
//         */
//        @Override
//        public T visitAdditiveExpression(MathGrammerParser.AdditiveExpressionContext ctx) {
//            System.out.println("visitAdditiveExpression " + ctx.getText() + " " + ctx.getChildCount());
//
//            return visitChildren(ctx);
//        }
//
//        /**
//         * {@inheritDoc}
//         *
//         * <p>The default implementation returns the result of calling
//         * {@link #visitChildren} on {@code ctx}.</p>
//         */
//        @Override
//        public T visitMultiplicativeExpression(MathGrammerParser.MultiplicativeExpressionContext ctx) {
//            System.out.println("visitMultiplicativeExpression " + ctx.getText() + " " + ctx.getChildCount());
//            return visitChildren(ctx);
//        }
//
//        /**
//         * {@inheritDoc}
//         *
//         * <p>The default implementation returns the result of calling
//         * {@link #visitChildren} on {@code ctx}.</p>
//         */
//        @Override
//        public T visitPrimaryExpression(MathGrammerParser.PrimaryExpressionContext ctx) {
//            System.out.println("visitPrimaryExpression " + ctx.getText() + " " + ctx.getChildCount());
//            return visitChildren(ctx);
//        }
//    }

}
