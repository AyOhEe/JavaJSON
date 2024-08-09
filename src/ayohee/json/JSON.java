package ayohee.json;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.io.*;

//obeys https://www.crockford.com/mckeeman.html
public final class JSON {
    public static Object Parse(Path datapath, Charset encoding) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(datapath, encoding)) {
            return Parse(reader);
        }
    }
    public static Object Parse(Path datapath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(datapath.toFile()), StandardCharsets.UTF_8))) {
            return Parse(reader);
        }
    }
    public static Object Parse(String data) throws IOException {
        return Parse(new StringReader(data));
    }
    public static Object Parse(Reader reader) throws IOException {
        return JSONParser.ParseReader(reader);
    }



    public static String Encode(Object data) {
        return JSONEncoder.ParseObject(data);
    }
    public static String Encode(Object data, boolean pretty) {
        return JSONEncoder.Prettify(JSONEncoder.ParseObject(data));
    }


    //this class only contains static helper methods.
    //this protection exists only in case of the constructor being called via reflection.
    private JSON() {
        throw new UnsupportedOperationException("ayohee.json.JSON cannot be instantiated");
    }
}
