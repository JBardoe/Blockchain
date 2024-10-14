import java.security.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a user's wallet with their keys and transactions.
 */
public final class Wallet{
    private PrivateKey privateKey;
    private PublicKey publicKey;

    private final HashMap<String, TransactionOutput> UTXOs = new HashMap<>();

    /**
     * Default constructor for a new wallet that automatically generate the public and private keys.
     */
    public Wallet(){
        generateKeyPair();
    }

    /**
     * Generates a new public and private key for the wallet.
     */
    public void generateKeyPair(){
        try{
            //Create the generators for the keys and the random number generator
            KeyPairGenerator gen = KeyPairGenerator.getInstance("DSA");
            SecureRandom rand = SecureRandom.getInstance("SHA1PRNG");

            //Create the keys and assign them
            gen.initialize(1024, rand);
            KeyPair keyPair = gen.generateKeyPair();

            this.privateKey = keyPair.getPrivate();
            this.publicKey = keyPair.getPublic();
        } catch(NoSuchAlgorithmException e){
            throw new RuntimeException(e);
        }
    }

    /**
     * Calculates the current balance in a wallet using its past transactions.
     * @return The current balance in the wallet
     */
    public float getBalance(){
        float total = 0;

        //Iterate over the unspent transaction outputs in the chain 
        for(Map.Entry<String, TransactionOutput> item : Jackchain.UTXOs.entrySet()){
            TransactionOutput UTXO = item.getValue();

            //If the current wallet was the receiver of the transaction, add it to the wallet list and add its value to the total
            if(UTXO.isOwner(this.publicKey)){
                UTXOs.put(UTXO.id, UTXO);
                total += UTXO.val;
            }
        }
        return total;
    }

    /**
     * Sends funds from the wallet to another wallet
     * @param receiver The public key of the wallet to receive the funds
     * @param val The amount to send
     * @return The transaction created
     */
    public Transaction sendFunds(PublicKey receiver, float val){
        //Check if the wallet has enough funds to send the requested amount
        if(getBalance() < val){
            System.out.println("Not enough funds for transaction.");
            return null;
        }

        ArrayList<TransactionInput> inputs = new ArrayList<>();
        float total = 0;

        //Iterate over the wallet's unspent transaction outputs to gather the funds
        for(Map.Entry<String, TransactionOutput> item : UTXOs.entrySet()){
            //Add the outputs being used to the inputs of the new transaction
            TransactionOutput UTXO = item.getValue();
            total += UTXO.val;
            inputs.add(new TransactionInput(UTXO.id));

            //Once enough is added no more need to added
            if(total > val) break;
        }

        //Generate a new transaction and sign it with the wallet's private key
        Transaction newTransaction = new Transaction(publicKey, receiver, val, inputs);
        newTransaction.generateSignature(privateKey);

        //Remove the used funds from the unspent transactions.
        //Any amount left over will be readded to the wallet next time the getBalance function iterates over the chain
        for(TransactionInput input : inputs){
            UTXOs.remove(input.transactionOutputId);
        }

        return newTransaction;
    }
}