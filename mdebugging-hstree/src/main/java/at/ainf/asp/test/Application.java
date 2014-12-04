package at.ainf.asp.test;

import java.util.Set;

import at.ainf.asp.inputoutputactions.ASPConverter;
import at.ainf.asp.model.ASPModel;
import at.ainf.asp.model.ASPTheory;
import at.ainf.asp.model.IProgramElement;
import at.ainf.asp.model.ProgramListener;
import at.ainf.asp.model.ReasonerASP;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import at.ainf.asp.antlr.ASPProgramLexer;
import at.ainf.asp.antlr.ASPProgramParser;
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
	private static String fileToDebug = System.getProperty("user.dir") + "/src/test/modv1_debugwithoutnot.lp";
//	private static String fileToDebug = "";
	
	public static boolean enableInfo = true;
	// INFO: if you want to set the gringo and clasp path manually, set pathSet to true 
	public static boolean pathSet = false;
	public static String claspPath = "";
	public static String gringoPath = "";
	
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
		} else if (args.length==6) {
			// -gringo /usr/bin/gringo -clasp /usr/bin/clasp -file example.lp
			boolean gringo = args[0].equals("-gringo") || args[0].equals("-g");
			boolean clasp = args[2].equals("-clasp") || args[2].equals("-c");
			boolean file = args[4].equals("-file") || args[4].equals("-f");
			if (gringo && clasp && file) {
				gringoPath = args[1];
				claspPath = args[3];
				fileToDebug = args[5];
			} else {
				System.out.println(falseParameterMsg);
				System.exit(1);
			}
				
		} else if (args.length==7) {
			// -i -g /usr/bin/gringo -c /usr/bin/clasp -f example.lp
			boolean info = args[0].equals("-enableInfo") || args[0].equals("-i");
			boolean gringo = args[1].equals("-gringo") || args[1].equals("-g");
			boolean clasp = args[3].equals("-clasp") || args[3].equals("-c");
			boolean file = args[5].equals("-file") || args[5].equals("-f");
			if (info && gringo && clasp && file) {
				enableInfo = true;
				gringoPath = args[2];
				claspPath = args[4];
				fileToDebug = args[6];
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
		System.out.println("To debug a monotone answer set program use the following parameter:" +
				"\n [-file] or [-f] followed by the file to debug." +
				"\n I.e. java -jar aspmodule.jar -file example.lp" +
				"" +
				"\n\n To show more information about the debugging process use the following parameters:" +
				"\n [-enableInfo] or [-i] followed by [-file] or [-f] followed by the file to debug." +
				"\n I.e. java -jar aspmodule.jar -enableInfo -file example.lp" +
				"" +
				"\n\n Additional information:" +
				"\n Unix: Make sure that gringo and clasp (POTASSCO) are in the path." +
				"\n Windows: Make sure that gringo and clasp are in the current directory." +
				"" +
				"\n\n Otherwise there is the possibility to set the path of the two programs:" +
				"\n [-gringo] or [-g] followed by the gringo path and [-clasp] or [-c] followed by the clasp path." +
				"\n I.e. java -jar aspmodule.jar -gringo /usr/bin/gringo -clasp /usr/bin/clasp -file example.lp or" +
				"\n i.e. java -jar aspmodule.jar -i -g /usr/bin/gringo -c /usr/bin/clasp -f example.lp");
		
	}

}
