/**
 * 
 */
package at.ainf.asp.model;

import at.ainf.asp.inputoutputactions.ASPConverter;
import at.ainf.asp.inputoutputactions.StreamGobbler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


import java.util.Set;

/**
 * @author Melanie Fruehstueck
 *
 */
public class ASPSolver {


    private static Logger logger = LoggerFactory.getLogger(ASPSolver.class.getName());

	public boolean solve(Set<IProgramElement> program) {
		boolean retVal = true;
		Process proc = null;
		Runtime rt = Runtime.getRuntime();
		
		logger.info("\nProgram executed: ");
		for (IProgramElement pe : program) {
            logger.info(pe.getString());
		}
		
		ASPConverter con = new ASPConverter();
		String filePath = "";
		try {
			filePath = con.write(program);
		} catch (IOException e) {
            logger.info("Writing to file failed: " + e);
		}

        String shellName;
        if (System.getProperty("os.name").startsWith("Windows"))
            shellName = "cmd";
        else if (System.getProperty("os.name").startsWith("Linux"))
            shellName = "/bin/sh";
        else shellName = "/bin/sh";  // pipe under w

		String[] cmd = { shellName, "/c", "gringo " + filePath + " | clasp -q | grep UNSATISFIABLE" };
		try {
			proc = rt.exec(cmd);
		} catch (IOException e) {
            logger.info("Exception within execution of command: " + e);
		}

		StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR");
		StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT");
		errorGobbler.start();
		outputGobbler.start();

		try {
			proc.waitFor();
		} catch (InterruptedException e) {
            logger.info("Thread is waiting, sleeping, or otherwise occupied: " + e);
		}
		
		String output = outputGobbler.getOutput();
		if (output.equals("UNSATISFIABLE")) {
			retVal = false;
		}

//        logger.info ("result of " + cnt + " check: " + retVal);
//        if (cnt == 0 && !retVal ||
//                cnt == 1 && retVal ||
//                cnt == 2 && !retVal ||
//                cnt == 3 && !retVal ||
//                cnt == 4 && !retVal ||
//                cnt == 5 && retVal ||
//                cnt == 6 && retVal ||
//                cnt == 7 && retVal)
//            logger.info (" unexpected result :-)");
//        cnt++;


		return retVal;
	}

    // private static int cnt = 0;


}
