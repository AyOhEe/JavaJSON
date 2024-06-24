package ayohee.json;

import java.io.IOException;
import java.io.Reader;

class ConsumingReader {
    protected int current;
    protected char currentAsChar;
    protected Reader reader;

    public ConsumingReader(Reader reader) throws IOException {
        this.reader = reader;

        current = reader.read();
        currentAsChar = (char)current;
    }

    //reads a new character and returns the newly read character
    public int consume() throws IOException {
        current = reader.read();
        currentAsChar = (char)current;
        return current;
    }

    //returns the currently stored character
    public int getCurrent(){
        return current;
    }
}