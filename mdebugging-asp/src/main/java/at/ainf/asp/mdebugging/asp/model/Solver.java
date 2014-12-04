/**
 * 
 */
package at.ainf.asp.mdebugging.asp.model;

import java.io.IOException;

import at.ainf.asp.mdebugging.asp.Main;
import at.ainf.asp.mdebugging.asp.helper.AnswerSetReaderHelper;

/**
 * @author Melanie Frühstück
 *
 */
public class Solver {
	
	private String _answerSet;
	
	/**
	 * Executes solver clingo on an ASP file.
	 * @param filePath
	 */
	public void solve(String filePath) {
		Process proc = null;
		Runtime rt = Runtime.getRuntime();
		
//		String gringo = "gringo ";
//		String clasp = "clasp ";
		String clingo = "clingo";
		if (Main.pathSet) {
			clingo = Main.clingoPath;
		}
		
        if (System.getProperty("os.name").startsWith("Windows")) {
        	String[] cmd = { "cmd", "/c", clingo + " " + filePath };
        	try {
    			proc = rt.exec(cmd);
    		} catch (IOException e) {
    			System.out.println("Exception during execution of command: " + e);
    		}
        } else if (System.getProperty("os.name").startsWith("Linux")) {
        	String[] cmd = { "/bin/sh", "-c", clingo + " " + filePath };
        	try {
    			proc = rt.exec(cmd);
    		} catch (IOException e) {
    			System.out.println("Exception during execution of command: " + e);
    		}
        } else {
        	String[] cmd = { "/bin/sh", "-c", clingo + " " + filePath };
        	try {
    			proc = rt.exec(cmd);
    		} catch (IOException e) {
    			System.out.println("Exception during execution of command: " + e);
    		}
        }
		AnswerSetReaderHelper helperOutput = new AnswerSetReaderHelper(proc.getInputStream());
		helperOutput.start();

		try {
			proc.waitFor();
		} catch (InterruptedException e) { 
			System.out.println("Thread is waiting, sleeping, or otherwise occupied: " + e);
		}
		
		try {
			helperOutput.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		setAnswerSets(helperOutput.getOutput());
	}

	/**
	 * Returns the raw answer set, i.e. raw output of the solver.
	 * @return the raw answer set
	 */
	public String getRawAnswerSets() {
		return _answerSet;
	}

	/**
	 * Sets the raw answer set of the program, i.e. the raw output of the solver.
	 * @param answerSets
	 */
	public void setAnswerSets(String answerSets) {
		this._answerSet = answerSets;
	}

}
