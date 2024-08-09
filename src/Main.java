import ayohee.json.JSON;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length == 0){
            System.out.print("No arguments provided. Please provide a file path.");
            System.exit(3);
        }

        try{
            JSON.Parse(Paths.get(args[0]));
            System.exit(0);
        }
        catch (IllegalStateException | EOFException e) {
            System.out.print("Unexpected end to file or invalid file.");
            e.printStackTrace();
            System.exit(1);
        }
        catch (FileNotFoundException e){
            System.out.print("File does not exist.");
            System.exit(2);
        }
        catch (IOException e){
            e.printStackTrace();
            System.exit(-1);
        }
    }
}