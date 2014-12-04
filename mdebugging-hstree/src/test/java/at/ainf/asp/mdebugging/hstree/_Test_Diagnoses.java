package at.ainf.asp.mdebugging.hstree;

/**
 * When executing all test at all, there are incorrect solutions.
 * Therefore, tests should be done separately.
 */

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Set;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.Ignore;
import org.junit.Test;

import at.ainf.asp.antlr.ASPProgramLexer;
import at.ainf.asp.antlr.ASPProgramParser;
import at.ainf.asp.mdebugging.hstree.ioactions.ASPConverter;
import at.ainf.asp.mdebugging.hstree.model.ASPModel;
import at.ainf.asp.mdebugging.hstree.model.ASPTheory;
import at.ainf.asp.mdebugging.hstree.model.IProgramElement;
import at.ainf.asp.mdebugging.hstree.model.ProgramListener;
import at.ainf.asp.mdebugging.hstree.model.ReasonerASP;
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
 * @author Melanie Frühstück
 *
 */
public class _Test_Diagnoses {

	@Ignore @Test
	public void testHouseExample() throws IOException {
		ASPConverter converter = new ASPConverter();
		String filePath = ClassLoader.getSystemResource("house_p02t002.lp").getPath();
		String src = converter.convertFromFileToString(filePath);
		
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
		assertEquals(1, i);
		
		for (FormulaSet<IProgramElement> fs : diagnosis) {
			System.out.println("\nDiagnosis " + j + ":");
			for (IProgramElement pe : fs) {
				System.out.println(pe.getString());
			}
			j++;
		}
		assertEquals(4, j);
	}
	
	@Ignore @Test
	public void testPPMExample() throws IOException {
		ASPConverter converter = new ASPConverter();
		String filePath = ClassLoader.getSystemResource("ppm_t05p03.lp").getPath();
		String src = converter.convertFromFileToString(filePath);
		
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
		assertEquals(1, i);
		
		for (FormulaSet<IProgramElement> fs : diagnosis) {
			System.out.println("\nDiagnosis " + j + ":");
			for (IProgramElement pe : fs) {
				System.out.println(pe.getString());
			}
			j++;
		}
		assertEquals(5, j);
	}
	
	@Test
	public void testPUPExample() throws IOException {
		ASPConverter converter = new ASPConverter();
		String filePath = ClassLoader.getSystemResource("pup_grid01.lp").getPath();
		String src = converter.convertFromFileToString(filePath);
		
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
		assertEquals(1, i);
		
		for (FormulaSet<IProgramElement> fs : diagnosis) {
			System.out.println("\nDiagnosis " + j + ":");
			for (IProgramElement pe : fs) {
				System.out.println(pe.getString());
			}
			j++;
		}
		assertEquals(1, j);
	}

}
