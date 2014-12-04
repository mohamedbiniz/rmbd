package at.ainf.asp.mdebugging.asp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import at.ainf.asp.antlr.AnswerSetLexer;
import at.ainf.asp.antlr.AnswerSetParser;
import at.ainf.asp.antlr.ProgramLexer;
import at.ainf.asp.antlr.ProgramParser;
import at.ainf.asp.mdebugging.asp.helper.ExportHelper;
import at.ainf.asp.mdebugging.asp.helper.File2StringHelper;
import at.ainf.asp.mdebugging.asp.model.AnswerSet;
import at.ainf.asp.mdebugging.asp.model.AnswerSetListener;
import at.ainf.asp.mdebugging.asp.model.Program;
import at.ainf.asp.mdebugging.asp.model.ProgramListener;
import at.ainf.asp.mdebugging.asp.model.Rule;
import at.ainf.asp.mdebugging.asp.model.Solver;

/**
 * @author Melanie Frühstück
 *
 */
public class Main {
	
	private static final String falseParameterMsg = "Type in [-help] or [-h] to show possible parameters and how they are used.";
	// INFO: if module doesn't serve as a executable jar, the fileToDebug has to be set manually
	private static String fileToDebug = System.getProperty("user.dir") + "/testfiles/house_p02t002.lp";
	// INFO: if you want to set clingo path manually, set pathSet to true 
	public static boolean pathSet = false;
	public static String clingoPath = "";

	public static void main(String[] args) throws Exception {
		// INFO: if you want to use the asp module as executable jar, uncomment the following comment
		handleCommandLine(args);

		String programString = File2StringHelper
				.convertFromFileToString(fileToDebug);

		ANTLRInputStream programInput = new ANTLRInputStream(programString);
		ProgramLexer programLexer = new ProgramLexer(programInput);
		CommonTokenStream programTokens = new CommonTokenStream(programLexer);
		ProgramParser programParser = new ProgramParser(programTokens);
		ParseTree programTree = programParser.prog();

		ProgramListener pl = new ProgramListener();
		ParseTreeWalker.DEFAULT.walk(pl, programTree);

		Program program = pl.getProgram();
		// add once the additional rules
		program.addAdditionalStatements();

		int counter = 0;
		fileToDebug = fileToDebug.substring(0, fileToDebug.length() - 3);
		fileToDebug += "_ext" + counter + ".lp";
		fileToDebug = fileToDebug.replace("original", "extended");
		ExportHelper.export(program, fileToDebug);
		
		Solver solver = new Solver();
		solver.solve(fileToDebug);
		String rawAS = solver.getRawAnswerSets();

		// parse raw answer set and get labels
		List<AnswerSet> answerSets = parseAnswerSetOutput(rawAS);
//		if (answerSets.size() >= 1 && answerSets.get(0).getAnswerSet() != "") {
//			System.out.println("AS size: " + answerSets.size());
//		}
		AnswerSet as = answerSets.get(answerSets.size() - 1);
		List<String> labels = extractLabelPredicate(as);
		Set<Rule> insertedRules = new HashSet<Rule>();

		// add constraints (diagnoses) to program
		while (AnswerSet.SATISFIABLE.equals(as.getState())) {
			StringBuffer labelBuffer = new StringBuffer();
			for (int i = 0; i < labels.size(); i++) {
				if (i == 0) {
					labelBuffer.append(labels.get(i));
				} else {
					labelBuffer.append(", ").append(labels.get(i));
				}
			}
			Rule rule = new Rule(":-" + labelBuffer.toString() + ".");
			if (!insertedRules.contains(rule)) {
				insertedRules.add(rule);
				program.addLabelConstraint(rule);
			}
			printDiagnosis(program, labels, counter);
			
			counter++;
			fileToDebug = fileToDebug.substring(0, fileToDebug.length() - 4);
			fileToDebug += counter + ".lp";
			ExportHelper.export(program, fileToDebug);
			solver.solve(fileToDebug);
			// after adding constraints to program, solve again
			// to get next minimal diagnosis
			rawAS = solver.getRawAnswerSets();
			answerSets.clear();
			answerSets = parseAnswerSetOutput(rawAS);
//			if (answerSets.size() >= 1
//					&& answerSets.get(0).getAnswerSet() != "") {
//				System.out.println("AS size: " + answerSets.size());
//			}
			if (!answerSets.isEmpty()) {
				as = answerSets.get(answerSets.size() - 1);
			}
			labels.clear();
			labels = extractLabelPredicate(as);
			// parse raw answer set and get labels
		}

	}

