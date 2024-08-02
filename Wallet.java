import java.security.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class Wallet{
    public PrivateKey privateKey;
    public PublicKey publicKey;

    public HashMap<String, TransactionOutput> UTXOs = new HashMap<>();


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

    public float getBalance(){
        float total = 0;

        for(Map.Entry<String, TransactionOutput> item : Jackchain.UTXOs.entrySet()){
            TransactionOutput UTXO = item.getValue();
            if(UTXO.isOwner(this.publicKey)){
                UTXOs.put(UTXO.id, UTXO);
                total += UTXO.val;
            }
        }
        return total;
    }

    public Transaction sendFunds(PublicKey receiver, float val){
        if(getBalance() < val){
            System.out.println("Not enough funds for transaction.");
            return null;
        }

        ArrayList<TransactionInput> inputs = new ArrayList<>();
        
        float total = 0;

        for(Map.Entry<String, TransactionOutput> item : UTXOs.entrySet()){
            TransactionOutput UTXO = item.getValue();
            total += UTXO.val;
            inputs.add(new TransactionInput(UTXO.id));
            if(total > val)
                break;
        }

        Transaction newTransaction = new Transaction(publicKey, receiver, val, inputs);
        newTransaction.generateSignature(privateKey);

        for(TransactionInput input : inputs){
            UTXOs.remove(input.transactionOutputId);
        }

        return newTransaction;
    }
}