package unimelb.bitbox;

import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;


class Client{

    public static void main(String[] args ) {

        Security.addProvider(new BouncyCastleProvider());
        // Create the public and private keys
        KeyPairGenerator generator;
        try {
            generator = KeyPairGenerator.getInstance("RSA", "BC");
            // BASE64Encoder b64 = new BASE64Encoder();
            Base64 b64 = new Base64();
            SecureRandom random = createFixedRandom();
            generator.initialize(1024, random);
            //System.out.println(random);
            KeyPair pair = generator.generateKeyPair();
            Key pubKey = pair.getPublic();
            Key privKey = pair.getPrivate();

            String encodedStringPub = new String(b64.encode(pubKey.getEncoded()));
            String encodedStringPriv = new String(b64.encode(privKey.getEncoded()));

            System.out.println("publicKey : " + encodedStringPub);
            System.out.println("privateKey : " + encodedStringPriv);


        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    public static SecureRandom createFixedRandom()
    {
        return new FixedRand();
    }
    private static class FixedRand extends SecureRandom {

        MessageDigest sha;
        byte[] state;

        FixedRand() {
            try
            {
                this.sha = MessageDigest.getInstance("SHA-1");
                this.state = sha.digest();
            }
            catch (NoSuchAlgorithmException e)
            {
                throw new RuntimeException("can't find SHA-1!");
            }
        }

    }
}


