import java.security.*;
import java.util.ArrayList;
import java.util.Base64;

public class Transaction{
    
    public String id;
    public PublicKey senderKey;
    public PublicKey receiverKey;
    public float val;
    public byte[] signature;
    
    public ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
    public ArrayList<TransactionOutput> outputs = new ArrayList<TransactionOutput>();

    private static int sequence = 0;

    public Transaction(PublicKey senderKey, PublicKey receiverKey, float val, ArrayList<TransactionInput> inputs){
        this.senderKey = senderKey;
        this.receiverKey = receiverKey;
        this.val = val;
        this.inputs = inputs;
    } 

    private String calculateHash(){
        sequence++;
        return Block.encryptSha(getStringFromKey(senderKey) + getStringFromKey(receiverKey) + Float.toString(val) + sequence);
    }

    public static String getStringFromKey(Key key){
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    public void generateSignature(PrivateKey privateKey){
        String data = getStringFromKey(senderKey) + getStringFromKey(receiverKey) + Float.toString(val);
        this.signature = ECDSASigUtils.applyECDSASig(privateKey, data);
    }

    public boolean verifySignature(){
        String data = getStringFromKey(senderKey) + getStringFromKey(receiverKey) + Float.toString(val);
        return ECDSASigUtils.verifyECDSASig(senderKey, data, signature);
    }
}

class TransactionInput{

}

class TransactionOutput{

}

class ECDSASigUtils{
    public static byte[] applyECDSASig(PrivateKey privateKey, String input){
        Signature dsa;
        byte[] output = new byte[0];
        try{
            dsa = Signature.getInstance("ECDSA", "BC");
            dsa.initSign(privateKey);
            byte[] strByte = input.getBytes();
            dsa.update(strByte);
            byte[] realSig = dsa.sign();
            output = realSig;
        } catch(Exception e){
            throw new RuntimeException(e);
        }
        return output;
    }

    public static boolean verifyECDSASig(PublicKey publicKey, String data, byte[] signature){
        try{
            Signature ecdsaVerify = Signature.getInstance("ECDSA", "BS");
            ecdsaVerify.initVerify(publicKey);
            ecdsaVerify.update(data.getBytes());
            return ecdsaVerify.verify(signature);
        } catch(Exception e){
            throw new RuntimeException(e);
        }
    }
}