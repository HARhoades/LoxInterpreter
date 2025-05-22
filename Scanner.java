package LoxInterpreter;

import static LoxInterpreter.TokenType.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0; // First character in current lexeme
    private int current = 0; // Current character in current lexeme
    private int line = 1; // "Location" of the token being parsed
    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and", AND);
        keywords.put("class", CLASS);
        keywords.put("else", ELSE);
        keywords.put("false", FALSE);
        keywords.put("for", FOR);
        keywords.put("fun", FUN);
        keywords.put("if", IF);
        keywords.put("nil", NIL);
        keywords.put("or", OR);
        keywords.put("print", PRINT);
        keywords.put("return", RETURN);
        keywords.put("super", SUPER);
        keywords.put("this", THIS);
        keywords.put("true", TRUE);
        keywords.put("var", VAR);
        keywords.put("while", WHILE);
    }

    Scanner(String source) {
        this.source = source;
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

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
        if (isAtEnd()) {
            return false;
        }
        if (source.charAt(current) != expected) {
            return false;
        }
        current++;
        return true;
    }

    private char peek() {
        if (isAtEnd()) {
            return '\0';
        }
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) {
            return '\0';
        }
        return source.charAt(current + 1);
    }

    private void string() {
        while ((peek() != '"') && (isAtEnd() == false)) {
            if (peek() == '\n') {
                line++;
                advance();
            }
            if (isAtEnd()) {
                Lox.error(line, "Unterminated string.");
                return;
            }
            advance(); // Gets closing "
            String value = source.substring(start + 1, current - 1); // Extracts string w/o " "
            addToken(STRING, value);
        }
    }

    private boolean isDigit(char c) {
        return ((c >= '0') && (c <= '9'));
    }

    private void number() {
        while (isDigit(peek())) advance(); // Consume all contiguous digits

        if ((peek() == '.') && (isDigit(peekNext()))) {
            advance(); // Consume the .
            while (isDigit(peek())) advance(); // Continue consuming digits
        }

        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
        return;
    }

    private boolean isAlpha(char c) {
        return ((c >= 'a') && (c <= 'z')) ||
            ((c >= 'A') && (c <= 'Z')) ||
            c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();

        addToken(IDENTIFIER);
        return;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            // Single character tokens without possible multi-char modifiers
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

            // Possible multi-char tokens
            case '!':
                addToken(match('=') ? BANG_EQUAL : BANG);
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                addToken(match('=') ? LESS_EQUAL : LESS);
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;

            case '/':
                if (match('/')) {
                    // Case of comment, skip to end of line
                    while ((peek() != '\n') && (isAtEnd() == false)){
                        advance();
                    }
                }
                else {
                    addToken(SLASH);
                }
                break;
            
            // White-space characters
            case ' ': break;
            case '\r': break;
            case '\t': break;
            case '\n':
                line++;
                break;

            // Literals
            case '"':
                string();
                break;

            default:
                if (isDigit(c)) {
                    number();
                }
                else if (isAlpha(c)) {
                    identifier();
                }
                else {
                    Lox.error(line, "Unexpected character.");
                }
                break;
        }
    }
}
