/**
 * 
 */
package at.ainf.asp.model;

import at.ainf.asp.inputoutputactions.ASPConverter;
import at.ainf.asp.inputoutputactions.StreamGobbler;

import java.io.IOException;
import java.util.Set;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import at.ainf.asp.test.Application;

import at.ainf.asp.antlr.ASPOutputLexer;
import at.ainf.asp.antlr.ASPOutputParser;

/**
 * @author Melanie Fruehstueck
 *
 */
public class ASPSolver {
	
	public boolean solve(Set<IProgramElement> program) {
		boolean retVal = false;
		Process proc = null;
		Runtime rt = Runtime.getRuntime();
		
		if (Application.enableInfo) {
			System.out.println("\nProgram executed: ");
			for (IProgramElement pe : program) {
				System.out.println(pe.getString());
			}
		}
		
		ASPConverter con = new ASPConverter();
		String filePath = "";
		try {
			filePath = con.write(program);
		} catch (IOException e) {
			System.out.println("Writing to file failed: " + e);
		}
		
		
		String gringo = "gringo ";
		String clasp = "clasp ";
		if (Application.pathSet) {
			gringo = Application.gringoPath + " ";
			clasp = Application.claspPath + " ";
		}
		
        if (System.getProperty("os.name").startsWith("Windows")) {
        	String[] cmd = { "cmd", "/c", gringo + filePath + " | " + clasp + "-q" };
        	try {
    			proc = rt.exec(cmd);
    		} catch (IOException e) {
    			System.out.println("Exception within execution of command: " + e);
    		}
        } else if (System.getProperty("os.name").startsWith("Linux")) {
        	String[] cmd = { "/bin/sh", "-c", gringo + filePath + " | " + clasp + "-q" };
        	try {
    			proc = rt.exec(cmd);
    		} catch (IOException e) {
    			System.out.println("Exception within execution of command: " + e);
    		}
        } else {
        	String[] cmd = { "/bin/sh", "-c", gringo + filePath + " | " + clasp + "-q" };
        	try {
    			proc = rt.exec(cmd);
    		} catch (IOException e) {
    			System.out.println("Exception within execution of command: " + e);
    		}
        }

        if (Application.enableInfo) { System.out.println("\n"); }
		StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR");
		StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT");
		errorGobbler.start();
		outputGobbler.start();

		try {
			proc.waitFor();
		} catch (InterruptedException e) { 
			System.out.println("Thread is waiting, sleeping, or otherwise occupied: " + e);
		}
		
		try {
			errorGobbler.join();
			outputGobbler.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		String output = outputGobbler.getOutput();
		
		// parse output
		ANTLRInputStream input = new ANTLRInputStream(output);
        ASPOutputLexer lexer = new ASPOutputLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ASPOutputParser parser = new ASPOutputParser(tokens);
        ParseTree tree = parser.output();
        OutputListener pl = new OutputListener();
        ParseTreeWalker.DEFAULT.walk(pl, tree);
        
        //get output
        ASPOutput o = ASPOutput.getASPOutputInstance();
		
		if (o.isSatisfiabl()) {
			retVal = true;
		}
		
		return retVal;
	}

}
