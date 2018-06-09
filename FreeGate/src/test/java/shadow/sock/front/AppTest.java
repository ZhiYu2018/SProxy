package shadow.sock.front;

import shadow.sock.freegate.core.ssl.SFCrypto;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        assertTrue( true );
    }
    
    public void testSF(){
    	byte[]key = "ABCDEFGHIJKLMNOP".getBytes();
    	byte[] s = "1234567890asdfghjklzxcvbnm".getBytes();
    	byte[] d = new byte[s.length];
    	for(int i = 0; i < d.length; i++){
    		d[i] = SFCrypto.encode(s[i], key);
    	}
    	
    	System.out.println("dd:[" + new String(d) + "]");
    	byte[] ss = new byte[s.length];
    	for(int i = 0; i < ss.length; i++){
    		ss[i] = SFCrypto.decode(d[i], key);
    	}
    	
    	System.out.println("SS:[" + new String(ss) + "]");
    }
    
}
