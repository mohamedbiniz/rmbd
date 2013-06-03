package at.ainf.owlapi3.test.modules;

import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.owlapi3.module.modprovider.OtfModuleProvider;
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOWLObjectRendererImpl;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

import java.io.InputStream;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: pr8
 * Date: 05.04.13
 * Time: 16:43
 * To change this template use File | Settings | File Templates.
 */
public class Modules4AllClassesTest {

    private static Logger logger = LoggerFactory.getLogger(PrintModulesTest.class.getName());

            @Ignore @Test
            /**
             * This testcase is a simple example how to start diagnoses
             */
            public void searchModules4AllClassesTest() throws OWLOntologyCreationException, SolverException, InconsistentTheoryException {

                //InputStream koalaStream = ClassLoader.getSystemResourceAsStream("ontologies/University.owl");
                InputStream ontologyStream = ClassLoader.getSystemResourceAsStream("ontologies/example_ontology_20130411.owl");
                OWLOntology ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(ontologyStream);
                OtfModuleProvider provider = new OtfModuleProvider(ontology,new Reasoner.ReasonerFactory(),false);
                ToStringRenderer.getInstance().setRenderer(new ManchesterOWLSyntaxOWLObjectRendererImpl());


                // starts extraction of a module for each unsat class and gives union
                // this needs to be called before getUnsatClasses otherwise getUnsatClasses returns empty Set
                provider.getModuleUnsatClass();


                /*ChiaraDecompositionAlgorithm atomicDecomposer = new ChiaraDecompositionAlgorithm(ModuleType.STAR);
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
                }*/

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
                Set<OWLClass> satClasses = ontology.getClassesInSignature();
                satClasses.removeAll(unsatClasses.keySet());

                SyntacticLocalityModuleExtractor moduleStar = new SyntacticLocalityModuleExtractor(OWLManager.createOWLOntologyManager(), ontology, ModuleType.STAR);

                Map<OWLClass,Set<OWLAxiom>> modules4SatClasses = new HashMap<OWLClass, Set<OWLAxiom>>();

                for (OWLEntity satClass : satClasses) {
                    //Set<OWLLogicalAxiom> owlLogicalAxioms = convertAxiom2LogicalAxiom(moduleStar.extract(Collections.singleton(satClass)));
                    Set<OWLAxiom> owlAxioms = moduleStar.extract(Collections.singleton(satClass));
                    modules4SatClasses.put((OWLClass) satClass, owlAxioms);
                }

                printClassesAndModules(modules4SatClasses);



            }

            private Set<OWLLogicalAxiom> convertAxiom2LogicalAxiom (Set<OWLAxiom> axioms) {
                Set<OWLLogicalAxiom> result = new LinkedHashSet<OWLLogicalAxiom>();
                for (OWLAxiom axiom : axioms)
                    result.add((OWLLogicalAxiom)axiom);
                return result;
            }


            private void printClassesAndModules(Map<OWLClass,Set<OWLAxiom>> classModuleMap) {
                for (OWLClass cls : classModuleMap.keySet()) {
                    logger.info("-----------------------------------------------------------------------");
                    logger.info("Module for Class: " + cls);
                    logger.info("Number of axioms in module: " + classModuleMap.get(cls).size());
                    Iterator axiomIterator = classModuleMap.get(cls).iterator();
                    while(axiomIterator.hasNext()){
                        logger.info(axiomIterator.next().toString());
                    }
                }
            }






}
