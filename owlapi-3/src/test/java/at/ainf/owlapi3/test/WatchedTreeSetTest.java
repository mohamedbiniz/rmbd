package at.ainf.owlapi3.test;

import at.ainf.diagnosis.storage.FormulaSetImpl;
import at.ainf.diagnosis.storage.FormulaSet;
import at.ainf.owlapi3.base.CalculateDiagnoses;
import at.ainf.owlapi3.model.OWLTheory;
import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.watchedset.WatchedTreeSet;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 19.01.12
 * Time: 13:24
 * To change this template use File | Settings | File Templates.
 */
public class WatchedTreeSetTest {

    private static Logger logger = LoggerFactory.getLogger(WatchedTreeSetTest.class.getName());

     /*@BeforeClass
     public static void setUp() {
         String conf = ClassLoader.getSystemResource("owlapi3-log4j.properties").getFile();
         PropertyConfigurator.configure(conf); }*/

    private OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

    private FormulaSet<OWLLogicalAxiom> createSet(String name, double measure, OWLLogicalAxiom axiom) {
        Set<OWLLogicalAxiom> set = new HashSet<OWLLogicalAxiom>();
        set.add(axiom);

        //return new AxiomSetImpl<OWLLogicalAxiom>(AxiomSet.TypeOfSet.HITTING_SET,name, measure,set, Collections.<OWLLogicalAxiom>emptySet());
        return new FormulaSetImpl<OWLLogicalAxiom>(BigDecimal.valueOf(measure), set, Collections.<OWLLogicalAxiom>emptySet() );
    }

    @Test
    public void testSet() throws OWLOntologyCreationException, SolverException, InconsistentTheoryException {

        OWLTheory theory = new CalculateDiagnoses().getSimpleTheory(new CalculateDiagnoses().getOntologySimple("ontologies/koala.owl"), false);
        ArrayList<OWLLogicalAxiom> list = new ArrayList<OWLLogicalAxiom>(theory.getKnowledgeBase().getFaultyFormulas());
        Collections.sort(list);
        WatchedTreeSet<FormulaSet<OWLLogicalAxiom>,BigDecimal> set = new WatchedTreeSet<FormulaSet<OWLLogicalAxiom>, BigDecimal>();

        FormulaSet<OWLLogicalAxiom> modFormulaSet = null;
        for (int i = 1; i < 10; i++) {
            FormulaSet<OWLLogicalAxiom> s = createSet("set_" + i, i/10.0, list.get(i));
            set.add(s);
            if (i == 6)
                modFormulaSet = s;

        }

        modFormulaSet.setMeasure(new BigDecimal("0.35"));
        boolean removed = set.remove(modFormulaSet);

        assert (removed);


    }

}
