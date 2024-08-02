import java.util.ArrayList;
import java.util.HashMap;

public class Jackchain{

    public static ArrayList<Block> chain = new ArrayList<>();
    public static HashMap<String, TransactionOutput> UTXOs = new HashMap<>();
    
    public static final int DIFF = 3;
    public static final float MINIMUM_TRANSACTION = 0.1f;

    public static Wallet wallet1;
    public static Wallet wallet2;

    public static Transaction genesisTransaction;

    public static void main(String[] args){
    }

    public static Boolean isValidChain(){
        Block current = chain.get(1);
        Block previous;
        String target = new String(new char[DIFF]).replace('\0', '0');
        HashMap<String, TransactionOutput> tempUTXOs = new HashMap<>();
        tempUTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));

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

        TransactionOutput tempOutput;
        for(int j = 0; j < current.transactions.size(); j++){
            Transaction currentTransaction = current.transactions.get(j);

            if(!currentTransaction.verifySignature()){
                System.out.println("Signature on transaction " + j + "is invalid");
                return false;
            }

            if(currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()){
                System.out.println("Inputs are not equal to outputs on transaction " + j);
                return false;
            }

            for(TransactionInput input : currentTransaction.inputs){
                tempOutput = tempUTXOs.get(input.transactionOutputId);

                if(tempOutput == null){
                    System.out.println("No input for transaction " + j);
                    return false;
                }

                if(input.UTXO.val != tempOutput.val){
                    System.out.println("Input for transaction " + j + "is invalid." );
                    return false;
                }

                tempUTXOs.remove(input.transactionOutputId);
            }

            for(TransactionOutput output : currentTransaction.outputs){
                tempUTXOs.put(output.id, output);
            }

            if(currentTransaction.outputs.get(0).receiver != currentTransaction.receiverKey){
                System.out.println("Receiver for transaction " + j + " is incorrect.");
                return false;
            }
        }

        System.out.println("Chain is valid");
        return true;
    }

    public static void addBlock(Block newBlock){
        newBlock.mineBlock(DIFF);
        chain.add(newBlock);
    }
}