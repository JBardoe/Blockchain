import java.security.*;
import java.util.ArrayList;
import java.util.Base64;

/**
 * Represents a single transaction between two agents
 */
public class Transaction{
    
    private String id; //Transaction id
    private final PublicKey senderKey; //Public key of the sender wallet
    private final PublicKey receiverKey; //Public key of the receiver wallet
    private final float val; //Value transferred in the transaction
    private byte[] signature; //Signature generated from the sender's public key to verify the transaction 
    
    private ArrayList<TransactionInput> inputs = new ArrayList<>(); //Input funds to the transaction 
    private final ArrayList<TransactionOutput> outputs = new ArrayList<>(); //Output funds from the transaction 

    private static int sequence = 0; //Number of transactions executed

    /**
     * Constructor for a new transaction 
     * @param senderKey Public key of the sender
     * @param receiverKey Public key of the receiver
     * @param val Value transferred in the transaction
     * @param inputs Input funds to the transaction
     */
    public Transaction(PublicKey senderKey, PublicKey receiverKey, float val, ArrayList<TransactionInput> inputs){
        this.senderKey = senderKey;
        this.receiverKey = receiverKey;
        this.val = val;
        this.inputs = inputs;
    } 

    /**
     * Hashes the information of the transaction to generate an id
     * @return The hashed string
     */
    private String calculateHash(){
        sequence++;
        return Block.encryptSha(getStringFromKey(senderKey) + getStringFromKey(receiverKey) + Float.toString(val) + sequence);
    }

    /**
     * Gets the string part out of a key 
     * @param key The to extract the string from
     * @return The extracted string
     */
    public static String getStringFromKey(Key key){
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    /**
     * Uses the sender's private key to sign the transaction 
     * @param privateKey Private key of the sender
     */
    public void generateSignature(PrivateKey privateKey){
        String data = getStringFromKey(senderKey) + getStringFromKey(receiverKey) + Float.toString(val);
        this.signature = DSASigUtils.applyDSASig(privateKey, data);
    }

    /**
     * Checks the signature on the transaction is valid using the sender's public key
     * @return Whether the signature on the transaction is valid
     */
    public boolean verifySignature(){
        String data = getStringFromKey(senderKey) + getStringFromKey(receiverKey) + Float.toString(val);
        return DSASigUtils.verifyDSASig(senderKey, data, signature);
    }

    /**
     * Processes, checks, and executes the transaction then logs the results on the chain
     * @return Whether the transaction is able to be correctly processed
     */
    public boolean processTransaction(){
        //Return false if the signature is invalid
        if(!verifySignature()){
            System.out.println("Invalid Signature");
            return false;
        }

        //Get unspent transaction output for each input from the central repository on the chain
        for(TransactionInput input : inputs){
            input.UTXO = Jackchain.UTXOs.get(input.transactionOutputId);
        }

        float inputsValue = getInputsValue(); //Calculate the total value of the inputs

        //Return false if the transaction is less than the required minimum
        if(inputsValue < Jackchain.MINIMUM_TRANSACTION){
            System.out.println("Transaction inputs too small. Currently " + inputsValue + ". Needs to be at least " + Jackchain.MINIMUM_TRANSACTION);
            return false;
        }

        this.id = calculateHash(); //Generate the id of the transaction

        //Generate the outputs of the transaction for the sender and receiver 
        outputs.add(new TransactionOutput(this.receiverKey, val, id));
        outputs.add(new TransactionOutput(this.senderKey, inputsValue - val, id));

        //Add the outputs to the unspent transaction outputs on the chain
        for(TransactionOutput output : outputs){
            Jackchain.UTXOs.put(output.id, output);
        }

        //Iterate over the inputs and remove any UTXOs from the centralised chain
        for(TransactionInput input :  inputs){
            if(input.UTXO == null) continue;
            Jackchain.UTXOs.remove(input.UTXO.id);
        }

        return true;
    }

    /**
     * Iterates over the inputs and calculates the total value inputted to the transaction
     * @return The total value of the inputs
     */
    public float getInputsValue(){
        float total = 0.0f;
        for(TransactionInput input : inputs){
            if(input.UTXO == null)
                continue;
            total += input.UTXO.val;
        }
        return total;
    }

    /**
     * Iterates over the outputs and calculates the total value outputted from the transaction 
     * @return The total value of the outputs
     */
    public float getOutputsValue(){
        float total = 0.0f;
        for(TransactionOutput output : outputs){
            total += output.val;
        }
        return total;
    }

    ArrayList<TransactionInput> getInputs(){return this.inputs;}
    ArrayList<TransactionOutput> getOutputs(){return this.outputs;}
    public String getID(){return this.id;}
    public PublicKey getReceiverKey(){return this.receiverKey;}
}

/**
 * Represents an input into a transaction
 */
class TransactionInput{
    protected String transactionOutputId; //Id of the linked utput
    protected TransactionOutput UTXO; //Unspent transaction output generated

    /**
     * Constructor for a new transaction input 
     * @param transactionOutputId Linked transaction output id
     */
    public TransactionInput(String transactionOutputId){
        this.transactionOutputId = transactionOutputId;
    }
}

/**
 * Represents an output from a transaction 
 */
class TransactionOutput{
    protected String id; //Id of the output
    protected PublicKey receiver; //Public key of the sender
    protected float val; //Value left in the output
    protected String parentTransactionId; //Id of the transaction

    /**
     * Constructor for a new transaction output
     * @param receiver Public key of the receiver
     * @param val Value of the transaction
     * @param parentTransactionId Id of the parent transaction
     */
    public TransactionOutput(PublicKey receiver, float val, String parentTransactionId){
        this.receiver = receiver;
        this.val = val;
        this.parentTransactionId = parentTransactionId;
        this.id = Block.encryptSha(Transaction.getStringFromKey(receiver) + Float.toString(val) + parentTransactionId);
    }

    /**
     * Checks whether a person is the receiver of a transaction
     * @param key Public key to check
     * @return Whether the key given is the same as the receiver of the transaction
     */
    public boolean isOwner(PublicKey key){
        return key == receiver;
    }
}

/**
 * A class of utility function for using DSA signatures
 */
class DSASigUtils{
    /**
     * Applies the DSA signature algorithm to generate a signature
     * @param privateKey The private key of the person signing
     * @param input A string of transaction information to incorporate into the signature
     * @return The generated signature
     */
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

    /**
     * Verifies that a DSA signature is correct
     * @param publicKey The public key of the signer
     * @param data String of transaction information that should be incorporated into the signature
     * @param signature The signature to check
     * @return Whether the signature is correct
     */
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