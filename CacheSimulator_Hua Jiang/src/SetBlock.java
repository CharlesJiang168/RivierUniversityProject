/**
 * 
 * It’s the place the simulator will perform LRU replacement policy
 * 
 * */


import java.util.ArrayList;

//A container class for cache data
public class SetBlock {
	private Cache cacheObj;                   //A pointer to the parent cache object
    private CacheEntry[] entries;
    private ArrayList<String> LRUcontainer;  //we store the whole address of the entry here 
    private int blocks;                       //The number of words -- bytes
    private int tagSize;                      //The size of our tags
    private int boffset;                      //block offset size
    private int index;                        //The number of index bits
    private Cache parent;                     //The cache object that points to this set
    
    /*
     *  Constructor: creates a full, empty set
     *  @param datablocks the amount of blocks per cache entry
     *  @param numEntries the number of entries in the set
     */
    // datablocks -- blocksize, setSize -- numEntries
    public SetBlock(int datablocks, int numEntries, int offset, int index, int tag, Cache cachemem){
        this.boffset = offset;
        this.index = index;
        this.tagSize = tag;
        
        this.entries = new CacheEntry[numEntries];
        this.LRUcontainer = new ArrayList<String>(); //our LRU queue, holds binary addresses
        this.blocks = datablocks;
        this.parent = cachemem;
        
        for(int i = 0; i < numEntries; i++){
            String array[] = new String[datablocks];
            for(int x = 0; x < datablocks; x++){
                array[x] = "00000000";
            }
            this.entries[i] = new CacheEntry(datablocks, "00000000", 0, array, false);
        }
    }

    /*
     *  Write an entry to this set. If it already exists, update it and set to dirty
     *  @param address, the memory location we're writing from, binary string
     *  @param data, the block of data passed in from 
     */
    public int writeEntry(String address, String[] data, Memory mem){ 
        //Check to see if we're holding onto this address in this set
        int entryIndex = findEntry(address); //entryIndex:-1 for hexAddr:506e8631
        
        int miss = 0;
        //We'll need this in both
        String temptag = address.substring(0, this.tagSize);
        //Get the location
        int addressIdx = this.LRUcontainer.indexOf(temptag); //addressIdx:-1

        //Hit: Address is in cache
        if(entryIndex >= 0 && addressIdx > 0){  //&& addressIdx > 0
        	Cache.conflict_misses++;
        	//Keep track of total memory references. 
        	//Write-back only update memory by override
        	Cache.mem_refs++;
            //System.out.println("Cache Hit!");
            //So move it to the end, because it is the most recently used
            CacheEntry test = this.entries[entryIndex];
            //test.printEntry();
            
            //System.out.println("Address is in container at: " + addressIdx);
            //System.out.println("Address : " + address);
            //System.out.println("Looking for tag : " + temptag);
            //System.out.println(LRUcontainer);
            this.LRUcontainer.remove(addressIdx);
            this.LRUcontainer.add(temptag);
            //Now find it's location
            int selectIdx = findEntry(address);
            CacheEntry current = this.entries[selectIdx];
            //Update the entry with the new data, we use the address to find the correct offset
            int boffset = MyUtil.binary_to_int(address.substring(tagSize + index));
            //System.out.println("Current boffset " + boffset + "pulled from " + (tagSize + index));
            current.updateEntry(data, boffset);

        }   
        else {
        	Cache.compulsory_misses++;
            //Miss: Address isn't in cache 
            //System.out.println("We just missed");
            this.LRUcontainer.add(temptag);
            miss = 1;
            String newTag = address.substring(0, this.tagSize);
            int intAddress = MyUtil.binary_to_int(address);
            
            //Offset so entry has correct address
            int position = intAddress % this.blocks; 
            intAddress -= position;
//            System.out.println("this.blocks: " + this.blocks + ", newTag: " 
//            + newTag + ", intAddress:" + intAddress + ", data: " + data);
            CacheEntry newEntry = new CacheEntry(this.blocks, newTag, intAddress, data, true);
            newEntry.makeDirty();
            
            //Find somewhere to put this block in the cache
            int emptyIdx = findEmptyIndex();

            //No vacancy, we have to evict somebody
            if(emptyIdx == -1){ 
            	//System.out.println("SetBlock::writeEntry, no vacancy, address: " + address);
            	evict(LRUcontainer.get(0), newEntry, address, mem);
            } 
            else { //Otherwise set the new entry into the empty Index
            	this.entries[emptyIdx] = newEntry;
            }
        }

        return miss;
    }

