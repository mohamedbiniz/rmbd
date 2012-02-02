package at.ainf.owlcontroller;

import at.ainf.owlapi3.model.OWLTheory;
import at.ainf.theory.model.InconsistentTheoryException;
import at.ainf.theory.model.SolverException;
import at.ainf.theory.storage.AxiomSet;
import at.ainf.theory.storage.AxiomSetFactory;
import at.ainf.theory.storage.AxiomSetImpl;
import at.ainf.theory.watchedset.WatchedTreeSet;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static junit.framework.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 19.01.12
 * Time: 13:24
 * To change this template use File | Settings | File Templates.
 */
public class TestWatchedTreeSet {

    private static Logger logger = Logger.getLogger(TestWatchedTreeSet.class.getName());

     @BeforeClass
     public static void setUp() {
         String conf = ClassLoader.getSystemResource("owlcontroller-log4j.properties").getFile();
         PropertyConfigurator.configure(conf);
     }

    private OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

    private AxiomSet<OWLLogicalAxiom> createSet(String name, double measure, OWLLogicalAxiom axiom) {
        Set<OWLLogicalAxiom> set = new HashSet<OWLLogicalAxiom>();
        set.add(axiom);

        //return new AxiomSetImpl<OWLLogicalAxiom>(AxiomSet.TypeOfSet.HITTING_SET,name, measure,set, Collections.<OWLLogicalAxiom>emptySet());
        return AxiomSetFactory.createAxiomSet(AxiomSet.TypeOfSet.HITTING_SET,measure,set, Collections.<OWLLogicalAxiom>emptySet());
    }

    @Test
    public void testSet() throws OWLOntologyCreationException, SolverException, InconsistentTheoryException {

        OWLTheory theory = Utils.loadTheory(manager, "queryontologies/koala.owl");
        ArrayList<OWLLogicalAxiom> list = new ArrayList<OWLLogicalAxiom>(theory.getActiveFormulas());
        Collections.sort(list);
        WatchedTreeSet<AxiomSet<OWLLogicalAxiom>,Double> set = new WatchedTreeSet<AxiomSet<OWLLogicalAxiom>, Double>();

        AxiomSet<OWLLogicalAxiom> modAxiomSet = null;
        for (int i = 1; i < 10; i++) {
            AxiomSet<OWLLogicalAxiom> s = createSet("set_" + i, i/10.0, list.get(i));
            set.add(s);
            if (i == 6)
                modAxiomSet = s;

        }

        modAxiomSet.setMeasure(0.35);
        boolean removed = set.remove(modAxiomSet);

        assert (removed);


    }

}