	private static void printDiagnosis(Program program, List<String> labels, int counter) {
		System.out.println("\nDiagnosis " + counter + ":");
		for (String l : labels) {
			System.out.println("Label: " + l);
			System.out.println("Rule: " + program.getRuleByLabel(l).getRule());
		}
	}

	private static void handleCommandLine(String[] args) {
		String dir = System.getProperty("user.dir") + "/";
		
		// handle arguments of command line
		if (args.length==0) {
			System.out.println(falseParameterMsg);
			System.exit(1);
		} else if (args[0].equals("-help") || args[0].equals("-h")) {
			printHelp();
			System.exit(0);
		// -f example.lp
		} else if (args.length==2) {
			if (args[0].equals("-file") || args[0].equals("-f")) {
				fileToDebug = dir + args[1];
			} else {
				System.out.println(falseParameterMsg);
				System.exit(1);
			}
		} else if (args.length==4) {
			// -clingo /usr/bin/clingo -file example.lp
			boolean clingo = args[0].equals("-clingo") || args[0].equals("-c");
			boolean file = args[2].equals("-file") || args[2].equals("-f");
			if (clingo && file) {
				clingoPath = args[1];
				pathSet = true;
				fileToDebug = args[3];
			} else {
				System.out.println(falseParameterMsg);
				System.exit(1);
			}
		} else {
			System.out.println(falseParameterMsg);
			System.exit(1);
		}
		
	}

	private static void printHelp() {
		System.out.println(" Make sure that clingo is in your path (for UNIX) or in the current directory (for WINDOWS)." + 
				"\n Otherwise it is possible to set the path manually (see below)." +
				"\n To debug a monotone answer set program use the following parameter:" +
				"\n [-file] or [-f] followed by the file to debug." +
				"\n E.g. java -jar monotonic-asp-asp-1.0.jar -file example.lp" +
				"" +
				"\n\n There is the possibility to set the path of clingo:" +
				"\n [-clingo] or [-c] followed by the clingo path." +
				"\n E.g. java -jar monotonic-asp-asp-1.0.jar -clingo /usr/bin/clingo -f example.lp");
	}

	private static List<AnswerSet> parseAnswerSetOutput(String input) {
		// parse output
		ANTLRInputStream asInput = new ANTLRInputStream(input);
		AnswerSetLexer asLexer = new AnswerSetLexer(asInput);
		CommonTokenStream asTokens = new CommonTokenStream(asLexer);
		AnswerSetParser asParser = new AnswerSetParser(asTokens);
		ParseTree asTree = asParser.output();

		AnswerSetListener asl = new AnswerSetListener();
		ParseTreeWalker.DEFAULT.walk(asl, asTree);

		return asl.getAnswerSets();
	}

	private static List<String> extractLabelPredicate(AnswerSet as) {
		List<String> labels = new ArrayList<String>();
		String regex = ".*?(label\\([0-9]+\\)\\s)+.*";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(as.getAnswerSet());
		String labelString = "";
		if (matcher.matches()) {
			labelString = matcher.group(1);
			String[] literals = labelString.split(" ");
			for (String lit : literals) {
				labels.add(lit);
			}
		}
		return labels;
	}

}
