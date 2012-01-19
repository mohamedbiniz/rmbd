package at.ainf.owlcontroller;

import at.ainf.owlapi3.model.OWLTheory;
import at.ainf.theory.model.InconsistentTheoryException;
import at.ainf.theory.model.SolverException;
import org.junit.Test;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: kostya
 * Date: 17.01.2010
 * Time: 12:59:14
 * To change this template use File | Settings | File Templates.
 */
public class TheoryTest {

    public OWLOntologyManager getManager() {
        return manager;
    }

    private OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

    public List<OWLLogicalAxiom> negateFormulas(Set<OWLLogicalAxiom> ntest, OWLTheory th) {
        List<OWLLogicalAxiom> negated = new LinkedList<OWLLogicalAxiom>();
        for (OWLLogicalAxiom test : ntest)
            negated.add(th.negate(test));
        return negated;
    }

    @Test
    public void testTheory() throws OWLOntologyCreationException, URISyntaxException, SolverException,
            OWLOntologyChangeException, OWLOntologyStorageException, IOException, InconsistentTheoryException {

        URL path = ClassLoader.getSystemResource("ontologies/ecai.1.owl");
        OWLTheory th = createTheory(getManager().loadOntologyFromOntologyDocument(path.openStream()));

        Collection<OWLLogicalAxiom> res = negateFormulas(th.getActiveFormulas(), th);
        OWLDataFactory dataFactory = manager.getOWLDataFactory();
        String str = "http://negation.ainf.at/";

        Set<OWLClassExpression> set = new TreeSet<OWLClassExpression>();
        set.add(createClass(dataFactory, str + "c2"));
        set.add(dataFactory.getOWLObjectComplementOf(createClass(dataFactory, str + "c3")));

        Set<OWLClassExpression> set1 = new TreeSet<OWLClassExpression>();
        set1.add(createClass(dataFactory, str + "c4"));
        set1.add(dataFactory.getOWLObjectComplementOf(createClass(dataFactory, str + "c5")));
        set.add(dataFactory.getOWLObjectUnionOf(set1));

        OWLEquivalentClassesAxiom axiom = dataFactory.getOWLEquivalentClassesAxiom(createClass(dataFactory, str + "c1"),
                dataFactory.getOWLObjectIntersectionOf(set));
        res.add(th.negate(axiom));
        //res.add(axiom);

        OWLOntology neg = manager.createOntology(IRI.create(str));
        manager.addAxioms(neg, new TreeSet<OWLLogicalAxiom>(res));
        String outPath = ClassLoader.getSystemResource("ontologies").getPath() + "/ecai.1.neg.owl";
        File ont = new File(outPath);
        ont.createNewFile();
        manager.saveOntology(neg, new FileOutputStream(ont));
    }

    private OWLClass createClass(OWLDataFactory dataFactory, String str) {
        return dataFactory.getOWLClass(IRI.create(str));
    }

    public OWLTheory createTheory(OWLOntology ontology) throws SolverException, InconsistentTheoryException {
        Set<OWLLogicalAxiom> bax = new HashSet<OWLLogicalAxiom>();
        for (OWLIndividual ind : ontology.getIndividualsInSignature()) {
            bax.addAll(ontology.getClassAssertionAxioms(ind));
            bax.addAll(ontology.getObjectPropertyAssertionAxioms(ind));
        }

        OWLReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory();
        OWLTheory theory = new OWLTheory(reasonerFactory, ontology, bax);
        assertTrue(theory.isConsistent());

        return theory;
    }

}
