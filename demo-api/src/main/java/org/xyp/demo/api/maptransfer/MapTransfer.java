package org.xyp.demo.api.maptransfer;

import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;

public class MapTransfer {

    private static final String EL_PREFIX = "#{";

    private static final String PLACEHOLDER_PREFIX = "${";

    private static final String EXP_SUFFIX = "}";
    private static final String PRESERVE_PREFIX = "\\";

    public String parseExpression(Object context, String input) {

        PositionHolder pos = new PositionHolder(0);
        StringBuilder sb = new StringBuilder();
        while (pos.position < input.length()) {
            findExpressionInLoop(context, input, pos, sb);
        }
        return sb.toString();
    }

    private Object parsePlaceHolder(String input, PositionHolder pos
            , Object context) {

        StringBuilder sb = findExpressionForElAndPlaceholder(input, pos, context);

        return PropertyUtil.getProperty(context, sb.toString());
    }

    private Object parseElExpression(String input, PositionHolder pos
            , Object context) {


        StringBuilder sb = findExpressionForElAndPlaceholder(input, pos, context);

        String expressionString = sb.toString();
        SpelExpressionParser parser = new SpelExpressionParser();
        Expression expression = parser.parseExpression(expressionString);

        return expression.getValue(context, Object.class);

    }

    private StringBuilder findExpressionForElAndPlaceholder(
            String input, PositionHolder pos, Object context) {
        final int begin = pos.position;
        pos.position += 2;
        StringBuilder sb = new StringBuilder();
        boolean closed = false;

        while (pos.position < input.length()) {
            if (input.startsWith(EXP_SUFFIX, pos.position)) {
                pos.position++;
                closed = true;
                break; // el finished
            }

            findExpressionInLoop(context, input, pos, sb);
        }

        if (!closed) {
            throw new IllegalStateException("el expression [" + input.substring(begin)
                    + "] is not closed");
        }
        return sb;
    }

    private void findExpressionInLoop(Object context, String input
            , PositionHolder pos, StringBuilder sb) {
        if (isPlaceholderBegin(input, pos.position)) {
            // place holder
            sb.append(parsePlaceHolder(input, pos, context));
        } else if (isElBegin(input, pos.position)) {
            // embedded el
            sb.append(parseElExpression(input, pos, context));
        } else if (isQuoteBegin(input, pos.position)) {
            // quote
            sb.append(parseQuote(input, pos));
        } else if (isPreservedBegin(input, pos.position)) {
            // preserved
            sb.append(parsePreserved(input, pos));
        } else {
            sb.append(input.substring(pos.position, pos.position + 1));
            pos.position++;
        }
    }

    private String parseQuote(String input, PositionHolder pos) {
        pos.position += 1;
        StringBuilder sb = new StringBuilder();

        while (pos.position < input.length()) {
            if (isQuoteBegin(input, pos.position)) {
                pos.position++;
                break;
            }
            if (isPreservedBegin(input, pos.position)) {
                // preserved
                sb.append(parsePreserved(input, pos));
            } else {
                sb.append(input.substring(pos.position, pos.position + 1));
                pos.position++;
            }
        }
        return sb.toString();
    }

    private String parsePreserved(String input, PositionHolder pos) {
        final int bg = pos.position + 1;
        pos.position = bg + 1;

        return input.substring(bg, pos.position + 1);
    }

    private boolean isElBegin(String input, int pos) {
        int inputLen = input.length();
        return pos < inputLen - 2
                && input.startsWith(EL_PREFIX, pos);
    }

    private boolean isPlaceholderBegin(String input, int pos) {
        int inputLen = input.length();
        return pos < inputLen - 2
                && input.startsWith(PLACEHOLDER_PREFIX, pos);
    }

    private boolean isQuoteBegin(String input, int pos) {
        int inputLen = input.length();
        return pos < inputLen - 2
                && input.startsWith(PLACEHOLDER_PREFIX, pos);
    }

    private boolean isPreservedBegin(String input, int pos) {
        int inputLen = input.length();
        return pos < inputLen - 1
                && input.startsWith(PRESERVE_PREFIX, pos);
    }

    private static class PositionHolder {
        int position;

        PositionHolder(int position) {
            this.position = position;
        }

    }

}
