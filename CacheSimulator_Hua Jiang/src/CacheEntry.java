/**
 * A class used to contain cache data,
 * tracks the valid, dirty and tag fields.
 * 
 * */

public class CacheEntry {
	private boolean valid;
    private boolean dirty;
    private String[] data;
    private String tag;
    private long address;
    
    /*
     * Constructor, uses deep copy on incoming block data
     * @param numblocks, the number of blocks this entry is expecting
     * @param tag,       the tag for this entry
     * @param location,  the address in memory that this entry points to as an int
     * @param blocks,    the datablock for this entry
     * @param isValid,   is this a valid entry
     */
    public CacheEntry(int numblocks, String tag, long location, String[] blocks, boolean isValid){
        this.tag = tag;
        this.address = location;
        valid = isValid;
        dirty = false;
        data = new String[numblocks];
        String zeroes = "";
            for(int i = 0; i < numblocks; i++){
            if( blocks[i] != null && blocks[i].length() < 8){
                    for(int x = 0; x < (8-blocks[i].length()); x++) {
                            zeroes += "0";
                    }
                    blocks[i] = "" + zeroes + blocks[i];
            }
            data[i] = blocks[i];
        }
    }
    

    //Update method to just change the data associated with it, sets the dirty bit
    //TODO: handle the offset address with same tag problem.
    public void updateEntry(String[] blocks, int boffset){
        this.data[boffset] = blocks[boffset];
        makeDirty();
    }

    //Helper method for writing back an entry if it is dirty
    public void write2file(long address, Memory mem){
        for(int i = 0; i < this.data.length; i++){
            //mem.setBlock( address + i, this.data[i]);
        }
    }

    //returns whether this is a valid entry
    public boolean isValid(){
        return this.valid;
    }

    public int isValidNum(){
        if(this.valid)
            return 1;
        else return 0;
    }
    
    //Get the tag for this entry
    public String getTag(){
        return this.tag;
    }
    
    //Set the dirty bit on this entry
    public void makeDirty() {
        this.dirty = true;
    } 

    //Given a block offset, get the inner block from this entry 
    public String getWord(int blockoffset) {
        return this.data[blockoffset];
    }       
    
    public boolean isDirty() {
        return this.dirty;
    }
    
    public int isDirtyNum() {
        if(this.dirty)
            return 1;
        else return 0;
    }

    public long getAddress() {
        return this.address;
    }

    public void printEntry(){
        System.out.println( "Valid: " + isValid() );
        System.out.println( "Tag: " + printTag() );
        System.out.println( "Dirty: " + isDirty() );
        System.out.println( "Address: " + MyUtil.long_to_hex(getAddress()));
        System.out.println( "Data: ");
        for( int i = 0; i < this.data.length; i++){
            System.out.print( getWord(i) + "  ");
        }
        System.out.println();
    }
    
    public String printTag(){
        String tagtag = MyUtil.binary_to_hex(this.tag);
        int temp = 8 - tagtag.length();
        String zeroes = "";
        for(int i = 0;  i < temp; i++){
            zeroes += "0";
        }
        return zeroes + MyUtil.binary_to_hex(this.tag);
    }
}