    /*
     *  Similar to writeEntry, except we're just inserting an entry into the cache
     *  still perform an evict if set is full
     *  @param address, the memory location we're writing
     *  @param data, the block of data passed in from 
     */
    public CacheEntry insertEntry(String address, String[] data, Memory mem){
        String tag = address.substring(0, this.tagSize);
        long longAddr = MyUtil.binary_to_long(address);
        
        CacheEntry newEntry = new CacheEntry(this.blocks, tag, longAddr, data, true);          
        int emptyIdx = this.findEmptyIndex(); //Now find somewhere to put this block in the cache
        //System.out.println( "@@@@ emptyIdx: " + emptyIdx );
        
        if(emptyIdx == -1){
            //No vacancy, we have to evict somebody
        	Cache.conflict_misses++;
            evict(LRUcontainer.get(0), newEntry, address, mem);     
        } else {
            //Otherwise set the new entry into the empty Index
        	Cache.compulsory_misses++;
            this.entries[emptyIdx] = newEntry;        
        }
        return newEntry;
    }

    public void evict(String toevict, CacheEntry entry, String address, Memory mem){
        //System.out.println(this.LRUcontainer);
        /*for( int i=0; i < entries.length; i++){
            System.out.println(this.entries[i].getTag());
            }*/
        //Before we remove lets check to see if this entry is dirty and if so, write back to memory
        int oldIdx = findTag(toevict);
        //System.out.println("SetBlock::evict, oldIdx: " + oldIdx);
        
        CacheEntry evicted = this.entries[oldIdx];
        //System.out.print("Making room for ");
        //entry.printEntry();
        //System.out.println("I'm evicting " + binary_to_hex(evicted.getTag())  + " right now");
        //evicted.printEntry();
        if (evicted.isDirty()){
            //Write back to memory
            //convert the binary string address to an integer value
            long longAddr = evicted.getAddress();
            
            //System.out.println("I'm writing back at address" + int_to_hex(evicted.getAddress()));
            evicted.write2file(longAddr, mem);
            //System.out.println( int_to_hex(mem.getBlock(intAddr)) );
        }
        LRUcontainer.remove(0);
        //Put the entry into the location that we just evicted
        this.entries[oldIdx] = entry; 
        this.parent.updateEvict();
    }

    /*
     * Read an entry from this set, returns an invalid block if the read fails
     * @param address, the memory location we're looking for
     */
    public CacheEntry readEntry(String address, Memory mem){
    	//System.out.println("SetBlock::readEntry, address: " + address);
        String temptag = address.substring(0, this.tagSize);
        int entryIdx = findEntry(address);
        //System.out.println("entryIdx: " + entryIdx);
        
        if( entryIdx == -1){ //If we read a miss we return an invalid entry
            this.LRUcontainer.add(temptag);
            return new CacheEntry(this.blocks, "", 0, new String[this.blocks], false);
        } 
        else {
            // Return the index location of the address in our LRU
            int addressIdx = this.LRUcontainer.indexOf(temptag);
            //And remove it as it has been used recently
            this.LRUcontainer.remove(entryIdx);
            this.LRUcontainer.add(temptag); 
            return this.entries[entryIdx];
        }
    }

    // Simple helper method to find an empty block in this set
    public int findEmptyIndex(){
        //System.out.println("looking for an empty index");
        for( int i = 0; i < entries.length; i++){
        	CacheEntry current = entries[i];
            //System.out.println( "Entry: " + i + " is " + current.isValid() );
            if( !current.isValid() ){
                return i;
            }
        }
        return -1;
    }

    // Helper to find an entry by address
    private int findEntry(String address){
        String tag = MyUtil.binary_to_hex(address.substring(0, this.tagSize));
        //System.out.println("Find Tag: " + tag);
        for( int i = 0; i < this.entries.length; i++){
        	CacheEntry current = this.entries[i];
            String hexTag = MyUtil.binary_to_hex(current.getTag());
            //System.out.println("Current Tag: " + hexTag);
            if( hexTag.equals(tag)){
                //System.out.println("FOUND TAG");
                return i;
            }
        }
        return -1;
    }

    // Helper to find an entry by address
    private int findTag(String tag){
        String checktag = MyUtil.binary_to_hex(tag);
        //System.out.println("Find Tag: " + checktag);
        for( int i = 0; i < this.entries.length; i++){
        	CacheEntry current = this.entries[i];
            String hexTag = MyUtil.binary_to_hex(current.getTag());
            //System.out.println("Current Tag: " + hexTag);
            if( hexTag.equals(checktag)){
                //System.out.println("FOUND TAG");
                return i;
            }
        }
        return 0;
    }

    public int getIndexLRU(String tag){
        //System.out.println("Length of tag: " + tag.length());
        int intTag = MyUtil.binary_to_int(tag);
        //System.out.println("SIZE: " + this.LRUcontainer.size());
        for( int i = 0; i < this.LRUcontainer.size(); i++){
            int currentInt = MyUtil.binary_to_int(this.LRUcontainer.get(i));
            //System.out.println(currentInt + "   " + intTag + "   " + (currentInt == intTag));
            if( currentInt == intTag){
                return i;
            }
        }
        return -1;
    }

    CacheEntry getEntry(int idx){
        return this.entries[idx];
    }

}
