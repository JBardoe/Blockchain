import java.security.*;
import java.security.spec.*;

public class Wallet{
    public PrivateKey privateKey;
    public PublicKey publicKey;

    public Wallet(){
        generateKeyPair();
    }

    public void generateKeyPair(){
        try{
            KeyPairGenerator gen = KeyPairGenerator.getInstance("ECDSA", "BC");
            SecureRandom rand = SecureRandom.getInstance("SHA1PRNG");
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");

            gen.initialize(ecSpec, rand);
            KeyPair keyPair = gen.generateKeyPair();

            this.privateKey = keyPair.getPrivate();
            this.publicKey = keyPair.getPublic();
        } catch(Exception e){
            throw new RuntimeException(e);
        }
    }
}