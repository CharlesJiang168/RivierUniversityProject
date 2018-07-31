import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Random;


public class TraceGenerator {

	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
		int fileNum = 2; // 3
		int refMin = 800000; //800000; 1
		int refMax = 1000000; //1000000; 5
		
		int[] actions = {0, 1, 2}; //0 = read, 1 = write, 2 = instruction fetch
		long refAddrMax = (long) Math.pow(2, 32) - 1;
		System.out.println("refAddrMax is " + refAddrMax);

		
		for(int i = 0; i < fileNum; i++) {
			
		    // nextInt is normally exclusive of the top value,
		    // So add 1 to make it inclusive
		    int memRef = new Random().nextInt((refMax - refMin) + 1) + refMin;
			System.out.println("memory reference is " + memRef);
			
			String fileName =  Integer.toString(i) + ".trace";
			
			PrintWriter writer = new PrintWriter(fileName, "UTF-8");
			
			
			
			while ( memRef > 0 ) {
				// Get random action
				int idx = new Random().nextInt(actions.length);
				int act = (actions[idx]);
				
				// Get random address
				long refAddr = nextLong(new Random(), refAddrMax);
				String strhex = Long.toHexString(refAddr).toUpperCase(); 
				
				// Write to trace file
				writer.print( act + " " );
				writer.println( strhex );
				
				memRef = memRef - 1;
			}
			
			
			writer.close();
			System.out.println("Done with trace file (" + fileName + ") writing.");
		}
	}
	
	public static long nextLong(Random rng, long n) {
		// error checking and 2^x checking removed for simplicity.
		long bits, val;
		do {
			bits = (rng.nextLong() << 1) >>> 1;
			val = bits % n;
		} while (bits-val+(n-1) < 0L);
		return val;
	}
	
}
