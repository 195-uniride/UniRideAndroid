public class IpAddress{
    public boolean detectAddress(String adr){

    }

    public boolean detectIPV4(String adr){
        String[] blocks = adr.split("//.");
        if(blocks.length() == 4){
            for (String b: blocks){
                try {
                    int block = Integer.parseInt(b);
                }
                catch (NumberFormatException e){
                    return false;
                }
                if(block<0 || block>255){
                    return false;
                }
            }
            return true;
        }
        else{
            return false;
        }
    }

    public boolean detectIPV6(String adr){
        String blocks = adr.split("//:");
        if(blocks.length() == 4){
            for(String b: blocks){
                if(b.length>4){
                    return false;
                }
                String.characters = adr.split("");
                for(String c: characters){
                    try{
                        int blockInt = Integer.parseInt(b);
                        if(blockInt<0 || blockInt>9){
                            return false;
                        }
                    }
                    catch(NumberFormatException e){
                        c = c.toLowerCase();
                        Char blockChar = c.charAt(0);
                        if(blockChar<'a' || blockChar>'f'){
                            return false;
                        }
                    }
                }
            }
            return true
        }
        else{
            return false;
        }
    }
}
