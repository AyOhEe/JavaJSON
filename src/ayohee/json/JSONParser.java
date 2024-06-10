package ayohee.json;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.*;
import java.util.function.IntPredicate;

final class JSONParser{
    static Object ParseReader(Reader reader) throws IOException{
        return new JSONParser(new PeekableReader(reader)).ParseJSON();
    }


    private final PeekableReader reader;
    private Object result = null;

    private JSONParser(PeekableReader reader){
        this.reader = reader;
    }
    private Object ParseJSON() throws IOException {
        if (result != null){
            return result;
        } else {
            result = ParseElement();
            return result;
        }
    }

    private AbstractMap.SimpleEntry<String, Object> ParseMember() throws IOException{
        SkipToNonWhitespace();
        if((char)reader.peek() != '\"'){
            throw new IllegalStateException("JSON ended abruptly after encountering illegally formed member");
        }
        String key = ParseStringLiteral();
        SkipWhitespace();

        if((char)reader.read() != ':'){
            throw new IllegalStateException("JSON ended abruptly after encountering illegally formed member");
        }
        //consume the colon
        reader.read();

        Object value = ParseElement();

        return new AbstractMap.SimpleEntry<>(key, value);
    }
    private Object ParseElement() throws IOException {
        SkipWhitespace();
        int c = reader.peek();
        if (c == -1){
            throw new EOFException("JSON ended abruptly while parsing element");
        }

        return switch ((char) c) {
            case '{' -> ParseObject();
            case '[' -> ParseArray();
            case '\"' -> ParseStringLiteral();
            case 't', 'f', 'n' -> ParseBooleanOrNullLiteral();
            default -> ParseNumberLiteral();
        };
    }


    private HashMap<String, Object> ParseObject() throws IOException {
        //consume the opening bracket
        reader.read();

        SkipToNonWhitespace();
        if ((char)reader.peek() == '}'){
            return new HashMap<>();
        }

        HashMap<String, Object> hashmap = new HashMap<>();
        while((char)reader.current() != '}'){
            AbstractMap.SimpleEntry<String, Object> member = ParseMember();
            hashmap.put(member.getKey(), member.getValue());
            SkipWhitespace();

            //ensure object is either comma-delimited or closed
            if((char)reader.peek() == '}'){
                break;
            }
            if((char)reader.current() != ','){
                throw new IllegalStateException("JSON ended while parsing invalid object");
            }

            //move to the next member to be parsed
            reader.read();
        }
        //consume the closing bracket (which is currently in .peek())
        reader.read();
        reader.read();

        return hashmap;
    }
    private ArrayList<Object> ParseArray() throws IOException {
        //consume the opening bracket
        reader.read();

        SkipToNonWhitespace();
        if ((char)reader.peek() == ']'){
            return new ArrayList<>();
        }

        ArrayList<Object> array = new ArrayList<>();
        while((char)reader.current() != ']'){
            array.add(ParseElement());
            SkipWhitespace();

            //ensure array is either comma-delimited or closed
            if((char)reader.current() == ']'){
                break;
            }
            if((char)reader.current() != ','){
                throw new IllegalStateException("JSON ended while parsing invalid array");
            }

            //move to the next element to be parsed
            reader.read();
        }
        //consume the closing bracket
        reader.read();

        return array;
    }


    private Object ParseBooleanOrNullLiteral() throws IOException {
        String sequence = ReadChars(4);
        if (sequence == null){
            throw new EOFException("JSON ended abruptly while parsing literal value");
        }

        switch (sequence) {
            case "true":
                //consume the last letter
                reader.read();
                return true;
            case "null":
                //consume the last letter
                reader.read();
                return null;
            case "fals":
                int c = reader.read();
                if (c == -1) {
                    throw new EOFException("JSON ended abruptly while parsing literal value");
                }

                if ((char)c == 'e') {
                    //consume the last letter
                    reader.read();
                    return false;
                }
        }

        throw new IllegalStateException("JSON contained invalid literal \"" + sequence + "\"");
    }
    private Number ParseNumberLiteral() throws IOException {
        //the first character of the literal is in reader.peek(), so skip
        //the contents of .current()
        reader.read();

        String sequence = ReadUntil((raw) -> {
            char c = (char)raw;
            return raw == -1 || Character.isWhitespace(c) || (c == ',' || c == ']' || c == '}');
        });
        try {
            //TODO JSON specific formatter, but this will do for now
            //toUpperCase is used as the default instance doesn't like 1.23e2, but takes 1.23E2
            return NumberFormat.getInstance().parse(sequence.toUpperCase());
        } catch (ParseException e){
            throw new IllegalStateException("JSON ended abruptly while parsing invalid literal", e);
        }
    }
    private String ParseStringLiteral() throws IOException {
        //consume the opening quote
        reader.read();
        StringBuilder sb = new StringBuilder();

        //store the contents into the stringbuilder, observing escaped characters
        char c = (char)reader.read();
        while((char)reader.current() != '"' && reader.current() != -1){
            if(c == '\\'){
                sb.append(ParseEscapedCharacter());
            } else{
                sb.append(c);
            }

            c = (char)reader.read();
        }

        if (reader.current() == -1){
            throw new EOFException("JSON ended abruptly while parsing string literal");
        }
        //consume the closing quote
        reader.read();

        return sb.toString();
    }
    private String ParseEscapedCharacter() throws IOException {
        int c = reader.read();

        if (c == -1){
            throw new EOFException("JSON ended abruptly while parsing escaped character");
        }

        return switch ((char)c) {
            case '\\' -> "\\";
            case '\"' -> "\"";
            case 'b' -> "\b";
            case 'f' -> "\f";
            case 'n' -> "\n";
            case 'r' -> "\r";
            case 't' -> "\t";
            case 'u' -> ParseEscapedUnicodeCharacter();
            default -> throw new IllegalStateException("JSON ended abruptly after encountering invalid escaped character");
        };
    }
    private String ParseEscapedUnicodeCharacter() throws IOException {
        String sequence = ReadChars(4);
        if (sequence == null){
            throw new EOFException("JSON ended abruptly while parsing escaped unicode character");
        }

        try {
            return Character.toString((Integer.parseInt(sequence, 16)));
        } catch (IllegalArgumentException e){
            throw new IllegalStateException("JSON contained invalid escaped unicode character", e);
        }
    }


    private void SkipWhitespace() throws IOException {
        if(!Character.isWhitespace((char)reader.current())) {
            return;
        }
        SkipToNonWhitespace();
    }
    private void SkipToNonWhitespace() throws IOException {
        while(Character.isWhitespace((char)reader.peek())){
            reader.read();
        }
    }
    private String ReadUntil(IntPredicate test) throws IOException {
        StringBuilder sb = new StringBuilder();
        int raw = reader.current();
        while(!test.test(raw)){
            sb.append((char)raw);
            raw = reader.read();
        }

        return sb.toString();
    }
    private String ReadChars(int count) throws IOException {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < count; i++){
            int c = reader.read();;
            if (c == -1){
                return null;
            }

            sb.append((char)c);
        }

        return sb.toString();
    }
}
