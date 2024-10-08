package com.craftinginterpreters.lox;

/**TODO
 * 1. Add documentation
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.craftinginterpreters.lox.TokenType.*;
public class Scanner {

    private final String source;
    private int start; //Beginning of the lexeme
    private int line; //Line at which current is pointing to
    private int current; //Character currently being processed
    private final List<Token> tokens = new ArrayList<>();
    Scanner(String source) {
        this.source = source;
    }
    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and",    AND);
        keywords.put("class",  CLASS);
        keywords.put("else",   ELSE);
        keywords.put("false",  FALSE);
        keywords.put("for",    FOR);
        keywords.put("fun",    FUN);
        keywords.put("if",     IF);
        keywords.put("nil",    NIL);
        keywords.put("or",     OR);
        keywords.put("print",  PRINT);
        keywords.put("return", RETURN);
        keywords.put("super",  SUPER);
        keywords.put("this",   THIS);
        keywords.put("true",   TRUE);
        keywords.put("var",    VAR);
        keywords.put("while",  WHILE);
    }
    public List<Token> scanTokens() {
        while(!isAtEnd()) {
            start = current;
            scanToken(); //Scanning continues even after error
        }
        //Reached the end of code
        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    /**
     * Processes one character at a time
     */
    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ';': addToken(SEMICOLON); break;
            case '*': addToken(STAR); break;
            case '!': addToken(match('=') ? BANG_EQUAL : BANG); break;
            case '=': addToken(match('=') ? EQUAL_EQUAL : EQUAL); break;
            case '<': addToken(match('=') ? LESS_EQUAL : LESS); break;
            case '>': addToken(match('=') ? GREATER_EQUAL : GREATER); break;
            case '/':
                if (match('/')) {
                    while (peek() != '\n' && !isAtEnd()) advance(); //Note that we aren't adding comments to token list
                } else {
                    addToken(SLASH);
                }
                break;
            case ' ':
            case '\r':
            case '\t':
                // Ignore whitespace.
                break;
            case '\n':
                line++;
                break;

                //String handling
            case '"': string(); break;

            default:
                if (isDigit(c)) {
                    number();
                } else if(isAlpha(c)) {
                    identifier();
                } else {
                    Lox.error(line, String.format("Unexpected character '%s' [ASCII: %d]", c, (int)c));
                }
                break;
        }

    }




    //Helper functions

    /**
     *Returns if we have reached the end of source file i.e all characters are processed
     * @return Boolean
     */
    private boolean isAtEnd() {
        return current >= source.length();
    }

    /**
     * Returns the current character and advances current pointer by 1
     * @return character at current position
     */
    private char advance() {
        return source.charAt(current++);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }
    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;

        current++;
        return true;
    }

    private char peek() {
        if(isAtEnd()) return '\0';

        //pointer isn't advanced
        return source.charAt(current);
    }
    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++; //Supporting multiline string.
            advance();
        }
        if(isAtEnd()) {
            Lox.error(line, "Unclosed string.");
            return;
        }

        //Consume the closing quotes
        advance();
        String value = source.substring(start+1, current-1);
        addToken(STRING, value);

    }
    private char peekNext() {
        if (current + 1 >= source.length()) return '\0'; //Last char of file is . -> should be error
        return source.charAt(current + 1);
    }
    private void number() {
        while(isDigit(peek())) advance();
        if (peek() == '.' && isDigit(peekNext())) {
            advance();
            while(isDigit(peek())) advance();
        }
        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }
    private void identifier() {
        while(isAlphaNumeric(peek())) advance();
        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if(type == null) type = IDENTIFIER;
        addToken(type); //No point in storing variable name ig
    }
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }
    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c=='_';
    }
    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }
}
