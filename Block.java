import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;

public class Block{
    public String hash;
    public String previous;
    public String merkleRoot;
    public ArrayList<Transaction> transactions = new ArrayList<>();
    private final long timeStamp;
    private int count;

    public Block(String previous){
        this.previous = previous;
        this.timeStamp = new Date().getTime();

        this.hash = calculateHash();
    }

    public String calculateHash(){
        return encryptSha(previous + Long.toString(timeStamp) + Integer.toString(count) + merkleRoot);
    }

    public void mineBlock(int diff){
        merkleRoot = getMerkleRoot(transactions);
        String target = new String(new char[diff]).replace('\0', '0');
        while(!hash.substring(0, diff).equals(target)){
            count++;
            hash = calculateHash();
        }
        System.out.println("Block Mined!!! : " + hash);
    }

    public boolean addTransaction(Transaction transaction){
        if(transaction == null)
            return false;
        
        if(!previous.equals("0") && transaction.processTransaction() != true){
            System.out.println("Transaction failed to process.");
            return false;
        }

        transactions.add(transaction);
        System.out.println("Transaction successfullly added.");
        return true;
    }

    public static String encryptSha(String plain){
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(plain.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for(int i = 0; i < hash.length; i++){
                String hex = Integer.toHexString(0xff & hash[i]);
                if(hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e){
            throw new RuntimeException(e);
        }
    }

    public static String getMerkleRoot(ArrayList<Transaction> transactions){
        int count = transactions.size();
        ArrayList<String> previousTreeLayer = new ArrayList<>();

        for(Transaction transaction : transactions){
            previousTreeLayer.add(transaction.id);
        }

        ArrayList<String> treeLayer = previousTreeLayer;

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
}