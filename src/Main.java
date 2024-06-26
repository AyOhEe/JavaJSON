import ayohee.json.JSON;

import java.io.EOFException;
import java.io.IOException;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length == 0){
            System.exit(3);
        }

        try{
            JSON.Parse(Paths.get(args[0]));
            System.exit(0);
        }
        catch (IllegalStateException | EOFException e) {
            System.exit(1);
        }
        catch (IOException e){
            System.exit(2);
        }
    }
}