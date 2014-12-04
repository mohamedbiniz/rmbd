package at.ainf.asp.mdebugging.asp.helper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import at.ainf.asp.mdebugging.asp.model.Program;
import at.ainf.asp.mdebugging.asp.model.Rule;

/**
 * @author Melanie Frühstück
 *
 */
public class ExportHelper {

	/**
	 * Exports the program to the file path given.
	 * @param program
	 * @param fileName
	 * @throws IOException
	 */
	public static void export(Program program, String fileName) throws IOException {
		
		File fileOut = new File(fileName);
		PrintWriter pw = new PrintWriter(new FileWriter(fileOut));
		int numRules = program.getRules().size();
		
		for (int i = 0; i < numRules; i++) {
			Rule rule = program.getRules().get(i);
			if ("".equals(rule.getLabel())) {
				pw.println(rule.getRule());
			} else {
				pw.println(rule.getRuleIncludingLabel());
			}
		}
		pw.println();
		
		if (!program.getLabelConstraints().isEmpty()) {
			for (Rule con : program.getLabelConstraints()) {
				pw.println(con.getRule());
			}
			pw.println();
		}
		
		for (Rule fact : program.getFacts()) {
			pw.println(fact.getRule());
		}
		
		pw.flush();
		pw.close();
	}
	
}
