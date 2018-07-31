/**
 * 
 * Main function has three phases 
 * Configuration parameters collecting, 
 * Cache initialization and Trace file parsing, 
 * and Displaying statistics.
 * 
 * 
 * */

import java.io.BufferedReader;
import java.io.FileReader;

public class CacheSimulator {

	// U, a single unified cache; SC, separate cache
	private static String UoSC;
	// Size of block in bytes
	private static int UBlockSize, IBlockSize, DBlockSize;
	// Size of a cache set in blocks
	private static int USetSize, ISetSize, DSetSize;
	// Number of blocks in a cache
	private static long UNumBlocks, INumBlocks, DNumBlocks;
	
	// UHT, a cache hit time in nanosecond; Miss penalty in nanosecond
	public static double UNT, MissPenalty;
	public static int total_refs;
	
	// Those need to be calculated by configuration above
	final static int MEM_ADDR = 32; // 28 bits
	
	// Total memory size
	final static long memorySize = (long) Math.pow(2, MEM_ADDR) - 1;
	
    final static int CACHE_READ = 0;
    final static int CACHE_WRITE = 1;
    final static int INSTRUCTION_FETCH = 2; //Both read and instruction fetch are treated as read references

    public static void main(String[] args) throws java.io.IOException {
    	//UorSC=U UBlockSize=16 USetSize=2 UNumBlocks=1024 UNT=5 MissPenalty=100
    	//UorSC=U UBlockSize=16 USetSize=1 UNumBlocks=1024 UNT=5 MissPenalty=100
    	//UorSC=U UBlockSize=128 USetSize=4 UNumBlocks=16384 UNT=2 MissPenalty=108
    	//UorSC=SC IBlockSize=64 ISetSize=1 INumBlocks=2048 DBlockSize=128 DSetSize=2 DNumBlock=16384 UNT=1 MissPenalty=120

    	System.out.println("memorySize: " + memorySize);
    	
    	CacheSimulator c = new CacheSimulator();
    	//*************************************************
    	// Phase 1 -- Collecting configuration parameters
    	//*************************************************
        if(!c.parseParams(args)) { // Invalid parameters
            return;
        }
        if(args.length == 0) {
        	System.err.println("Please specify the configuration parameters...");
			System.err.println("usage: java CacheSimulator UorSC=<SC> IBlockSize=<int> ISetSize=<int> INumBlocks=<int> DBlockSize=<int> DSetSize=<int> DNumBlock=<int> UNT=<double> MissPenalty=<double>\n");
			return;
        }
        printConfiguration();
 
        //*************************************************
    	// Phase 2 -- Reading trace file
    	//*************************************************
        int read_write;
        BufferedReader br = new BufferedReader(new FileReader("2.trace"));
        Memory mem = new Memory(memorySize); 
 
        //UorSC=U UBlockSize=16(cache_blocksize) USetSize=2(cache_associativity) UNumBlocks=1024(cache_capacity)
        // Initialize Cache
//        Cache cachemem = new Cache(c.cache_capacity, c.cache_blocksize, c.cache_associativity, mem);
//        Cache cachemen, cachemenData, cachemenInstr;
        if ( UoSC.equals("U") ) {
        	Cache cachemen = new Cache( UNumBlocks,  UBlockSize,  USetSize, mem);
     
        	System.out.println("...... Parsing trace files ......");
        	try {
                String line = br.readLine();

                while (line != null) {
                	//System.out.println("ACTION:" + line);
                	total_refs++;
                    // Read the first character of the line.
                    // It determines 0 = read, 1 = write, 2 = instruction fetch.
                    String action = "" + line.charAt(0);
                    read_write = Integer.parseInt(action);                
                    //System.out.println(" read_write:" + read_write);

                    // Read the address (as a hex number)
                    String hexAddr = (line.substring(1)).trim(); // get rid of front space
//                    System.out.print(" hexAddr:" + hexAddr);
//                    long longAddr = Long.parseLong(hexAddr, 16);
//                    System.out.println(" <> longAddr:" + longAddr);

                    //if it is a cache write the we have to read the data
                    if(read_write == CACHE_WRITE) { // 1 -- write
                    	//data = hex_to_int(strings[2]);
                    	//String address, String value
                    	cachemen.cacheWrite(hexAddr, hexAddr);
                    } 
                    else if(read_write == CACHE_READ){ // 0 -- read, NO 2 -- fetch intruction
                    	cachemen.cacheRead(hexAddr); // read hex address string as value
                    	//System.out.println( "Read data: " + cachemem.cacheRead(hexAddr) );
                    }
                    //Output the new contents
                    //System.out.println("memory[" + longAddr + "] = " + mem.getBlock(longAddr)); 
                    
                    line = br.readLine();
                }
            } finally {
                br.close();
            }
        	System.out.println("--------- End of trace files parsing -----------");
        	
            //*************************************************
        	// Phase 3 -- Displaying statistics
        	//*************************************************
            System.out.println("--------- Displaying Statistics -----------");
//            cachemem.outputState();
            System.out.println(cachemen);
//            System.out.println(mem);
        }
        else {
        	//UorSC=SC IBlockSize=64 ISetSize=1 INumBlocks=2048 DBlockSize=128 DSetSize=2 DNumBlock=16384
        	Cache cachemenData = new Cache( DNumBlocks, DBlockSize, DSetSize, mem);
        	Cache cachemenInstr = new Cache( INumBlocks, IBlockSize, ISetSize, mem);
        	
        	System.out.println("...... Parsing trace files ......");
        	try {
                String line = br.readLine();

                while (line != null) {
                	//System.out.println("ACTION:" + line);
                	total_refs++;
                    // Read the first character of the line.
                    // It determines 0 = read, 1 = write, 2 = instruction fetch.
                    String action = "" + line.charAt(0);
                    read_write = Integer.parseInt(action);                
                    //System.out.println(" read_write:" + read_write);

                    // Read the address (as a hex number)
                    // 4,294,967,295
                    String hexAddr = (line.substring(1)).trim(); // get rid of front space
                    //System.out.print(" hexAddr:" + hexAddr);
//                    long longAddr = Long.parseLong(hexAddr, 16);
                    //System.out.println(" <> longAddr:" + longAddr);
                    
                    //if it is a cache write the we have to read the data
                    if(read_write == CACHE_WRITE) { // 1 -- write
                    	//data = hex_to_int(strings[2]);
                    	//String address, String value
                    	cachemenData.cacheWrite(hexAddr, hexAddr);
                    } 
                    else if(read_write == CACHE_READ) { // 0 -- read
                    	cachemenData.cacheRead(hexAddr); // read hex address string as value
                    	//System.out.println( "Read data: " + cachemem.cacheRead(hexAddr) );
                    }
                    else { //2 -- fetch intruction
                    	cachemenInstr.cacheRead(hexAddr);
                    }
                    //Output the new contents
                    //System.out.println("memory[" + longAddr + "] = " + mem.getBlock(longAddr)); 
                    
                    line = br.readLine();
                }
            } finally {
                br.close();
            }
        	System.out.println("--------- End of trace files parsing -----------");
            //*************************************************
        	// Phase 3 -- Displaying statistics
        	//*************************************************
            System.out.println("--------- Displaying statistics -----------");
//            cachemem.outputState();
            // All statistics are class variables,
            // Data Cache and Instruction Cache have same statistics result
            System.out.println(cachemenInstr); 
        }
        
    }

