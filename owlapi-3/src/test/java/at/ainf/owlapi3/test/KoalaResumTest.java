package at.ainf.owlapi3.test;

import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.quickxplain.QuickXplain;
import at.ainf.diagnosis.storage.FormulaSet;
import at.ainf.diagnosis.tree.AbstractTreeSearch;
import at.ainf.diagnosis.tree.BinaryTreeSearch;
import at.ainf.diagnosis.tree.HsTreeSearch;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.diagnosis.tree.searchstrategy.UniformCostSearchStrategy;
import at.ainf.owlapi3.costestimation.OWLAxiomKeywordCostsEstimator;
import at.ainf.owlapi3.model.OWLTheory;
import at.ainf.owlapi3.parser.MyOWLRendererParser;
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOWLObjectRendererImpl;

import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 29.11.12
 * Time: 12:07
 * To change this template use File | Settings | File Templates.
 */
public class  KoalaResumTest {

    private OWLLogicalAxiom getAxiom(Set<FormulaSet<OWLLogicalAxiom>> result, int hs, int axiom) {
        return ((OWLLogicalAxiom)((FormulaSet<OWLLogicalAxiom>)result.toArray()[hs]).toArray()[axiom]);
    }

    private String renderAxiom(OWLLogicalAxiom axiom) {
        return new ManchesterOWLSyntaxOWLObjectRendererImpl().render(axiom);
    }


    @Test
    public void searchKoalaTest() throws OWLOntologyCreationException, SolverException, InconsistentTheoryException {

        //Binary Tree
        BinaryTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = new BinaryTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom>();
        InputStream koalaStream = ClassLoader.getSystemResourceAsStream("ontologies/koala.owl");
        OWLOntology ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(koalaStream);
        Set<OWLLogicalAxiom> bax = new LinkedHashSet<OWLLogicalAxiom>();
        for (OWLIndividual ind : ontology.getIndividualsInSignature()) {
            bax.addAll(ontology.getClassAssertionAxioms(ind));
            bax.addAll(ontology.getObjectPropertyAssertionAxioms(ind));
        }
        OWLTheory theory = new OWLTheory(new Reasoner.ReasonerFactory(), ontology, bax);
        search.setSearchStrategy(new UniformCostSearchStrategy<OWLLogicalAxiom>());
        search.setSearcher(new QuickXplain<OWLLogicalAxiom>());
        search.setCostsEstimator(new OWLAxiomKeywordCostsEstimator(theory));
        search.setSearchable(theory);
           long binaryStart=System.currentTimeMillis();
        Set<FormulaSet<OWLLogicalAxiom>> resultBin = performSearch(search,ontology, theory);
        long binaryEnd=System.currentTimeMillis();
        long binaryTime=binaryEnd-binaryStart;



        //HS tree
        /*  HsTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom> search2 = new HsTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom>();
        InputStream koalaStream2 = ClassLoader.getSystemResourceAsStream("ontologies/koala.owl");
        OWLOntology ontology2 = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(koalaStream2);
        Set<OWLLogicalAxiom> bax2 = new LinkedHashSet<OWLLogicalAxiom>();
        for (OWLIndividual ind : ontology2.getIndividualsInSignature()) {
            bax2.addAll(ontology2.getClassAssertionAxioms(ind));
            bax2.addAll(ontology2.getObjectPropertyAssertionAxioms(ind));
        }
        OWLTheory theory2 = new OWLTheory(new Reasoner.ReasonerFactory(), ontology2, bax2);
        search2.setSearchStrategy(new UniformCostSearchStrategy<OWLLogicalAxiom>());
        search2.setSearcher(new QuickXplain<OWLLogicalAxiom>());
        search2.setCostsEstimator(new OWLAxiomKeywordCostsEstimator(theory2));
        search2.setSearchable(theory2);
        long hsStart=System.currentTimeMillis();
        Set<FormulaSet<OWLLogicalAxiom>> resultHs = performSearch(search2,ontology2, theory2);
        long hsEnd=System.currentTimeMillis();
        long hsTime=hsEnd-hsStart;
                                     */


      // System.out.println("HS: "+ hsTime);
        System.out.println("Binary: "+ binaryTime);

        //assertTrue(resultBin.equals(resultHs));

    }



    public Set<FormulaSet<OWLLogicalAxiom>> performSearch(AbstractTreeSearch search, OWLOntology ontology, OWLTheory theory) throws OWLOntologyCreationException, SolverException, InconsistentTheoryException {


        try {
            search.setMaxDiagnosesNumber(3);
            search.start();
        } catch (NoConflictException e) {

        }

        Set<FormulaSet<OWLLogicalAxiom>> result = new LinkedHashSet<FormulaSet<OWLLogicalAxiom>>(search.getDiagnoses());


        assertTrue(result.size() == 3);
        assertTrue(renderAxiom(getAxiom(result,0,0)).equals("Marsupials DisjointWith Person"));
        assertTrue(renderAxiom(getAxiom(result,1,0)).equals("KoalaWithPhD EquivalentTo Koala and (hasDegree value PhD)"));
        assertTrue(renderAxiom(getAxiom(result,1,1)).equals("isHardWorking Domain Person"));
        assertTrue(renderAxiom(getAxiom(result,2,0)).equals("Koala SubClassOf Marsupials"));
        assertTrue(renderAxiom(getAxiom(result,2,1)).equals("Quokka SubClassOf isHardWorking value true"));

        OWLLogicalAxiom testcase = new MyOWLRendererParser(ontology).parse("Marsupials DisjointWith Person");

        //New testcase
       // OWLLogicalAxiom testcase = new MyOWLRendererParser(ontology).parse("KoalaWithPhD EquivalentTo Koala and (hasDegree value PhD)");
        theory.getKnowledgeBase().addEntailedTest(Collections.singleton(testcase));

        try {
            search.setMaxDiagnosesNumber(3);
            search.start();
        } catch (NoConflictException e) {

        }

        result = new LinkedHashSet<FormulaSet<OWLLogicalAxiom>>(search.getDiagnoses());

        /**assertTrue(result.size() == 3);
        assertTrue(renderAxiom(getAxiom(result,0,0)).equals("KoalaWithPhD EquivalentTo Koala and (hasDegree value PhD)"));
        assertTrue(renderAxiom(getAxiom(result,0,1)).equals("isHardWorking Domain Person"));
        assertTrue(renderAxiom(getAxiom(result,1,0)).equals("Koala SubClassOf Marsupials"));
        assertTrue(renderAxiom(getAxiom(result,1,1)).equals("Quokka SubClassOf isHardWorking value true"));
        assertTrue(renderAxiom(getAxiom(result,2,0)).equals("Koala SubClassOf Marsupials"));
        assertTrue(renderAxiom(getAxiom(result,2,1)).equals("isHardWorking Domain Person"));**/
          return result;
    }


}
