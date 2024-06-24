package ayohee.json;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;

public class JSONParser {
    static Object ParseReader(Reader reader) throws IOException {
        return new JSONParser(new ConsumingReader(reader)).ParseJSON();
    }


    private final ConsumingReader reader;
    private Object result = null;

    private JSONParser(ConsumingReader reader){
        this.reader = reader;
    }
    private Object ParseJSON() throws IOException {
        if (result != null){
            return result;
        } else {
            SkipWhitespace();
            result = ParseElement();
            return result;
        }
    }


    //NOTE: each "ParseXXX()" method expects reader.current() to be the first character
    //      of the element, and should consume their last character

    private AbstractMap.SimpleEntry<String, Object> ParseMember() throws IOException {
        AssertNotEOF();
        AssertCurrentCharacterEquals('"');
        String key = ParseStringLiteral();

        SkipWhitespace();
        AssertNotEOF();
        AssertCurrentCharacterEquals(':');
        reader.consume();
        SkipWhitespace();

        Object value = ParseElement();
        return new AbstractMap.SimpleEntry<>(key, value);
    }
    private Object ParseElement() throws IOException {
        AssertNotEOF();
        int c = reader.getCurrent();

        try {
            return switch ((char) c) {
                case '{' -> ParseObject();
                case '[' -> ParseArray();
                case '\"' -> ParseStringLiteral();
                case 't' -> ParseExactLiteral("true", true);
                case 'f' -> ParseExactLiteral("false", false);
                case 'n' -> ParseExactLiteral("null", null);
                default -> ParseNumberLiteral();
            };
        }
        catch (Exception e) {
            throw new IllegalStateException("JSON parsing ended after failing to parse an element", e);
        }
    }

    private HashMap<String, Object> ParseObject() throws IOException {
        AssertNotEOF();
        AssertCurrentCharacterEquals('{');
        reader.consume();


        HashMap<String, Object> hashmap = new HashMap<>();
        while ((char)reader.getCurrent() != '}') {
            SkipWhitespace();
            AbstractMap.SimpleEntry<String, Object> member = ParseMember();
            hashmap.put(member.getKey(), member.getValue());

            //potential early exit. otherwise, enforce use of commas to separate members
            SkipWhitespace();
            if((char)reader.getCurrent() == '}'){
                break;
            }
            AssertCurrentCharacterEquals(',');
            reader.consume();
        }


        AssertNotEOF();
        AssertCurrentCharacterEquals('}');
        reader.consume();
        return hashmap;
    }
    private ArrayList<Object> ParseArray() throws IOException {
        AssertNotEOF();
        AssertCurrentCharacterEquals('[');
        reader.consume();


        ArrayList<Object> array = new ArrayList<>();
        while ((char)reader.getCurrent() != ']') {
            SkipWhitespace();
            Object element = ParseElement();
            array.add(element);

            //potential early exit. otherwise, enforce use of commas to separate elements
            SkipWhitespace();
            if((char)reader.getCurrent() == ']'){
                break;
            }
            AssertCurrentCharacterEquals(',');
            reader.consume();
        }


        AssertNotEOF();
        AssertCurrentCharacterEquals(']');
        reader.consume();
        return array;
    }

    private String ParseStringLiteral() throws IOException {
        AssertNotEOF();
        AssertCurrentCharacterEquals('"');
        reader.consume();

        StringBuilder sb = new StringBuilder();
        while((char)reader.getCurrent() != '"'){
            sb.append(ParseCharacter());
        }

        AssertNotEOF();
        AssertCurrentCharacterEquals('"');
        reader.consume();
        return sb.toString();
    }
    private char ParseCharacter() throws IOException {
        AssertNotEOF();
        char c = (char)reader.getCurrent();
        reader.consume();

        if (c == '\\'){
            return ParseEscapedCharacter();
        }
        else{
            return c;
        }
    }
    private char ParseEscapedCharacter() throws IOException {
        char c = (char)reader.getCurrent();
        reader.consume();

        return switch (c) {
            case '\\' -> '\\';
            case '\"' -> '\"';
            case 'b' -> '\b';
            case 'f' -> '\f';
            case 'n' -> '\n';
            case 'r' -> '\r';
            case 't' -> '\t';
            case 'u' -> ParseEscapedUnicodeCharacter();
            default -> throw new IllegalStateException("JSON ended abruptly after encountering invalid escaped character: \\" + (char)reader.getCurrent());
        };
    }
    private char ParseEscapedUnicodeCharacter() throws IOException {
        String sequence = ReadChars(4);

        try {
            //TODO check when this returns more than one character
            return Character.toString((Integer.parseInt(sequence, 16))).charAt(0);
        } catch (IllegalArgumentException e){
            throw new IllegalStateException("JSON contained invalid escaped unicode character", e);
        }
    }

    private Object ParseExactLiteral(String literal, Object value) throws IOException {
        for (char c : literal.toCharArray()){
            //the conditional will fail on EOF, but this gives better error messages
            AssertNotEOF();
            if ((char)reader.getCurrent() != c){
                throw new IllegalStateException("JSON parsing ended after incorrect match for exact literal: expected " + literal);
            }

            reader.consume();
        }

        return value;
    }
    private Number ParseNumberLiteral() throws IOException{
        StringBuilder sb = new StringBuilder();
        char c = (char)reader.getCurrent();
        while(c != ',' && c != ']' && c != '}'){
            AssertNotEOF();
            sb.append(c);

            reader.consume();
            c = (char)reader.getCurrent();
        }

        try {
            //TODO JSON specific formatter, but this will do for now
            //toUpperCase is used as the default instance doesn't like 1.23e2, but takes 1.23E2
            return NumberFormat.getInstance().parse(sb.toString().toUpperCase());
        } catch (ParseException e){
            throw new IllegalStateException("JSON ended abruptly while parsing invalid literal", e);
        }
    }


    //utility methods
    private void SkipWhitespace() throws IOException {
        while(Character.isWhitespace((char)reader.getCurrent())) {
            reader.consume();
        }
    }

    private void AssertCurrentCharacterEquals(char expected) {
        AssertCurrentCharacterEquals(expected, "JSON parsing ended after encountering unexpected character");
    }
    private void AssertCurrentCharacterEquals(char expected, String message) {
        if ((char)reader.getCurrent() != expected){
            throw new IllegalStateException(message);
        }
    }

    private void AssertNotEOF() throws IOException {
        AssertNotEOF("JSON parsing ended after end-of-file reached before clean exit");
    }
    private void AssertNotEOF(String message) throws IOException {
        if(reader.getCurrent() == -1){
            throw new EOFException(message);
        }
    }

    private String ReadChars(int count) throws IOException {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < count; i++){
            AssertNotEOF();
            sb.append((char)reader.getCurrent());
            reader.consume();
        }

        return sb.toString();
    }
}
