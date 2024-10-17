package com.craftinginterpreters.lox;

import java.util.List;
import static com.craftinginterpreters.lox.TokenType.*;
public class Parser {
    private static class ParseError extends RuntimeException {}
    private final List<Token> tokens;
    private int current = 0;
    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }
    Expr parse() {
        try {
            return expression();
        } catch(ParseError e) {
            return null;
        }
    }
    private Expr expression() {
        return equality();
    }
    private Expr equality() {
        Expr expr = comparision();
        while(match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparision();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr comparision() {
        Expr expr = term();
        while(match(LESS_EQUAL, GREATER_EQUAL, LESS, GREATER)) {
            Token operator = previous(); //match consumes the current character hence previous
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr term() {
        Expr expr = factor();
        while(match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }
    private Expr factor() {
        Expr expr = unary();
        while(match(SLASH, STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }
    private Expr unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }
        return primary();
    }
    private Expr primary() {
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(NIL)) return new Expr.Literal(null);

        if (match(NUMBER, STRING)) {
            return new Expr.Literal(previous().literal);
        }

        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }
        throw error(peek(), "Expect expression.");
    }
    private Token consume(TokenType type, String message) {
        if(check(type)) return advance();
        throw error(peek(), message);
    }
    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }

    private void synchronize() {
        advance();
        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) return;

            switch (peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }

            advance();
        }
    }

    //Helper functions

    /**
     * Checks if the the current token is of any type mentioned in params
     * @param types
     * @return boolean
     */
    private boolean match(TokenType... types) {
        for(TokenType type: types) {
            if(check(type)) {
                advance(); //Consume the token
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the token at current position without advancing the current pointer
     * @return Token
     */
    private Token peek() {
        return tokens.get(current);
    }

    /**
     * Returns the current token and advances the current pointer by one place
     * @return Token
     */
    private Token advance() {
        if(!isAtEnd()) current++;
        return previous();

    }

    /**
     * Returns the token at current - 1 position
     * @return Token
     */
    private Token previous() {
        return tokens.get(current-1);
    }

    /**
     * Returns true if the current token in the list is of type mentioned in parameter
     * @param  type
     * @return  boolean
     */
    private boolean check(TokenType type) {
        if(isAtEnd()) return false;
        return peek().type == type;
    }

    /**
     * Checks if all the tokens have been read
     * @return boolean
     */
    private boolean isAtEnd() {
        return peek().type == EOF;
    }
}
