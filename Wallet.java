
import java.security.*;

public final class Wallet{
    public PrivateKey privateKey;
    public PublicKey publicKey;

    public Wallet(){
        generateKeyPair();
    }

    public void generateKeyPair(){
        try{
            KeyPairGenerator gen = KeyPairGenerator.getInstance("DSA");
            SecureRandom rand = SecureRandom.getInstance("SHA1PRNG");

            gen.initialize(1024, rand);
            KeyPair keyPair = gen.generateKeyPair();

            this.privateKey = keyPair.getPrivate();
            this.publicKey = keyPair.getPublic();
        } catch(NoSuchAlgorithmException e){
            throw new RuntimeException(e);
        }
    }
}