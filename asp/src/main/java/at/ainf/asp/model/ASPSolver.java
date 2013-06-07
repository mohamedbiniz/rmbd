/**
 * 
 */
package at.ainf.asp.model;

import at.ainf.asp.inputoutputactions.ASPConverter;
import at.ainf.asp.inputoutputactions.StreamGobbler;

import java.io.IOException;


import java.util.Set;

/**
 * @author Melanie Fruehstueck
 *
 */
public class ASPSolver {
	
	public boolean solve(Set<IProgramElement> program) {
		boolean retVal = true;
		Process proc = null;
		Runtime rt = Runtime.getRuntime();
		
		System.out.println("\nProgram executed: ");
		for (IProgramElement pe : program) {
			System.out.println(pe.getString());
		}
		
		ASPConverter con = new ASPConverter();
		String filePath = "";
		try {
			filePath = con.write(program);
		} catch (IOException e) {
			System.out.println("Writing to file failed: " + e);
		}

        String shellName;
        if (System.getProperty("os.name").startsWith("Windows"))
            shellName = "cmd";
        else if (System.getProperty("os.name").startsWith("Linux"))
            shellName = "/bin/sh";
        else shellName = "/bin/sh";  // pipe under w

		String[] cmd = { shellName, "-c", "gringo " + filePath + " | clasp -q | grep UNSATISFIABLE" };
		try {
			proc = rt.exec(cmd);
		} catch (IOException e) {
			System.out.println("Exception within execution of command: " + e);
		}

		StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR");
		StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT");
		errorGobbler.start();
		outputGobbler.start();

		try {
			proc.waitFor();
		} catch (InterruptedException e) { 
			System.out.println("Thread is waiting, sleeping, or otherwise occupied: " + e);
		}
		
		String output = outputGobbler.getOutput();
		if (output.equals("UNSATISFIABLE")) {
			retVal = false;
		}
		
		return retVal;
	}
	
}
