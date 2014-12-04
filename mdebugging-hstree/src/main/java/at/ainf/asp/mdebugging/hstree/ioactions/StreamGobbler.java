package at.ainf.asp.mdebugging.hstree.ioactions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import at.ainf.asp.mdebugging.hstree.Application;

/**
 * @author Melanie Frühstück
 *
 */
public class StreamGobbler extends Thread {
	
	InputStream is;
    String output;
    
    public StreamGobbler(InputStream is) {
        this.is = is;
        this.output = "";
    }
    
    /**
     * Thread processes the output of the solver.
     */
    public void run() {
        try {
        	InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            StringBuffer buf = new StringBuffer();
            String line=null;
            while ( (line = br.readLine()) != null) {
            	if (Application.enableInfo) { System.out.println(line); }
            	buf.append(line).append("\n");
            }
            output = buf.toString();
        } catch (IOException ioe) {
            ioe.printStackTrace();  
        }
    }
    
    public String getOutput() {
    	return output;
    }
}
