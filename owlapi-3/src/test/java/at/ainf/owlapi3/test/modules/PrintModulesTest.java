package at.ainf.owlapi3.test.modules;

import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.quickxplain.QuickXplain;
import at.ainf.diagnosis.storage.FormulaSet;
import at.ainf.diagnosis.tree.HsTreeSearch;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.diagnosis.tree.searchstrategy.UniformCostSearchStrategy;
import at.ainf.owlapi3.costestimation.OWLAxiomKeywordCostsEstimator;
import at.ainf.owlapi3.model.OWLTheory;
import at.ainf.owlapi3.module.OtfModuleProvider;
import edu.arizona.bio5.onto.decomposition.Atom;
import edu.arizona.bio5.onto.decomposition.AtomicDecomposition;
import edu.arizona.bio5.onto.decomposition.ChiaraDecompositionAlgorithm;
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOWLObjectRendererImpl;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;

import java.io.InputStream;
import java.util.*;

import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: pr8
 * Date: 02.04.13
 * Time: 14:45
 * To change this template use File | Settings | File Templates.
 */
public class PrintModulesTest {

    private static Logger logger = LoggerFactory.getLogger(PrintModulesTest.class.getName());

        @Ignore
        @Test
        /**
         * This testcase is a simple example how to start diagnoses
         */
        public void searchKoalaTest() throws OWLOntologyCreationException, SolverException, InconsistentTheoryException {

            //InputStream koalaStream = ClassLoader.getSystemResourceAsStream("ontologies/University.owl");
            InputStream ontologyStream = ClassLoader.getSystemResourceAsStream("ontologies/example_ontology_20130412_1_coherent.owl");
            OWLOntology ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(ontologyStream);
            OtfModuleProvider provider = new OtfModuleProvider(ontology,new Reasoner.ReasonerFactory(),false);
            ToStringRenderer.getInstance().setRenderer(new ManchesterOWLSyntaxOWLObjectRendererImpl());


            // starts extraction of a module for each unsat class and gives union
            // this needs to be called before getUnsatClasses otherwise getUnsatClasses returns empty Set
            provider.getModuleUnsatClass();


            ChiaraDecompositionAlgorithm atomicDecomposer = new ChiaraDecompositionAlgorithm(ModuleType.BOT);
            AtomicDecomposition ad = atomicDecomposer.decompose(OWLManager.createOWLOntologyManager(),ontology);
            logger.info(ad.toString());
            for(OWLLogicalAxiom ax : ontology.getLogicalAxioms()){
                logger.info("Atom for axiom " + ax + " is: " + ad.getAtomForAxiom(ax));
            }
            Map<Atom,Set<Atom>> dependencyMap = new HashMap<Atom, Set<Atom>>();
            for(Atom atom : ad.getAtoms()){
                dependencyMap.put(atom,ad.getDependencies(atom,true));
            }
            for(Atom atom : dependencyMap.keySet()){
                logger.info("Atom " + atom + " depends on the following atoms: " + dependencyMap.get(atom));
            }

            Map<OWLClass,Set<OWLLogicalAxiom>> unsatClasses = provider.getUnsatClasses();
            for (OWLClass unsatClass : unsatClasses.keySet()) {
                logger.info("-----------------------------------------------------------------------");
                logger.info("Module for unsatisfiable Class: " + unsatClass);
                logger.info("Number of axioms in module: " + unsatClasses.get(unsatClass).size());
                Iterator axiomIterator = unsatClasses.get(unsatClass).iterator();
                while(axiomIterator.hasNext()){
                    logger.info(axiomIterator.next().toString());
                }
            }

        }


}
