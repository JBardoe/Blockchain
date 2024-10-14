import java.util.ArrayList;
import java.util.HashMap;

public class Jackchain{

    private static final ArrayList<Block> chain = new ArrayList<>(); //The chain of blocks
    static HashMap<String, TransactionOutput> UTXOs = new HashMap<>(); //The unspent transaction outputs on the chain
    
    public static final int DIFF = 3; //Required difficulty for mining blocks
    public static final float MINIMUM_TRANSACTION = 0.1f; //Minimum amount transferred in a transaction

    private static Transaction genesisTransaction; //The first transaction

    public static void main(String[] args){}

    /**
     * Checks if the chain is still valid
     * @return Whether the chain is valid
     */
    public static Boolean isValidChain(){
        Block current; //Iterator to go over every block in the chain
        Block previous; //Iterator to track the previous block to ensure the hashes line up
        String target = new String(new char[DIFF]).replace('\0', '0'); //A string of the required number of zeroes to compare to the hash attempts
        HashMap<String, TransactionOutput> tempUTXOs = new HashMap<>(); //A temporary store for the UTXOs during the check

        //Start by readding the genesis transaction
        tempUTXOs.put(genesisTransaction.getOutputs().get(0).id, genesisTransaction.getOutputs().get(0));

        //Iterate over every block in the chain
        for(int i = 1; i < chain.size(); i++){
            current = chain.get(i);
            previous = chain.get(i - 1);
            
            //Return false if the current block's hash is not what it should be
            if(!current.getHash().equals(current.calculateHash())){
                System.out.println("Error! Current hashes not aligning");
                return false;
            }

            //Return false if the actual previous hash does not match the expected previous hash
            if(!previous.getHash().equals(current.getPrevious())){
                System.out.println("Error! Previous hashes not aligning");
                return false;
            }

            //Return false if the current hash does not meet the required difficulty
            if(!current.getHash().substring(0, DIFF).equals(target)){
                System.out.println("This block has not been mined");
                return false;
            }

            TransactionOutput tempOutput;

            //Iterate over every transaction in the current block
            for(int j = 0; j < current.getTransactions().size(); j++){
                Transaction currentTransaction = current.getTransactions().get(j);

                //Return false if the signature cannot be verified
                if(!currentTransaction.verifySignature()){
                    System.out.println("Signature on transaction " + j + "is invalid");
                    return false;
                }

                //Return false if there is an inconsistency between the amount going in and out
                if(currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()){
                    System.out.println("Inputs are not equal to outputs on transaction " + j);
                    return false;
                }

                //Iterate over the inputs to the transaction
                for(TransactionInput input : currentTransaction.getInputs()){
                    tempOutput = tempUTXOs.get(input.transactionOutputId);

                    //Return false if there are no inputs
                    if(tempOutput == null){
                        System.out.println("No input for transaction " + j);
                        return false;
                    }

                    //Return false if input does not align with the output
                    if(input.UTXO.val != tempOutput.val){
                        System.out.println("Input for transaction " + j + "is invalid." );
                        return false;
                    }

                    tempUTXOs.remove(input.transactionOutputId);
                }

                //Iterate over all the outputs of the transaction
                for(TransactionOutput output : currentTransaction.getOutputs()){
                    tempUTXOs.put(output.id, output);
                }

                //Return false if the expected and actual receiver do not align
                if(currentTransaction.getOutputs().get(0).receiver != currentTransaction.getReceiverKey()){
                    System.out.println("Receiver for transaction " + j + " is incorrect.");
                    return false;
                }
            }
        }

        //Otherwise return that the chain is valid
        System.out.println("Chain is valid");
        return true;
    }

    /**
     * Mines and adds a block to the chain
     * @param newBlock The block to be added
     */
    public static void addBlock(Block newBlock){
        newBlock.mineBlock(DIFF);
        chain.add(newBlock);
    }
}