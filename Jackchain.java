import java.util.ArrayList;
import java.util.HashMap;

public class Jackchain{

    public static ArrayList<Block> chain = new ArrayList<>();
    public static HashMap<String, TransactionOutput> UTXOs = new HashMap<>();
    public static final int DIFF = 3;
    public static final float MINIMUM_TRANSACTION = 0.1f;
    public static Wallet wallet1;
    public static Wallet wallet2;

    public static void main(String[] args){

        wallet1 = new Wallet();
        wallet2 = new Wallet();

        System.out.println("Private and public keys:");
        System.out.println(Transaction.getStringFromKey(wallet1.privateKey));
        System.out.println(Transaction.getStringFromKey(wallet1.publicKey));

        Transaction transaction = new Transaction(wallet1.publicKey, wallet2.publicKey, DIFF, null);
        transaction.generateSignature(wallet1.privateKey);

        System.out.println("Signature valid: " + transaction.verifySignature());
    }

    public static Boolean isValidChain(){
        Block current;
        Block previous;
        String target = new String(new char[DIFF]).replace('\0', '0');

        for(int i = 1; i < chain.size(); i++){
            current = chain.get(i);
            previous = chain.get(i - 1);
            
            if(!current.hash.equals(current.calculateHash())){
                System.out.println("Error! Current hashes not aligning");
                return false;
            }

            if(!previous.hash.equals(current.previous)){
                System.out.println("Error! Previous hashes not aligning");
                return false;
            }

            if(!current.hash.substring(0, DIFF).equals(target)){
                System.out.println("This block has not been mined");
                return false;
            }
        }
        return true;
    }
}