    public boolean parseParams(String[] args)
    {
        //needed for the parsing of command line options
        boolean errflg, b_flag, s_flag, n_flag, db_flag, ds_flag, dn_flag;
        boolean h_flag, m_flag; // Flags for hit time and miss penalty
        errflg = b_flag = s_flag = n_flag = db_flag = ds_flag = dn_flag = true;
        h_flag = m_flag = true;
        
        for(int i = 0; i < args.length; i++) {
        	//System.out.println(args[i]);
        	
        	// Unified cache or separate cache
        	if ( args[i].contains("UorSC") ) {
        		UoSC = args[i].substring( args[i].indexOf("=")+1, args[i].length() );
        		errflg = false;
        	}
        	
        	// Unified cache configuration
        	else if ( args[i].contains("UBlockSize") ) {
        		UBlockSize = Integer.parseInt( args[i].substring( args[i].indexOf("=")+1, args[i].length() ) );
        		b_flag = false;
        	}
        	else if ( args[i].contains("USetSize") ) {
        		USetSize = Integer.parseInt( args[i].substring( args[i].indexOf("=")+1, args[i].length() ) );
        		s_flag = false;
        	}
        	else if ( args[i].contains("UNumBlocks") ) {
        		UNumBlocks = Integer.parseInt( args[i].substring( args[i].indexOf("=")+1, args[i].length() ) );
        		n_flag = false;
        	}
        	
        	// Separate Instruction cache configuration
        	else if ( args[i].contains("IBlockSize") ) {
        		IBlockSize = Integer.parseInt( args[i].substring( args[i].indexOf("=")+1, args[i].length() ) );
        		b_flag = false;
        	}
        	else if ( args[i].contains("ISetSize") ) {
        		ISetSize = Integer.parseInt( args[i].substring( args[i].indexOf("=")+1, args[i].length() ) );
        		s_flag = false;
        	}
        	else if ( args[i].contains("INumBlocks") ) {
        		INumBlocks = Integer.parseInt( args[i].substring( args[i].indexOf("=")+1, args[i].length() ) );
        		n_flag = false;
        	}
        	
        	// Separate Data cache configuration
        	else if ( args[i].contains("DBlockSize") ) {
        		DBlockSize = Integer.parseInt( args[i].substring( args[i].indexOf("=")+1, args[i].length() ) );
        		db_flag = false;
        	}
        	else if ( args[i].contains("DSetSize") ) {
        		DSetSize = Integer.parseInt( args[i].substring( args[i].indexOf("=")+1, args[i].length() ) );
        		ds_flag = false; 
        	}
        	else if ( args[i].contains("DNumBlock") ) {
        		DNumBlocks = Integer.parseInt( args[i].substring( args[i].indexOf("=")+1, args[i].length() ) );
        		dn_flag = false;
        	}
        	else if ( args[i].contains("UNT") ) {
        		UNT = Integer.parseInt( args[i].substring( args[i].indexOf("=")+1, args[i].length() ) );
        		h_flag = false;
        	}
        	else if ( args[i].contains("MissPenalty") ) {
        		MissPenalty = Integer.parseInt( args[i].substring( args[i].indexOf("=")+1, args[i].length() ) );
        		m_flag = false;
        	}
        	
        	// Wrong key word
        	else {
        		System.err.println("Not a key word.\n");
        	}
        }
        

        //check if we have all the options and have no illegal options
        //UorSC=U UBlockSize=16 USetSize=2 UNumBlocks=1024
    	//UorSC=SC IBlockSize=64 ISetSize=1 INumBlocks=2048 DBlockSize=128 DSetSize=2 DNumBlock=16384
        if( !errflg ){ // UoSC is set
        	if( UoSC.equals("U") ){
        		if( b_flag || s_flag || n_flag || h_flag || m_flag ){
        			System.err.println("usage: java CacheSimulator UorSC=<U> UBlockSize=<int> USetSize=<int> UNumBlocks=<int> UNT=<double> MissPenalty=<double>\n");
                    return false;
        		}
        	}
        	else {
        		if( b_flag || s_flag || n_flag || db_flag || ds_flag || dn_flag || h_flag || m_flag){
        			System.err.println("usage: java CacheSimulator UorSC=<SC> IBlockSize=<int> ISetSize=<int> INumBlocks=<int> DBlockSize=<int> DSetSize=<int> DNumBlock=<int> UNT=<double> MissPenalty=<double>\n");
                    return false;
        		}
        	}
        	
        }
        return true;
        
    }

    public static void printConfiguration(){
    	System.out.println("--------- Configuration -----------");
    	if ( UoSC.equals("U") ){
        	//UorSC=U UBlockSize=16 USetSize=2 UNumBlocks=1024
    		System.out.println( "Unified Cache, Block size: " + UBlockSize + "bytes, Set size: " 
        	+ USetSize + "blocks, Number of Blocks: " + UNumBlocks + "blocks, Hit time: " + UNT + "ns, Miss penalty: " + MissPenalty + "ns");
    	}
    	else {
        	//UorSC=SC IBlockSize=64 ISetSize=1 INumBlocks=2048 DBlockSize=128 DSetSize=2 DNumBlocks=16384
    		System.out.println( "Separate Cache, Instruction Block size: " + IBlockSize + "bytes, Instruction Set size: " + ISetSize + "blocks, Instruction Number of Blocks: " + INumBlocks + "blocks");
    		System.out.println( "				 Data Block size: " + DBlockSize + "bytes, Data Set size: " + DSetSize + "blocks, Data Number of Blocks: " + DNumBlocks
    				+ "blocks, Hit time: " + UNT + "ns, Miss penalty in ns: " + MissPenalty + "ns");
    	}
    }

}
