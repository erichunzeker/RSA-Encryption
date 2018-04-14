import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

public class RsaKeyGen {
    public static void main(String[] args) {

        Random random = new Random();
        LargeInteger p, q, n, phiN, e, d, one, two;

        one = new LargeInteger(new byte[]{(byte) 1});
        two = new LargeInteger(new byte[]{(byte) 2});


        do {
            p = new LargeInteger(256, random);
            q = new LargeInteger(256, random);
            n = p.multiply(q);
            phiN = (p.subtract(one)).multiply(q.subtract(one));
            e = new LargeInteger(512, random);
        } while(phiN.subtract(e).isNegative() || !phiN.XGCD(e)[0].subtract(two).isNegative());

        d = e.modularExp(one.negate(), phiN);

        try {
            FileOutputStream pubkey = new FileOutputStream("pubkey.rsa");
            pubkey.write(e.resize().getVal());
            pubkey.write(n.resize().getVal());
            pubkey.close();
            FileOutputStream privkey = new FileOutputStream("privkey.rsa");
            privkey.write(d.resize().getVal());
            privkey.write(n.resize().getVal());
            privkey.close();


        } catch (IOException i) {
            System.out.println("File error");
        }
    }
}
