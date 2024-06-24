package ayohee.json;

import java.nio.charset.Charset;
import java.nio.file.*;
import java.io.*;

public final class JSON {
    public static Object Parse(Path datapath, Charset encoding) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(datapath, encoding)) {
            return Parse(reader);
        }
    }
    public static Object Parse(Path datapath) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(datapath, Charset.defaultCharset())) {
            return Parse(reader);
        }
    }
    public static Object Parse(String data) throws IOException {
        return Parse(new StringReader(data));
    }

    public static Object Parse(Reader reader) throws IOException {
        return JSONParser.ParseReader(reader);
    }


    //this class only contains static helper methods.
    //this protection exists only in case of the constructor being called via reflection.
    private JSON() {
        throw new UnsupportedOperationException("ayohee.json.JSON cannot be instantiated");
    }
}
