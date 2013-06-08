package at.ainf.asp;

import java.util.*;

import at.ainf.asp.inputoutputactions.ASPConverter;
import at.ainf.asp.model.ASPModel;
import at.ainf.asp.model.ASPTheory;
import at.ainf.asp.model.IProgramElement;
import at.ainf.asp.model.ProgramListener;
import at.ainf.asp.model.ReasonerASP;
import junit.framework.Assert;
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
	public void testHsTree() {

        ASPModel model = createAspModel(filePathUNSAT);
        
//        ReasonerASP reasoner = new ReasonerASP();
//        boolean consistent = reasoner.isConsistent();
//        System.out.println("is consistent: " + consistent);

        ASPTheory theory = createAspTheory(model);

        HsTreeSearch<FormulaSet<IProgramElement>, IProgramElement> search = createTreeSearch(theory);
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
        Assert.assertTrue(conflicts.size() == 1);
        Assert.assertTrue(diagnosis.size() == 1);
        Assert.assertTrue(conflicts.iterator().next().size() == 1);
        Assert.assertTrue(diagnosis.iterator().next().size() == 1);
        Assert.assertTrue(conflicts.iterator().next().iterator().next().getString().equals(":- pc(M)."));
        Assert.assertTrue(diagnosis.iterator().next().iterator().next().getString().equals(":- pc(M)."));
        printFormularSets(conflicts, "Conflict ");
        printFormularSets(diagnosis, "Diagnosis ");

//        QuickXplain<IProgramElement> qxp = new QuickXplain<IProgramElement>();
////			Set<FormulaSet<IProgramElement>> conflict = qxp.search(theory, model.getRules(), null);

	}

    protected ASPTheory createAspTheory(ASPModel model) {
        ASPTheory theory = new ASPTheory();
        ReasonerASP reasoner = new ReasonerASP();
        theory.setReasoner(reasoner);
        KnowledgeBase<IProgramElement> knowledgeBase = new KnowledgeBase<IProgramElement>();
        knowledgeBase.setBackgroundFormulas(model.getFacts());
        theory.setKnowledgeBase(knowledgeBase);
        theory.getKnowledgeBase().addFormulas(model.getProgramElements());
        return theory;
    }

    protected ASPModel createAspModel(String file) {
        ASPConverter converter = new ASPConverter();
        String src = converter.convertFromFileToString(file);
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
        return model;
    }

    @Ignore  @Test
    public void testQuickx() throws NoConflictException, InconsistentTheoryException, SolverException {

        ASPModel model = createAspModel(filePathUNSAT);
        ASPTheory theory = createAspTheory(model);


        QuickXplain<IProgramElement> qxp = new QuickXplain<IProgramElement>();
			Set<FormulaSet<IProgramElement>> conflict =
                    qxp.search(theory, model.getRules(), Collections.<IProgramElement>emptySet());

        List<IProgramElement> possibleFaultyRules = new LinkedList<IProgramElement>(model.getRules());
        Assert.assertTrue(possibleFaultyRules.get(0).getString().equals("conflict_of_interest(M,P):- bid(M,P,0)."));
        Assert.assertTrue(possibleFaultyRules.get(1).getString().equals("conflict_of_interest(M,P):- pc(M), paper(P), author(M,P)."));
        Assert.assertTrue(possibleFaultyRules.get(2).getString().equals("bid(M,P,0):- pc(M), paper(P), conflict_of_interest(M,P)."));
        Assert.assertTrue(possibleFaultyRules.get(3).getString().equals(":- assigned(P,M), bid(M,P,0)."));
        Assert.assertTrue(possibleFaultyRules.get(4).getString().equals(":- pc(M)."));

        Assert.assertTrue(conflict.size() == 1);
        Assert.assertTrue(conflict.iterator().next().size() == 1);
        Assert.assertTrue(conflict.iterator().next().iterator().next().getString().equals(":- pc(M)."));


    }

    private HsTreeSearch<FormulaSet<IProgramElement>, IProgramElement> createTreeSearch(ASPTheory theory) {
        HsTreeSearch<FormulaSet<IProgramElement>,IProgramElement> search = new HsTreeSearch<FormulaSet<IProgramElement>,IProgramElement>();
        search.setSearchStrategy(new BreadthFirstSearchStrategy<IProgramElement>());
        search.setSearcher(new QuickXplain<IProgramElement>());
        search.setCostsEstimator(new SimpleCostsEstimator<IProgramElement>());
        search.setSearchable(theory);
        return search;
    }

    private void printFormularSets(Set<FormulaSet<IProgramElement>> diagnosis, String namePr) {
        int j = 0;
        for (FormulaSet<IProgramElement> fs : diagnosis) {
            logger.info(namePr + j + ":");
            for (IProgramElement pe : fs) {
                logger.info(pe.getString());
            }
            j++;
        }
    }

}
