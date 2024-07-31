import java.util.Date;
import java.security.MessageDigest;

public class Block{
    public String hash;
    public String previous;
    private String data;
    private long timeStamp;
    private int count;

    public Block(String data, String previous){
        this.data = data;
        this.previous = previous;
        this.timeStamp = new Date().getTime();
        this.hash = calculateHash();
    }

    public String calculateHash(){
        return encryptSha(previous + Long.toString(timeStamp) + Integer.toString(count) + data);
    }

    public void mineBlock(int diff){
        String target = new String(new char[diff]).replace('\0', '0');
        while(!hash.substring(0, diff).equals(target)){
            count++;
            hash = calculateHash();
        }
        System.out.println("Block Mined!!! : " + hash);
    }

    public static String encryptSha(String plain){
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(plain.getBytes("UTF-8"));
            StringBuffer hexString = new StringBuffer();
            for(int i = 0; i < hash.length; i++){
                String hex = Integer.toHexString(0xff & hash[i]);
                if(hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

}