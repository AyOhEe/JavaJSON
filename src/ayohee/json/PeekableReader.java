package ayohee.json;

import java.io.IOException;
import java.io.Reader;

class PeekableReader {
    int last;
    int current;
    int next;
    Reader reader;

    public PeekableReader(Reader reader) throws IOException {
        this.reader = reader;

        last = -1;
        current = -1;
        next = reader.read();
    }

    public int read() throws IOException {
        last = current;
        current = next;
        next = reader.read();

        return current;
    }

    public int last() {
        return last;
    }

    public int current() {
        return current;
    }

    public int peek() {
        return next;
    }
}