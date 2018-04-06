import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class RsaSign {
    public static void main(String[] args) {
        try {
            Path path = Paths.get(args[1]);
            byte[] input = Files.readAllBytes(path);
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(input);
            byte[] digest = md.digest();

        } catch (NoSuchAlgorithmException e) {
            System.out.println("Algorithm not found");
        } catch (IOException e) {
            System.out.println("Error reading input file");
        }

        if (args[0].equalsIgnoreCase("s")) {

        }
        else if (args[0].equalsIgnoreCase("v")) {

        }
    }
}
