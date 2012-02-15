package at.ainf.owlcontroller;

import at.ainf.owlcontroller.parser.MyOWLRendererParser;
import at.ainf.theory.model.SolverException;
import at.ainf.theory.model.InconsistentTheoryException;
import at.ainf.diagnosis.quickxplain.NewQuickXplain;
import at.ainf.theory.storage.SimpleStorage;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.owlapi3.model.OWLTheory;
import at.ainf.diagnosis.tree.UniformCostSearch;
import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;

import static junit.framework.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 08.06.11
 * Time: 18:06
 * To change this template use File | Settings | File Templates.
 */
public class TestDiagnosisValidation {


    private static OWLOntology ontology;
    private static File file = new File(
            ClassLoader.getSystemResource("koala.owl").getFile());
    private static MyOWLRendererParser parser;
    private static Reasoner.ReasonerFactory reasonerFactory;
    private HashMap<ManchesterOWLSyntax, Double> map;
    private static LinkedHashSet<OWLLogicalAxiom> bax;

    @Before
    public void createMap() {
        map = new HashMap<ManchesterOWLSyntax, Double>();
        map.put(ManchesterOWLSyntax.SOME, 0.05);
        map.put(ManchesterOWLSyntax.ONLY, 0.05);
        map.put(ManchesterOWLSyntax.AND, 0.001);
        map.put(ManchesterOWLSyntax.OR, 0.001);
        map.put(ManchesterOWLSyntax.NOT, 0.01);
        map.put(ManchesterOWLSyntax.SUBCLASS_OF, 0.01);


        ManchesterOWLSyntax[] keywords = {ManchesterOWLSyntax.MIN,
                ManchesterOWLSyntax.MAX,
                ManchesterOWLSyntax.EXACTLY,

                ManchesterOWLSyntax.VALUE,

                ManchesterOWLSyntax.INVERSE,
                ManchesterOWLSyntax.SUBCLASS_OF,
                ManchesterOWLSyntax.EQUIVALENT_TO,
                ManchesterOWLSyntax.DISJOINT_WITH,
                ManchesterOWLSyntax.INVERSE_OF,
                ManchesterOWLSyntax.SUB_PROPERTY_OF,
                ManchesterOWLSyntax.SAME_AS,
                ManchesterOWLSyntax.DIFFERENT_FROM,
                ManchesterOWLSyntax.RANGE,
                ManchesterOWLSyntax.DOMAIN,
                ManchesterOWLSyntax.TYPE
        };


        for (ManchesterOWLSyntax keyword : keywords) {
            map.put(keyword, 0.02);
        }
    }

