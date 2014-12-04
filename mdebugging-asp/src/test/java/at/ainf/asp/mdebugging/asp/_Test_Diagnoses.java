package at.ainf.asp.mdebugging.asp;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
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
import org.junit.Ignore;
import org.junit.Test;

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
public class _Test_Diagnoses {

	@Test
	public void testHouseExample() throws IOException {
		String filePath = ClassLoader.getSystemResource("house_p02t002.lp").getPath();
		String programString = File2StringHelper
				.convertFromFileToString(filePath);

		ANTLRInputStream programInput = new ANTLRInputStream(programString);
		ProgramLexer programLexer = new ProgramLexer(programInput);
		CommonTokenStream programTokens = new CommonTokenStream(programLexer);
		ProgramParser programParser = new ProgramParser(programTokens);
		ParseTree programTree = programParser.prog();

		ProgramListener pl = new ProgramListener();
		ParseTreeWalker.DEFAULT.walk(pl, programTree);

		Program program = pl.getProgram();
		assertEquals(7, program.getRules().size());
		assertEquals(16, program.getFacts().size());
		// add once the additional rules
		program.addAdditionalStatements();

		String fileNameExt = "testfiles/house_p02t002_ext";
		ExportHelper.export(program, fileNameExt + "0.lp");

		Solver solver = new Solver();
		solver.solve(fileNameExt + "0.lp");
		String rawAS = solver.getRawAnswerSets();

		// parse raw answer set and get labels
		List<AnswerSet> answerSets = parseAnswerSetOutput(rawAS);
		if (answerSets.size() >= 1 && answerSets.get(0).getAnswerSet() != "") {
//			System.out.println("AS size: " + answerSets.size());
		}
		AnswerSet as = answerSets.get(answerSets.size()-1);
		List<String> labels = extractLabelPredicate(as);
		Set<Rule> insertedRules = new HashSet<Rule>();
		int counter = 0;

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
			ExportHelper.export(program, fileNameExt + counter + ".lp");
			solver.solve(fileNameExt + counter + ".lp");
			
			// after adding constraints to program, solve again
			// to get next minimal diagnosis
			rawAS = solver.getRawAnswerSets();
			answerSets.clear();
			answerSets = parseAnswerSetOutput(rawAS);
//			if (answerSets.size() >= 1 && answerSets.get(0).getAnswerSet() != "") {
//				System.out.println("AS size: " + answerSets.size());
//			}
			if (!answerSets.isEmpty()) {
				as = answerSets.get(answerSets.size()-1);
			}
			labels.clear();
			labels = extractLabelPredicate(as);
			// parse raw answer set and get labels
		}
		// for this test case there are 4 diagnoses
		assertEquals(4, program.getLabelConstraints().size());
	}
	
	@Test
	public void testPPMExample() throws IOException {
		String filePath = ClassLoader.getSystemResource("ppm_t05p03.lp").getPath();
		String programString = File2StringHelper
				.convertFromFileToString(filePath);

		ANTLRInputStream programInput = new ANTLRInputStream(programString);
		ProgramLexer programLexer = new ProgramLexer(programInput);
		CommonTokenStream programTokens = new CommonTokenStream(programLexer);
		ProgramParser programParser = new ProgramParser(programTokens);
		ParseTree programTree = programParser.prog();

		ProgramListener pl = new ProgramListener();
		ParseTreeWalker.DEFAULT.walk(pl, programTree);

		Program program = pl.getProgram();
		assertEquals(5, program.getRules().size());
		assertEquals(10, program.getFacts().size());
		// add once the additional rules
		program.addAdditionalStatements();

		String fileNameExt = "testfiles/ppm_t05p03_ext";
		ExportHelper.export(program, fileNameExt + "0.lp");

		Solver solver = new Solver();
		solver.solve(fileNameExt + "0.lp");
		String rawAS = solver.getRawAnswerSets();

		// parse raw answer set and get labels
		List<AnswerSet> answerSets = parseAnswerSetOutput(rawAS);
//		if (answerSets.size() >= 1 && answerSets.get(0).getAnswerSet() != "") {
//			System.out.println("AS size: " + answerSets.size());
//		}
		AnswerSet as = answerSets.get(answerSets.size()-1);
		List<String> labels = extractLabelPredicate(as);
		Set<Rule> insertedRules = new HashSet<Rule>();
		int counter = 0;

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
			ExportHelper.export(program, fileNameExt + counter + ".lp");
			solver.solve(fileNameExt + counter + ".lp");
			
			// after adding constraints to program, solve again
			// to get next minimal diagnosis
			rawAS = solver.getRawAnswerSets();
			answerSets.clear();
			answerSets = parseAnswerSetOutput(rawAS);
//			if (answerSets.size() >= 1 && answerSets.get(0).getAnswerSet() != "") {
//				System.out.println("AS size: " + answerSets.size());
//			}
			if (!answerSets.isEmpty()) {
				as = answerSets.get(answerSets.size()-1);
			}
			labels.clear();
			labels = extractLabelPredicate(as);
			// parse raw answer set and get labels
		}
		assertEquals(5, program.getLabelConstraints().size());
	}
	
	@Test
	public void testPUPExample() throws IOException {
		String filePath = ClassLoader.getSystemResource("pup_grid01.lp").getPath();
		String programString = File2StringHelper
				.convertFromFileToString(filePath);

		ANTLRInputStream programInput = new ANTLRInputStream(programString);
		ProgramLexer programLexer = new ProgramLexer(programInput);
		CommonTokenStream programTokens = new CommonTokenStream(programLexer);
		ProgramParser programParser = new ProgramParser(programTokens);
		ParseTree programTree = programParser.prog();

		ProgramListener pl = new ProgramListener();
		ParseTreeWalker.DEFAULT.walk(pl, programTree);

		Program program = pl.getProgram();
		assertEquals(7, program.getRules().size());
		assertEquals(424, program.getFacts().size());
		// add once the additional rules
		program.addAdditionalStatements();

		String fileNameExt = "testfiles/pup_grid01_ext";
		ExportHelper.export(program, fileNameExt + "0.lp");

		Solver solver = new Solver();
		solver.solve(fileNameExt + "0.lp");
		String rawAS = solver.getRawAnswerSets();

		// parse raw answer set and get labels
		List<AnswerSet> answerSets = parseAnswerSetOutput(rawAS);
//		if (answerSets.size() >= 1 && answerSets.get(0).getAnswerSet() != "") {
//			System.out.println("AS size: " + answerSets.size());
//		}
		AnswerSet as = answerSets.get(answerSets.size()-1);
		List<String> labels = extractLabelPredicate(as);
		Set<Rule> insertedRules = new HashSet<Rule>();
		int counter = 0;

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
			ExportHelper.export(program, fileNameExt + counter + ".lp");
			solver.solve(fileNameExt + counter + ".lp");
			
			// after adding constraints to program, solve again
			// to get next minimal diagnosis
			rawAS = solver.getRawAnswerSets();
			answerSets.clear();
			answerSets = parseAnswerSetOutput(rawAS);
			if (answerSets.size() >= 1 && answerSets.get(0).getAnswerSet() != "") {
				System.out.println("AS size: " + answerSets.size());
			}
			if (!answerSets.isEmpty()) {
				as = answerSets.get(answerSets.size()-1);
			}
			labels.clear();
			labels = extractLabelPredicate(as);
			// parse raw answer set and get labels
		}
		assertEquals(1, program.getLabelConstraints().size());
	}

	private static void printDiagnosis(Program program, List<String> labels, int counter) {
		System.out.println("\nDiagnosis " + counter + ":");
		for (String l : labels) {
			System.out.println("Label: " + l);
			System.out.println("Rule: " + program.getRuleByLabel(l).getRule());
		}
	}

	private List<AnswerSet> parseAnswerSetOutput(String input) {
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
	
	private List<String> extractLabelPredicate(AnswerSet as) {
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
