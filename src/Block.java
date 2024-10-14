import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;

/**
 * Represents a block in the blockchain
 */
public final class Block{
    private String hash; //Hash value for the block
    private final String previous; //Hash value for the previous block
    private String merkleRoot; //Merkle root of the block, generated though repeatedly hashing the transactions in the block 
    private final ArrayList<Transaction> transactions = new ArrayList<>(); //Transactions included in this block
    private final long timeStamp; //Time the block is created
    private int count; //A sequentially increased number used to make the hash of the block match the chain's difficulty requiremnet

    /**
     * Constructor for a new block 
     * @param previous Hash of the previous block
     */
    public Block(String previous){
        this.previous = previous;
        this.timeStamp = new Date().getTime();

        this.hash = calculateHash();
    }

    /**
     * Generates the hash for the current block by applying the encryption method to the previous block's hash, the time of creation, the current count, and the merkle root
     * @return
     */
    public String calculateHash(){
        return encryptSha(previous + Long.toString(timeStamp) + Integer.toString(count) + merkleRoot);
    }

    /**
     * Mines the block by adjusting the count and rehashing the block until it meets the requirements and can be added to the chain
     * @param diff The difficulty (number of leading zeroes) the final hash should have
     */
    public void mineBlock(int diff){
        //Generate the merkle root using the current transactions
        merkleRoot = getMerkleRoot(transactions);

        //Generate a string of the required number of zeroes to compare to the hash attempts
        String target = new String(new char[diff]).replace('\0', '0');

        //Rehash the block, generating a new count every time, until there are the required number of zeroes
        while(!hash.substring(0, diff).equals(target)){
            count++;
            hash = calculateHash();
        }
        System.out.println("Block Mined!!! : " + hash);
    }

    /**
     * Adds a transaction to the block
     * @param transaction The transaction to be added
     * @return Whether the transaction was able to be added
     */
    public boolean addTransaction(Transaction transaction){
        //If no transaction is given, nothing can be added
        if(transaction == null) return false;
        
        //If this is not the first block and the transaction cannot be processed correctly, it should not be added
        if(!previous.equals("0") && transaction.processTransaction() != true){
            System.out.println("Transaction failed to process.");
            return false;
        }

        //Add the transaction
        transactions.add(transaction);
        System.out.println("Transaction successfullly added.");
        return true;
    }

    /**
     * Encrypts a string using the SHA-256 algorithm
     * @param plain The plaintext to be encrypted
     * @return The encrypted text
     */
    public static String encryptSha(String plain){
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(plain.getBytes("UTF-8"));

            StringBuilder hexString = new StringBuilder();
            for(int i = 0; i < hash.length; i++){
                String hex = Integer.toHexString(0xff & hash[i]);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e){
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the merkle root of the transactions in the block by repeatedly hashing the ids of transactions in the block
     * @param transactions The transactions in the block
     * @return The generated merkle root
     */
    public static String getMerkleRoot(ArrayList<Transaction> transactions){
        //Start by filling the previous layer with the raw ids of the transactions
        int count = transactions.size();
        ArrayList<String> previousTreeLayer = new ArrayList<>();

        for(Transaction transaction : transactions){
            previousTreeLayer.add(transaction.getID());
        }

        ArrayList<String> treeLayer = previousTreeLayer;

        //Rehash the ids together in pairs until there is only 1 string left  
        while(count > 1){
            treeLayer = new ArrayList<>();
            for(int i = 1; i < previousTreeLayer.size(); i++){
                treeLayer.add(encryptSha(previousTreeLayer.get(i - 1) + previousTreeLayer.get(i)));
            }
            count = treeLayer.size();
            previousTreeLayer = treeLayer;
        }

        return (treeLayer.size() == 1) ? treeLayer.get(0) : "";
    }

    public String getHash(){return this.hash;}
    public String getPrevious(){return this.previous;}
    public ArrayList<Transaction> getTransactions(){return this.transactions;}
}