    @Test
    public void testDiagnosisValidation()
            throws InconsistentTheoryException, SolverException, NoConflictException, OWLOntologyCreationException {
        createOntology();
        SimpleStorage<OWLLogicalAxiom> storage = new SimpleStorage<OWLLogicalAxiom>();
        UniformCostSearch<OWLLogicalAxiom> search = new UniformCostSearch<OWLLogicalAxiom>(storage);

        search.setSearcher(new NewQuickXplain<OWLLogicalAxiom>());
        OWLTheory theory = new OWLTheory(reasonerFactory, ontology, bax);
        search.setNodeCostsEstimator(new OWLAxiomNodeCostsEstimator(theory));
        search.setTheory(theory);
        search.setMaxHittingSets(0);

        HashSet<OWLLogicalAxiom> tc = new HashSet<OWLLogicalAxiom>();
        tc.add(parser.parse("KoalaWithPhD SubClassOf Koala"));
        tc.add(parser.parse("GraduateStudent DisjointWith KoalaWithPhD"));
        tc.add(parser.parse("GraduateStudent DisjointWith Marsupials"));
        tc.add(parser.parse("GraduateStudent DisjointWith TasmanianDevil"));
        tc.add(parser.parse("KoalaWithPhD DisjointWith MaleStudentWith3Daughters"));
        tc.add(parser.parse("KoalaWithPhD DisjointWith Marsupials"));
        tc.add(parser.parse("KoalaWithPhD DisjointWith Quokka"));
        tc.add(parser.parse("KoalaWithPhD DisjointWith Student"));
        tc.add(parser.parse("KoalaWithPhD DisjointWith TasmanianDevil"));
        tc.add(parser.parse("MaleStudentWith3Daughters DisjointWith Marsupials"));
        tc.add(parser.parse("MaleStudentWith3Daughters DisjointWith TasmanianDevil"));
        tc.add(parser.parse("Marsupials DisjointWith Student"));
        tc.add(parser.parse("Person DisjointWith TasmanianDevil"));
        tc.add(parser.parse("Student DisjointWith TasmanianDevil"));

        //theory.addEntailedTest(tc);

        HashSet<OWLLogicalAxiom> tc1 = new HashSet<OWLLogicalAxiom>();
        tc1.add(parser.parse("Koala SubClassOf Person"));
        tc1.add(parser.parse("GraduateStudent DisjointWith Quokka"));
        tc1.add(parser.parse("Koala DisjointWith Marsupials"));
        tc1.add(parser.parse("Koala DisjointWith TasmanianDevil"));
        tc1.add(parser.parse("MaleStudentWith3Daughters DisjointWith Quokka"));
        tc1.add(parser.parse("Person DisjointWith Quokka"));
        tc1.add(parser.parse("Quokka DisjointWith Student"));

        //theory.addEntailedTest(tc1);

        HashSet<OWLLogicalAxiom> tc2 = new HashSet<OWLLogicalAxiom>();
        tc2.add(parser.parse("GraduateStudent DisjointWith Marsupials"));
        tc2.add(parser.parse("GraduateStudent DisjointWith Quokka"));
        tc2.add(parser.parse("GraduateStudent DisjointWith TasmanianDevil"));
        tc2.add(parser.parse("Koala DisjointWith Person"));
        tc2.add(parser.parse("MaleStudentWith3Daughters DisjointWith Marsupials"));
        tc2.add(parser.parse("MaleStudentWith3Daughters DisjointWith Quokka"));
        tc2.add(parser.parse("MaleStudentWith3Daughters DisjointWith TasmanianDevil"));
        tc2.add(parser.parse("Marsupials DisjointWith Student"));
        tc2.add(parser.parse("Person DisjointWith Quokka"));
        tc2.add(parser.parse("Person DisjointWith TasmanianDevil"));
        tc2.add(parser.parse("Quokka DisjointWith Student"));
        tc2.add(parser.parse("Student DisjointWith TasmanianDevil"));
        theory.addNonEntailedTest(tc2);

        HashSet<OWLLogicalAxiom> tc3 = new HashSet<OWLLogicalAxiom>();
        tc3.add(parser.parse("Koala SubClassOf Person"));
        tc3.add(parser.parse("Koala DisjointWith Marsupials"));
        tc3.add(parser.parse("Koala DisjointWith TasmanianDevil"));
        theory.addNonEntailedTest(tc3);

        HashSet<OWLLogicalAxiom> tc4 = new HashSet<OWLLogicalAxiom>();
        tc4.add(parser.parse("Koala SubClassOf Marsupials"));
        tc4.add(parser.parse("isHardWorking Domain Person"));

        assertTrue(theory.testDiagnosis(tc4));

    }

    public static void createOntology() throws OWLOntologyCreationException, InconsistentTheoryException, SolverException {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

        ontology = manager.loadOntologyFromOntologyDocument(file);

        bax = new LinkedHashSet<OWLLogicalAxiom>();

        for (OWLIndividual ind : ontology.getIndividualsInSignature()) {
            bax.addAll(ontology.getClassAssertionAxioms(ind));
            bax.addAll(ontology.getObjectPropertyAssertionAxioms(ind));
        }
        parser = new MyOWLRendererParser(ontology);

        reasonerFactory = new Reasoner.ReasonerFactory();
    }

}
