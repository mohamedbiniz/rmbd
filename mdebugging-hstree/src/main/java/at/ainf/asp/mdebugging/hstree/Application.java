package at.ainf.asp.mdebugging.hstree;

import java.util.Set;

import at.ainf.asp.antlr.ASPProgramLexer;
import at.ainf.asp.antlr.ASPProgramParser;
import at.ainf.asp.mdebugging.hstree.ioactions.ASPConverter;
import at.ainf.asp.mdebugging.hstree.model.ASPModel;
import at.ainf.asp.mdebugging.hstree.model.ASPTheory;
import at.ainf.asp.mdebugging.hstree.model.IProgramElement;
import at.ainf.asp.mdebugging.hstree.model.ProgramListener;
import at.ainf.asp.mdebugging.hstree.model.ReasonerASP;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.KnowledgeBase;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.quickxplain.QuickXplain;
import at.ainf.diagnosis.storage.FormulaSet;
import at.ainf.diagnosis.tree.HsTreeSearch;
import at.ainf.diagnosis.tree.SimpleCostsEstimator;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.diagnosis.tree.searchstrategy.BreadthFirstSearchStrategy;

/**
 * @author Melanie Fruehstueck
 *
 */
public class Application {

	private static final String falseParameterMsg = "Type in [-help] or [-h] to show possible parameters and how they are used.";
	// INFO: if module doesn't serve as a executable jar, the fileToDebug has to be set manually
	private static String fileToDebug = System.getProperty("user.dir") + "/src/test/house_p02t002.lp";
//	private static String fileToDebug = "";
	
	public static boolean enableInfo = false;
	// INFO: if you want to set clingo path manually, set pathSet to true 
	public static boolean pathSet = false;
//	public static String claspPath = "";
//	public static String gringoPath = "";
	public static String clingoPath = "";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// INFO: if you want to use the asp module as executable jar, uncomment the following comment
		handleCommandLine(args);
		
		ASPConverter converter = new ASPConverter();
		String src = converter.convertFromFileToString(fileToDebug);
		
		// do the parsing
		ANTLRInputStream input = null;
		input = new ANTLRInputStream(src);
        ASPProgramLexer lexer = new ASPProgramLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ASPProgramParser parser = new ASPProgramParser(tokens);
        ParseTree tree = parser.prog();
        ProgramListener pl = new ProgramListener();
        ParseTreeWalker.DEFAULT.walk(pl, tree);
        
//       	parser.setBuildParseTree(true);
//       	ParserRuleContext tree = parser.prog();
//       	tree.inspect(parser);    
        
        // fill the asp model with rules
        ASPModel model = ASPModel.getASPModelInstance();
        for (IProgramElement r : model.getRules()) {
        	model.addProgramElement(r);
        }
        
//        ReasonerASP reasoner = new ReasonerASP();
//        boolean consistent = reasoner.isConsistent();
//        System.out.println("is consistent: " + consistent);
        
        ASPTheory theory = new ASPTheory();
        ReasonerASP reasoner = new ReasonerASP();
        theory.setReasoner(reasoner);
        KnowledgeBase<IProgramElement> knowledgeBase = new KnowledgeBase<IProgramElement>();
        knowledgeBase.setBackgroundFormulas(model.getFacts());
        theory.setKnowledgeBase(knowledgeBase);
        theory.getKnowledgeBase().addFormulas(model.getProgramElements());
        
        HsTreeSearch<FormulaSet<IProgramElement>,IProgramElement> search = new HsTreeSearch<FormulaSet<IProgramElement>,IProgramElement>();

        // we want to use UniformCostSearch as our start strategy
        search.setSearchStrategy(new BreadthFirstSearchStrategy<IProgramElement>());

        // because we use Reiter's Tree nodes are conflicts which we start using QuickXplain
        search.setSearcher(new QuickXplain<IProgramElement>());

        // because we use UniformCostSearch we have to give a cost estimator to the start
        search.setCostsEstimator(new SimpleCostsEstimator<IProgramElement>());

        // at last we combine theory with start and get our ready to use object
        search.setSearchable(theory);
        try {
			search.start();
		} catch (SolverException e1) {
			e1.printStackTrace();
		} catch (NoConflictException e1) {
			e1.printStackTrace();
		} catch (InconsistentTheoryException e1) {
			e1.printStackTrace();
		}
        
        Set<FormulaSet<IProgramElement>> conflicts = search.getConflicts();
        Set<FormulaSet<IProgramElement>> diagnosis = search.getDiagnoses();
        int i = 0;
        int j = 0;
		for (FormulaSet<IProgramElement> fs : conflicts) {
			System.out.println("\nConflicts " + i + ":");
			for (IProgramElement pe : fs) {
				System.out.println(pe.getString());
			}
			i++;
		}
		for (FormulaSet<IProgramElement> fs : diagnosis) {
			System.out.println("\nDiagnosis " + j + ":");
			for (IProgramElement pe : fs) {
				System.out.println(pe.getString());
			}
			j++;
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
		} else if (args.length==2) {
			if (args[0].equals("-file") || args[0].equals("-f")) {
				fileToDebug = dir + args[1];
			} else {
				System.out.println(falseParameterMsg);
				System.exit(1);
			}
		} else if (args.length==3) {
			boolean info = args[0].equals("-enableInfo") || args[0].equals("-i");
			boolean file = args[1].equals("-file") || args[1].equals("-f");
			if (info && file) {
				enableInfo = true;
				fileToDebug = dir + args[2];
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
				
		} else if (args.length==5) {
			// -i -c /usr/bin/clingo -f example.lp
			boolean info = args[0].equals("-enableInfo") || args[0].equals("-i");
			boolean clingo = args[1].equals("-clingo") || args[1].equals("-c");
			boolean file = args[3].equals("-file") || args[3].equals("-f");
			if (info && clingo && file) {
				enableInfo = true;
				clingoPath = args[2];
				pathSet = true;
				fileToDebug = args[4];
			} else {
				System.out.println(falseParameterMsg);
				System.exit(1);
			}
		} else {
			System.out.println(falseParameterMsg);
			System.exit(1);
		}
	}
	
	/**
	 * 
	 */
	private static void printHelp() {
		System.out.println(" Make sure that clingo is in your path (for UNIX) or in the current directory (for WINDOWS)." + 
				"\n Otherwise it is possible to set the path manually (see below)." +
				"\n To debug a monotone answer set program use the following parameter:" +
				"\n [-file] or [-f] followed by the file to debug." +
				"\n E.g. java -jar monotonic-asp-hstree-1.0.jar -file example.lp" +
				"" +
				"\n\n To show more information about the debugging process use the following parameters:" +
				"\n [-enableInfo] or [-i] followed by [-file] or [-f] followed by the file to debug." +
				"\n E.g. java -jar monotonic-asp-hstree-1.0.jar -enableInfo -f example.lp" +
				"" +
				"\n\n There is the possibility to set the path of the program:" +
				"\n [-clingo] or [-c] followed by the clingo path." +
				"\n E.g. java -jar monotonic-asp-hstree-1.0.jar -clingo /usr/bin/clingo -file example.lp or" +
				"\n E.g. java -jar monotonic-asp-hstree-1.0.jar -i -c /usr/bin/clingo -f example.lp");
		
	}

}
