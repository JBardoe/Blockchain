import java.util.ArrayList;

public class Jackchain{

    public static ArrayList<Block> chain = new ArrayList<Block>();
    public static int diff = 5;
    public static void main(String[] args){
        chain.add(new Block("First Block", "0"));
        System.out.println("Mining Block 1 ...");
        chain.get(0).mineBlock(diff);

        chain.add(new Block("Second Block", chain.get(0).hash));
        System.out.println("Mining Block 2 ...");
        chain.get(1).mineBlock(diff);

        chain.add(new Block("Third Block", chain.get(1).hash));
        System.out.println("Mining Block 3 ...");
        chain.get(2).mineBlock(diff);


        System.out.println("\nChain is Valid: " + isValidChain());
    }

    public static Boolean isValidChain(){
        Block current;
        Block previous;
        String target = new String(new char[diff]).replace('\0', '0');

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

            if(!current.hash.substring(0, diff).equals(target)){
                System.out.println("This block has not been mined");
                return false;
            }
        }
        return true;
    }
}