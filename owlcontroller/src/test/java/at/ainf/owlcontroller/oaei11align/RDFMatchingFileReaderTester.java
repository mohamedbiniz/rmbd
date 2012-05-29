package at.ainf.owlcontroller.oaei11align;

import at.ainf.diagnosis.quickxplain.FastDiagnosis;
import at.ainf.diagnosis.quickxplain.NewQuickXplain;
import at.ainf.diagnosis.tree.BreadthFirstSearch;
import at.ainf.diagnosis.tree.DualTreeLogic;
import at.ainf.diagnosis.tree.TreeSearch;
import at.ainf.diagnosis.tree.UniformCostSearch;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.owlapi3.model.DualTreeOWLTheory;
import at.ainf.owlapi3.model.OWLIncoherencyExtractor;
import at.ainf.owlapi3.model.OWLTheory;
import at.ainf.owlcontroller.CreationUtils;
import at.ainf.owlcontroller.RDFUtils;
import at.ainf.owlcontroller.costestimation.OWLAxiomCostsEstimator;
import at.ainf.theory.model.InconsistentTheoryException;
import at.ainf.theory.model.SolverException;
import at.ainf.theory.storage.AxiomSet;
import at.ainf.theory.storage.DualStorage;
import at.ainf.theory.storage.SimpleStorage;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import java.io.File;
import java.util.*;
import java.util.concurrent.*;

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

    private class SearchThread implements Callable<String> {
        private File file;

        public SearchThread(File file) {
            this.file =  file;
        }

        public File getFile() {
            return file;
        }

        public String call() {
            String fileName = file.getName();
            StringTokenizer t = new StringTokenizer(fileName,"-");
            String matcher = t.nextToken();
            String o1 = t.nextToken();
            String o2 = t.nextToken();
            o2 = o2.substring(0,o2.length()-4);

            String n = file.getName().substring(0,file.getName().length()-4);
            OWLOntology merged = RDFUtils.createOntologyWithMappings("oaei11conference/ontology", o1, o2,
                    "oaei11conference/matchings/inconsistent", n + ".rdf");

            long extractionTime = System.currentTimeMillis();
            OWLOntology extracted = new OWLIncoherencyExtractor(
                    new Reasoner.ReasonerFactory(),merged).getIncoherentPartAsOntology();
            extractionTime = System.currentTimeMillis() - extractionTime;

            Set<OWLLogicalAxiom> ontoBackground = new LinkedHashSet<OWLLogicalAxiom>();

            String refmatchPath = "oaei11conference/matchings/references";
            String refMatch = o1 + "-" + o2 + ".rdf";
            Set<OWLLogicalAxiom> correctMappingAxioms = RDFUtils.readRdfMapping(refmatchPath,refMatch).keySet();
            ontoBackground.addAll(CreationUtils.getIntersection(extracted.getLogicalAxioms(),correctMappingAxioms));

            OWLOntology ontology1 = CreationUtils.createOwlOntology("oaei11conference/ontology",o1+".owl");
            OWLOntology ontology2 = CreationUtils.createOwlOntology("oaei11conference/ontology",o2+".owl");
            ontoBackground.addAll(CreationUtils.getIntersection(extracted.getLogicalAxioms(),ontology1.getLogicalAxioms()));
            ontoBackground.addAll(CreationUtils.getIntersection(extracted.getLogicalAxioms(),ontology2.getLogicalAxioms()));

            TreeSearch<AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom> searchDual = new BreadthFirstSearch<OWLLogicalAxiom>(new DualStorage<OWLLogicalAxiom>());
            searchDual.setSearcher(new FastDiagnosis<OWLLogicalAxiom>());


            Set<OWLLogicalAxiom> bax = new HashSet<OWLLogicalAxiom>();
            for (OWLIndividual ind : extracted.getIndividualsInSignature()) {
                bax.addAll(extracted.getClassAssertionAxioms(ind));
                bax.addAll(extracted.getObjectPropertyAssertionAxioms(ind));
                bax.addAll(ontoBackground);
            }
            OWLReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory();
            OWLTheory theory = null;
            try {
                theory = new DualTreeOWLTheory(reasonerFactory, extracted, bax);
            } catch (InconsistentTheoryException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (SolverException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            searchDual.setTheory(theory);
            searchDual.setLogic(new DualTreeLogic<AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom>());

            long time = System.currentTimeMillis();
            try {
                searchDual.run();
            } catch (SolverException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (NoConflictException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (InconsistentTheoryException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            time = System.currentTimeMillis() - time;
            int numDiags = searchDual.getStorage().getDiagnoses().size();

            Set<Integer> sizes = new LinkedHashSet<Integer>();
            for (AxiomSet<OWLLogicalAxiom> diagnosis : searchDual.getStorage().getDiagnoses())
                sizes.add(diagnosis.size());
            int minCardSize = Collections.min(sizes);
            int numOfMinCardDiags = 0;
            for (Integer size : sizes)
                if (minCardSize == size)
                    numOfMinCardDiags++;

            /*int cnt = 1;
            String f = file.getName().substring(0,file.getName().length()-4);
            for (Set<OWLLogicalAxiom> diag : searchDual.getStorage().getDiagnoses()) {
                CreationUtils.writeDiagnosisToFile(f + "_"+cnt,diag);

                Set<OWLLogicalAxiom> read = CreationUtils.readDiagnosisFromFile(f+"_"+cnt);
                if (diag.size()==read.size() && read.containsAll(diag) && diag.containsAll(read))
                    logger.info("diag written file: " + file.getName());
                cnt++;
            }*/
            Set<AxiomSet<OWLLogicalAxiom>> diagnoses = searchDual.getStorage().getDiagnoses();
            boolean found = false;
             Set<OWLLogicalAxiom> targetDg =  CreationUtils.readDiagnosisFromFile(file.getName().substring(0,file.getName().length()-4) + "_1");
            for (AxiomSet<OWLLogicalAxiom> diagnosis : diagnoses) {
                if (diagnosis.size() == targetDg.size() && diagnosis.containsAll(targetDg) &&
                        targetDg.containsAll(diagnosis))
                    found = true;
            }
            logger.info(file.getName() + ",found," + found);


            /*logger.info(","+matcher + "," + o1 + "," + o2 + "," + time + "," + extractionTime
                    + "," + numDiags + ","+ numOfMinCardDiags + "," + minCardSize ); */

            return "";

        }
    }

    @Test
    public void searchOneDiagTime() throws SolverException, InconsistentTheoryException, NoConflictException {
        File[] f = new File(ClassLoader.getSystemResource("oaei11conference/matchings/inconsistent").getFile()).listFiles();
        Set<String> excluded = new LinkedHashSet<String>();

        excluded.add("ldoa-conference-iasted-rdf");

        ExecutorService executor = Executors.newCachedThreadPool();

        for (int i = 0; i < f.length; i++) {
            if (f[i].isDirectory() || excluded.contains(f[i].getName()))
                continue;

            SearchThread search = new SearchThread(f[i]);

            Future future = executor.submit(search);
        }

        try {
            executor.awaitTermination(7,TimeUnit.DAYS);

        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Test
    public void testConsistency() throws SolverException, InconsistentTheoryException{
    File[] f = new File(ClassLoader.getSystemResource("oaei11conference/matchings/incoherent").getFile()).listFiles();
        for (int i = 2-2; i < f.length; i++) {
            if (f[i].isDirectory() )
                continue;
            String fileName = f[i].getName();
            StringTokenizer t = new StringTokenizer(fileName,"-");
            String matcher = t.nextToken();
            String o1 = t.nextToken();
            String o2 = t.nextToken();
            o2 = o2.substring(0,o2.length()-4);

            String n = f[i].getName().substring(0,f[i].getName().length()-4);
            OWLOntology merged = RDFUtils.createOntologyWithMappings("oaei11conference/ontology",o1,o2,
                    "oaei11conference/matchings/incoherent",n+".rdf");

            /*OWLOntology ontology1 = CreationUtils.createOwlOntology("oaei11conference/ontology",o1);
            OWLOntology ontology2 = CreationUtils.createOwlOntology("oaei11conference/ontology",o2);
            OWLOntology merged = CreationUtils.mergeOntologies(ontology1, ontology2);
            String n = f[i].getName().substring(0,f[i].getName().length()-4);
        Set<OWLLogicalAxiom> mapping = RDFUtils.readRdfMapping("oaei11conference/matchings/incoherent",n).keySet();
            for (OWLLogicalAxiom axiom : mapping)
                merged.getOWLOntologyManager().applyChange(new AddAxiom(merged, axiom));*/

            Set<OWLEntity> incoherentEntities = new LinkedHashSet<OWLEntity>();
            OWLReasoner reasoner = new Reasoner.ReasonerFactory().createReasoner(merged);

            if (reasoner.isConsistent()) {
                    if (reasoner.getUnsatisfiableClasses().getEntities().size() == 1)
                        logger.info(","+f[i].getName() + ",ok");
                    else
                        logger.info("," + f[i].getName() + ",incoherent");
                }
                else {
                    logger.info("," + f[i].getName() + ",inconsistent");
                }



        }
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
            OWLOntology ontology1 = CreationUtils.createOwlOntology("oaei11conference/ontology",o1+".owl");
            OWLOntology ontology2 = CreationUtils.createOwlOntology("oaei11conference/ontology",o2+".owl");
            OWLOntology merged = CreationUtils.mergeOntologies(ontology1, ontology2);
            String n = file.getName().substring(0,file.getName().length()-4);
            Set<OWLLogicalAxiom> mapping = RDFUtils.readRdfMapping("oaei11conference/matchings",n + ".rdf").keySet();
            for (OWLLogicalAxiom axiom : mapping)
                merged.getOWLOntologyManager().applyChange(new AddAxiom(merged, axiom));

            OWLOntology extracted = new OWLIncoherencyExtractor(new Reasoner.ReasonerFactory(),merged).getIncoherentPartAsOntology();

            BreadthFirstSearch<OWLLogicalAxiom> search = new BreadthFirstSearch<OWLLogicalAxiom>(new DualStorage<OWLLogicalAxiom>());
            DualTreeOWLTheory theory = null;
            try {
                theory = new DualTreeOWLTheory(new Reasoner.ReasonerFactory(),extracted, Collections.<OWLLogicalAxiom>emptySet());
            }
            catch(InconsistentTheoryException e) {
                logger.info(file.getName() + " cons");
                continue;
            }

            Set<OWLLogicalAxiom> ontology1CutExtracted = CreationUtils.getIntersection(extracted.getLogicalAxioms(), ontology1.getLogicalAxioms());
            Set<OWLLogicalAxiom> ontology2CutExtracted = CreationUtils.getIntersection(extracted.getLogicalAxioms(),ontology2.getLogicalAxioms());
            theory.addBackgroundFormulas(ontology1CutExtracted);
            theory.addBackgroundFormulas(ontology2CutExtracted);

            search.setSearcher(new FastDiagnosis<OWLLogicalAxiom>());
            search.setTheory(theory);

            try {
                search.run(9);
                logger.info(file.getName() + " " + search.getStorage().getDiagnoses().size());
            } catch (NoConflictException e) {
                logger.info(file.getName() + " cons");
                //e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

        }
    }

}
