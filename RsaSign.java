import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class RsaSign {
    public static void main(String[] args) {

        byte[] digest;
        LargeInteger c;

        try {
            Path path = Paths.get(args[1]);
            byte[] input = Files.readAllBytes(path);
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(input);
            digest = md.digest(); //hash of file
            c = new LargeInteger(digest); //largeinteger hash c

        } catch (NoSuchAlgorithmException e) {
            System.out.println("Algorithm not found");
            return;
        } catch (IOException e) {
            System.out.println("Error reading input file");
            return;
        }


        if (args[0].equalsIgnoreCase("s")) {
            try {
                FileInputStream privkey = new FileInputStream("privkey.rsa");

                byte[] byteArrayD = new byte[65];
                byte[] byteArrayN = new byte[65];
                privkey.read(byteArrayD, 1, 64);
                privkey.read(byteArrayN, 1, 64);
                LargeInteger d = new LargeInteger(byteArrayD);
                LargeInteger n = new LargeInteger(byteArrayN);
                privkey.close();

                FileOutputStream sig = new FileOutputStream(args[1] + ".sig");
                sig.write(c.modularExp(d, n).getVal()); //write m
                sig.close();
            } catch (FileNotFoundException e) {
                System.out.println("privkey.rsa not found");
            } catch (IOException e) {
                System.out.println("error reading file");
            }


        }
        else if (args[0].equalsIgnoreCase("v")) {
            try {
                Path sig = Paths.get(args[1] + ".sig");
                LargeInteger m = new LargeInteger(Files.readAllBytes(sig)); //read m

                FileInputStream pubkey = new FileInputStream("pubkey.rsa");

                byte[] byteArrayE = new byte[65];
                byte[] byteArrayN = new byte[65];
                pubkey.read(byteArrayE, 1, 64);
                pubkey.read(byteArrayN, 1, 64);
                LargeInteger e = new LargeInteger(byteArrayE);
                LargeInteger n = new LargeInteger(byteArrayN);

                pubkey.close();

                if (c.equals(m.modularExp(e, n)))
                    System.out.println("signature is valid");
                else
                    System.out.println("signature is not valid");


            } catch (IOException e) {
                System.out.println("error");
            }

        }
    }
}
