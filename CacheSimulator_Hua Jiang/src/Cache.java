/**
 * A key part for cache simulator. 
 * It stores all cache information like cache size, 
 * number of sets, tag fields, index fields, offset fields, 
 * and also all the global statistical variables. 
 * It’s the entry point for write, read memory references. 
 * 
 * */
public class Cache {

	final static int MEM_ADDR = 32; // 28 bits
	// Get from user configuration
	private long numBlocks;
	private int blockSize;
	private int setSize;

	// Calculated by configuration
	private long cacheSize; // numBlocks * blockSize = cacheSize
	private int numSets;
	private int tag, index, offset;

	private SetBlock[] sets;
	private Memory sysMem;     //The memory object that we will be writing to and reading from

	public static int mem_refs;
	public static int attempts_total;
	
	public static int read_attempts, write_attempts;
	public static int cache_hit, cache_miss;
	public static int miss_total, miss_reads, miss_writes, num_evicted;
	public static int hit_reads, hit_writes;
	public static double missrate_total, missrate_reads, missrate_writes, hitrate_total;
	public static double compulsory_misses, conflict_misses;
	

	//  public Cache(long capacity, long blocksize, long associativity, Memory mem){
	public Cache(long numblocks, int blocksize, int setsize, Memory mem){
		this.numBlocks = numblocks;
		this.blockSize = blocksize;
		this.setSize = setsize;
		//Set the memory object
		this.sysMem = mem;

		calculation();

		//System.out.println("We are making " + numSets + " entries");
		sets = new SetBlock[this.numSets];

		for( int i = 0; i < this.numSets; i++){
			sets[i] = new SetBlock(this.blockSize, this.setSize, this.offset, this.index, this.tag, this);
		}
		
		//Finally initialize just missed reads and writes, 
		//the rest can be instantiated when we run toString
		miss_reads = 0;
		miss_writes = 0;
	}

	public void calculation(){
		//private long numBlocks, blockSize, setSize;
		//    	if ( UoSC.equals("U") ) { // Unified Cache
		this.cacheSize = this.numBlocks * this.blockSize;
		this.numSets = (int) (this.numBlocks / this.setSize);

		offset = (int) (Math.log(blockSize) / Math.log(2));
		index = (int) (Math.log(numSets) / Math.log(2));
		tag = MEM_ADDR - offset - index;

		System.out.println("After calculation, cacheSize: " + cacheSize + "bytes, numSets: " + numSets 
				+ "sets\ntag: " + tag + "bits, index: " + index + "bits, offset: " + offset + "bits");
	}


	/*
	 * This method attempts to write to cache, if we find that entry we write to it
	 * otherwise we just create a new entry, either way the dirty bit will be set
	 * @param address: the location that this entry will represent
	 * @param value  : the value to be written to cache
	 */
	public void cacheWrite(String address, String value){
		String binaryAddr = MyUtil.hex_to_binary(address); //binaryAddr: 11101010111000010111110001000001
		this.write_attempts++;
		
		if( binaryAddr.length() != MEM_ADDR ) {
        	int numZeros = MEM_ADDR - binaryAddr.length();
        	String zeros = "";
        	for(int i = 0; i < numZeros; i++) {
        		zeros += "0";
        	}
        	binaryAddr += zeros;
        }		
		//Which set do we access?
		//setLocation(in parenthesis): 1110 1010 1110 0001 011(1 1100 0100) 0001
		int setLocation = MyUtil.binary_to_int( binaryAddr.substring(tag, tag+index));
		//System.out.println("Writing to set: " + setLocation);
		
		SetBlock currentSet = this.sets[setLocation];
		//Create a block of memory to pass to writeEntry
		String[] memBlock = makeBlock(binaryAddr);
		//update it with the new value
		long memAddr = MyUtil.binary_to_long(binaryAddr);
		long index = memAddr % this.blockSize;
		memBlock[(int) index] = value;
		//System.out.println("cacheWrite>>>memAddr: " + memAddr + "| memBlock index:" + index + " >>>> value: " + value);
		
		//Write it to the cache and update the miss ratio
		int hitormiss = currentSet.writeEntry(binaryAddr, memBlock, this.sysMem);
		if( hitormiss > 0){
			addWriteMiss();
		}
	}

	/*
	 * This method is used to construct a datablock from memory
	 * checks the datablock size to ensure we keep the entried aligned to that size
	 * @param startAddr: the location that we start building from, in binary
	 * @param mem      : the memory object that we will be reading from
	 */
	 
	public String[] makeBlock(String startAddr){
		//Mod the integer value of this address to get it's position in a datablock
		long start = MyUtil.binary_to_long(startAddr);
//		System.out.println("Inside makeBlock, startAddr:" + startAddr + ", start: " + start);
		
		long position = start % this.blockSize;
		start -= position;

		String[] dataBlock = new String[this.blockSize];
		for(int i = 0; i < this.blockSize; i++){
			dataBlock[i] = MyUtil.long_to_hex(this.sysMem.getBlock( start + i ));
		}
		return dataBlock;
	}

