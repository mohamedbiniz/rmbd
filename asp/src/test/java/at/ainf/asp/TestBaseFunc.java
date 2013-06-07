package at.ainf.asp;

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
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Melanie Fruehstueck
 *
 */
public class TestBaseFunc {


    private static Logger logger = LoggerFactory.getLogger(TestBaseFunc.class.getName());


	final private static String filePathSAT = ClassLoader.getSystemResource("test_SAT.lp").getPath();
//	final private static String filePathUNSAT = ClassLoader.getSystemResource("test_UNSAT.lp").getPath();
	final private static String filePathUNSAT = ClassLoader.getSystemResource("test1_UNSAT.lp").getPath();
	
	@Ignore  @Test
	public void testEasy() {
		
		ASPConverter converter = new ASPConverter();
		String src = converter.convertFromFileToString(filePathUNSAT);
//		String src = converter.convertFromFileToString(filePathSAT);
		
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
        
        logger.info("Parsing OK");
        
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
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (NoConflictException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InconsistentTheoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        
        Set<FormulaSet<IProgramElement>> conflicts = search.getConflicts();
        Set<FormulaSet<IProgramElement>> diagnosis = search.getDiagnoses();
        int i = 0;
        int j = 0;
		for (FormulaSet<IProgramElement> fs : conflicts) {
			logger.info("Conflicts " + i + ":");
			for (IProgramElement pe : fs) {
				logger.info(pe.getString());
			}
			i++;
		}
		for (FormulaSet<IProgramElement> fs : diagnosis) {
			logger.info("Diagnosis " + j + ":");
			for (IProgramElement pe : fs) {
				logger.info(pe.getString());
			}
			j++;
		}
        
//        QuickXplain<IProgramElement> qxp = new QuickXplain<IProgramElement>();
//        try {
////			Set<FormulaSet<IProgramElement>> conflict = qxp.search(theory, model.getRules(), null);
//			int i = 0;
//			for (FormulaSet<IProgramElement> fs : conflicts) {
//				System.out.println("Conflicts " + i + ":");
//				for (IProgramElement pe : fs) {
//					System.out.println(pe.getString());
//				}
//				i++;
//			}
//			for (FormulaSet<IProgramElement> fs : diagnosis) {
//				System.out.println("Diagnosis " + i + ":");
//				for (IProgramElement pe : fs) {
//					System.out.println(pe.getString());
//				}
//				i++;
//			}
//		} catch (SolverException e) {
//			System.out.println("Solving did fail: " + e);
//		} catch (NoConflictException e) {
//			System.out.println("No conflict was found: " + e);
//			e.printStackTrace();
//		} catch (InconsistentTheoryException e) {
//			System.out.println("Theory is inconsistent: " + e);
//			e.printStackTrace();
//		}
	}

}
