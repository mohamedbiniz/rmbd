package at.ainf.owlcontroller.oaei11align;

import at.ainf.diagnosis.quickxplain.FastDiagnosis;
import at.ainf.diagnosis.quickxplain.NewQuickXplain;
import at.ainf.diagnosis.tree.BreadthFirstSearch;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.owlapi3.model.DualTreeOWLTheory;
import at.ainf.owlapi3.model.OWLIncoherencyExtractor;
import at.ainf.owlapi3.model.OWLTheory;
import at.ainf.owlcontroller.CreationUtils;
import at.ainf.owlcontroller.RDFUtils;
import at.ainf.theory.model.InconsistentTheoryException;
import at.ainf.theory.model.SolverException;
import at.ainf.theory.storage.DualStorage;
import at.ainf.theory.storage.SimpleStorage;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import java.io.File;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 10.05.12
 * Time: 13:58
 * To change this template use File | Settings | File Templates.
 */
public class RDFMatchingFileReaderTester {

    private static Logger logger = Logger.getLogger(RDFMatchingFileReaderTester.class.getName());

    @BeforeClass
    public static void setUp() {
        String conf = ClassLoader.getSystemResource("owlcontroller-log4j.properties").getFile();
        PropertyConfigurator.configure(conf);
    }

    @Test
    public void testParser() throws SolverException, InconsistentTheoryException{
        for (File file : new File(ClassLoader.getSystemResource("oaei11conference/matchings").getFile()).listFiles()) {
            String fileName = file.getName();
            StringTokenizer t = new StringTokenizer(fileName,"-");
            String matcher = t.nextToken();
            String o1 = t.nextToken();
            String o2 = t.nextToken();
            o2 = o2.substring(0,o2.length()-4);
            OWLOntology ontology1 = CreationUtils.createOwlOntology("oaei11conference/ontology",o1);
            OWLOntology ontology2 = CreationUtils.createOwlOntology("oaei11conference/ontology",o2);
            OWLOntology merged = CreationUtils.mergeOntologies(ontology1, ontology2);
            String n = file.getName().substring(0,file.getName().length()-4);
            Set<OWLLogicalAxiom> mapping = RDFUtils.readRdfMapping("oaei11conference/matchings",n).keySet();
            for (OWLLogicalAxiom axiom : mapping)
                merged.getOWLOntologyManager().applyChange(new AddAxiom(merged, axiom));

            OWLOntology extracted = new OWLIncoherencyExtractor(new Reasoner.ReasonerFactory(),merged).getIncoherentPartAsOntology();

            BreadthFirstSearch<OWLLogicalAxiom> search = new BreadthFirstSearch<OWLLogicalAxiom>(new DualStorage<OWLLogicalAxiom>());
            DualTreeOWLTheory theory = new DualTreeOWLTheory(new Reasoner.ReasonerFactory(),extracted, Collections.<OWLLogicalAxiom>emptySet());

            Set<OWLLogicalAxiom> ontology1CutExtracted = CreationUtils.getIntersection(extracted.getLogicalAxioms(), ontology1.getLogicalAxioms());
            Set<OWLLogicalAxiom> ontology2CutExtracted = CreationUtils.getIntersection(extracted.getLogicalAxioms(),ontology2.getLogicalAxioms());
            theory.addBackgroundFormulas(ontology1CutExtracted);
            theory.addBackgroundFormulas(ontology2CutExtracted);

            search.setSearcher(new FastDiagnosis<OWLLogicalAxiom>());
            search.setTheory(theory);

            try {
                search.run(9);
                System.out.println(file.getName() + " " + search.getStorage().getDiagnoses().size());
            } catch (NoConflictException e) {
                System.out.println(file.getName() + " cons");
                //e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

        }
    }

}
