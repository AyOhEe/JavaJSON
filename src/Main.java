import ayohee.json.JSON;

import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws Exception{
        System.out.println(JSON.Parse(Path.of("test files", "1.json")).toString());
        System.out.println(JSON.Parse(Path.of("test files", "2.json")).toString());
    }
}