	/*
	 * This method is used to read from the cache, if the cache misses read from memory
	 * and create a new entry and add it to the cache
	 * @param address: the address that we're trying to find in cache
	 * @param mem    : the memory object that we will be reading from
	 */
	public String cacheRead(String address){
		//TODO: get the right set block/ direct mapped block for this address
		//Change it to a 32 bit address
		this.read_attempts++;
		String binaryAddr = MyUtil.hex_to_binary(address);
		
        if( binaryAddr.length() != MEM_ADDR ) {
        	int numZeros = MEM_ADDR - binaryAddr.length();
        	String zeros = "";
        	for(int i = 0; i < numZeros; i++) {
        		zeros += "0";
        	}
        	binaryAddr += zeros;
        }		
		int setLocation = MyUtil.binary_to_int( binaryAddr.substring(this.tag, this.tag+this.index));
		
		SetBlock currentSet = this.sets[setLocation];
		CacheEntry attempt = currentSet.readEntry(binaryAddr, this.sysMem);
		if( !attempt.isValid()){ 
			//If the attempt is an invalid block we need to read from memory and update the miss count
			this.mem_refs++;
			addReadMiss();
			//Change the offset of the block so it fits our block alignment
			long startAddr = MyUtil.hex_to_long(address);
			int startOffset = (int) (startAddr % this.blockSize);
			
			//offsetBin is our offset binary address
			String offsetBin = MyUtil.long_to_binary(startAddr - startOffset);
			String[] memBlock = makeBlock(offsetBin);
//			System.err.println("Cache::cacheRead, startAddr:" + startAddr + ", startOffset:"+startOffset+", offsetBin:"+offsetBin);
//			System.err.println("binaryAddr: " + binaryAddr);
			attempt = currentSet.insertEntry(binaryAddr, memBlock, this.sysMem);
		}
		//Get the block offset bits and convert to an int
		int boffset = MyUtil.binary_to_int( binaryAddr.substring(32 - this.offset));
//		System.err.println("Cache::cacheRead, boffset: " +boffset);
		
//		long startTime = System.nanoTime();
		String ret = attempt.getWord(boffset); 
//		this.hit_time = System.nanoTime() - startTime;
				
		return ret;
	}
	
	public void updateEvict(){
        this.num_evicted++;
    }

	public void addReadMiss(){
		this.miss_reads++;
	}

	public void addWriteMiss(){
		this.miss_writes++;
	}

	public void outputState(){
		System.out.println("miss_reads: " + this.miss_reads + " miss_writes: " + this.miss_writes);
		System.out.println("tag: " + this.tag + " index: " + this.index + " offset: " + this.offset ); 
	}

	public void writeToFile(){
		for( int i = 0; i < this.sets.length; i++){
			SetBlock cSet = this.sets[i];
			for( int tEntry = 0; tEntry < this.setSize; tEntry++){
				CacheEntry cEntry = cSet.getEntry(tEntry);
				long address = cEntry.getAddress();
				cEntry.write2file(address, this.sysMem);
			}
		}
	}



	public String toString(){
		String result = "";
		attempts_total = read_attempts + write_attempts;
		miss_total = miss_reads + miss_writes;
		if (miss_reads != 0){
			missrate_reads = (double)miss_reads / read_attempts;
		} else {
			missrate_reads = 0;
		}
		if (miss_writes != 0){
			missrate_writes = (double)miss_writes / write_attempts;
		} else {
			missrate_writes = 0;
		}
		missrate_total = (double)(miss_writes + miss_reads) / attempts_total;
		hitrate_total = 1 - missrate_total;
		
		hit_reads = read_attempts - miss_reads;
		hit_writes = write_attempts - miss_writes;
		
		cache_hit = hit_reads + hit_writes;
		cache_miss = miss_reads + miss_writes;
		
		double compulsaryRate = compulsory_misses / miss_total;
		double conflictRate = conflict_misses / miss_total;
		
		//AMAT = Hit time * Hit rate + Miss rate * Miss penalty
		double AMAT = CacheSimulator.UNT * hitrate_total + CacheSimulator.MissPenalty * missrate_total;
		
		result += "Total Memory References: " + Cache.attempts_total + " Out of " + CacheSimulator.total_refs + "\n";
		//result += "Total Memory References: " + Cache.mem_refs + "\n";
		result += "Average Memory Access Time: " + String.format("%.4g", AMAT) + "\n\n"; 
		
		result += "Cache hits: " + cache_hit + " <<>> Cache misses: " + cache_miss + "\n";
		result += "Hit rate total: " + String.format("%.4g", hitrate_total) + "\n";
		result += "Miss rate total: " + String.format("%.4g", missrate_total) + "\n\n";
		
		result += "Miss Read: " + miss_reads + " & Miss Write: " + miss_writes + "\n";
		result += "Miss Read Rate: " + String.format("%.4g", missrate_reads) + " & Miss Write Rate: " + String.format("%.4g", missrate_writes) + "\n";
		
		result += "Compulsary misses: " + Cache.compulsory_misses + " & Conflict misses: " + Cache.conflict_misses + "\n";
		result += "Compulsary Miss Rate: " + String.format("%.4g", compulsaryRate) + " & Conflict Miss Rate: " + String.format("%.4g", conflictRate) + "\n\n";
 
		return result;
	}
}
