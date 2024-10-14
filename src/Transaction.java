import java.security.*;
import java.util.ArrayList;
import java.util.Base64;

public class Transaction{
    
    private String id;
    private final PublicKey senderKey;
    private final PublicKey receiverKey;
    private final float val;
    private byte[] signature;
    
    private ArrayList<TransactionInput> inputs = new ArrayList<>();
    private final ArrayList<TransactionOutput> outputs = new ArrayList<>();

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
        this.signature = DSASigUtils.applyDSASig(privateKey, data);
    }

    public boolean verifySignature(){
        String data = getStringFromKey(senderKey) + getStringFromKey(receiverKey) + Float.toString(val);
        return DSASigUtils.verifyDSASig(senderKey, data, signature);
    }

    public boolean processTransaction(){
        if(!verifySignature()){
            System.out.println("Invalid Signature");
            return false;
        }

        for(TransactionInput input : inputs){
            input.UTXO = Jackchain.UTXOs.get(input.transactionOutputId);
        }

        float inputsValue = getInputsValue();

        if(inputsValue < Jackchain.MINIMUM_TRANSACTION){
            System.out.println("Transaction inputs too small. Currently " + inputsValue + ". Needs to be at least " + Jackchain.MINIMUM_TRANSACTION);
            return false;
        }

        this.id = calculateHash();
        outputs.add(new TransactionOutput(this.receiverKey, val, id));
        outputs.add(new TransactionOutput(this.senderKey, inputsValue - val, id));

        for(TransactionOutput output : outputs){
            Jackchain.UTXOs.put(output.id, output);
        }

        for(TransactionInput input :  inputs){
            if(input.UTXO == null)
                continue;
            Jackchain.UTXOs.remove(input.UTXO.id);
        }

        return true;
    }

    public float getInputsValue(){
        float total = 0.0f;
        for(TransactionInput input : inputs){
            if(input.UTXO == null)
                continue;
            total += input.UTXO.val;
        }
        return total;
    }

    public float getOutputsValue(){
        float total = 0.0f;
        for(TransactionOutput output : outputs){
            total += output.val;
        }
        return total;
    }

    protected ArrayList<TransactionInput> getInputs(){return this.inputs;}
    protected ArrayList<TransactionOutput> getOutputs(){return this.outputs;}
    public String getID(){return this.id;}
    public PublicKey getReceiverKey(){return this.receiverKey;}
}

class TransactionInput{
    protected String transactionOutputId;
    protected TransactionOutput UTXO;

    public TransactionInput(String transactionOutputId){
        this.transactionOutputId = transactionOutputId;
    }
}

class TransactionOutput{
    protected String id;
    protected PublicKey receiver;
    protected float val;
    protected String parentTransactionId;

    public TransactionOutput(PublicKey receiver, float val, String parentTransactionId){
        this.receiver = receiver;
        this.val = val;
        this.parentTransactionId = parentTransactionId;
        this.id = Block.encryptSha(Transaction.getStringFromKey(receiver) + Float.toString(val) + parentTransactionId);
    }

    public boolean isOwner(PublicKey key){
        return key == receiver;
    }
}

class DSASigUtils{
    public static byte[] applyDSASig(PrivateKey privateKey, String input){
        Signature dsa;
        byte[] output = new byte[0];
        try{
            dsa = Signature.getInstance("DSA");
            dsa.initSign(privateKey);
            byte[] strByte = input.getBytes();
            dsa.update(strByte);
            byte[] realSig = dsa.sign();
            output = realSig;
        } catch(InvalidKeyException | NoSuchAlgorithmException | SignatureException e){
            throw new RuntimeException(e);
        }
        return output;
    }

    public static boolean verifyDSASig(PublicKey publicKey, String data, byte[] signature){
        try{
            Signature dsaVerify = Signature.getInstance("DSA");
            dsaVerify.initVerify(publicKey);
            dsaVerify.update(data.getBytes());
            return dsaVerify.verify(signature);
        } catch(InvalidKeyException | NoSuchAlgorithmException | SignatureException e){
            throw new RuntimeException(e);
        }
    }
}