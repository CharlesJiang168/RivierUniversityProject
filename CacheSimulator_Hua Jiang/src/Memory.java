/**
 * A simple structure for main memory.
 * Main memory contains an array of values, 
 * the value is the same value of its address.
 * 
 * 
 * */

public class Memory {
	private long[] data;

    public Memory(long size){
//        data = new int[(int) size]; // something wrong here!! int is half size of real long num
//        for(int i=0; i<size; i++){
//            data[i] = i;
//        }
    }

    public void setBlock(long address, String value){
        //data[(int)address] = MyUtil.hex_to_long(value); //sth. wrong here long vs int
    }

    public long getBlock(long address){
        return address;
    }
    
    public String toString(){
        String result = "";
        result += "MAIN MEMORY:\n";
        result += "Address   Words\n";
        
        int start = MyUtil.hex_to_int("003f7e00");
   
        for(int i=0; i < 1024; i++){
            result += MyUtil.int_to_hex(start + i*8);
            for(int j=0; j < 8; j++){
                result += "  " + MyUtil.long_to_hex(getBlock(start + i*8 + j));
            }
            result += "\n";
        }
        return result;
    }
}
