package at.ainf.owlcontroller.queryeval;

import at.ainf.diagnosis.quickxplain.NewQuickXplain;
import at.ainf.diagnosis.tree.*;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.diagnosis.tree.searchstrategy.BreadthFirstSearchStrategy;
import at.ainf.owlapi3.model.DualTreeOWLTheory;
import at.ainf.owlapi3.model.OWLIncoherencyExtractor;
import at.ainf.owlapi3.model.OWLTheory;
import at.ainf.owlcontroller.Utils;
import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.storage.AxiomSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;
import org.apache.log4j.PropertyConfigurator;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.util.OWLOntologyMerger;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 30.04.12
 * Time: 08:48
 * To change this template use File | Settings | File Templates.
 */
public class OAEI2011 {

    @BeforeClass
    public static void setUp() {
        String conf = ClassLoader.getSystemResource("owlcontroller-log4j.properties").getFile();
        PropertyConfigurator.configure(conf);
    }

    @Test
    public void readData() throws ParserConfigurationException, IOException, SAXException {
        // create an empty model
        Model model = ModelFactory.createDefaultModel();

        // use the FileManager to find the input file
        String n = "c:/daten/work/tasks/oaei2011/1core8gb/run1/AgrMaker/anatomy-track1.rdf";
        InputStream in = FileManager.get().open(n);
        if (in == null) {
            throw new IllegalArgumentException(
                    "File: " + n + " not found");
        }

        // read the RDF/XML file
        model.read(in, null);

        // write it to standard out
        model.write(System.out);

        //StmtIterator iter =   model.listStatements(new SimpleSelector(model.getResource("AlignmentCell"), null, null));

    }

    public void readDataOAEI(String filename, Map<OWLLogicalAxiom, Double> axioms, Set<OWLLogicalAxiom> targetDiag, OWLOntologyManager man) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String line;
        String sourceNamespace = "";
        String targetNamespace = "";
        while ((line = br.readLine()) != null) {
            if (line.startsWith("sourceNamespace"))
                sourceNamespace = line.substring(line.indexOf("=") + 1).trim();
            if (line.startsWith("targetNamespace"))
                targetNamespace = line.substring(line.indexOf("=") + 1).trim();
            if (line.startsWith(">") || line.startsWith("<") || line.startsWith("+") || line.startsWith("-")) {
                String status = line.substring(0, 2).trim();
                String sub = line.substring(2);
                String source = "";
                String target = "";
                if (sub.contains("=")) {
                    source = sub.substring(0, sub.indexOf("=")).trim();
                    target = sub.substring(sub.indexOf("=") + 1, sub.indexOf("|")).trim();
                    axioms.put(createAxiomOAEI(sourceNamespace, source, targetNamespace, target, man),
                            Double.parseDouble(sub.substring(sub.indexOf("|") + 1)));
                    axioms.put(createAxiomOAEI(targetNamespace, target, sourceNamespace, source, man),
                            Double.parseDouble(sub.substring(sub.indexOf("|") + 1)));
                }
                if (sub.contains(">")) {
                    source = sub.substring(0, sub.indexOf(">")).trim();
                    target = sub.substring(sub.indexOf(">") + 1, sub.indexOf("|")).trim();
                    axioms.put(createAxiomOAEI(sourceNamespace, source, targetNamespace, target, man),
                            Double.parseDouble(sub.substring(sub.indexOf("|") + 1)));
                }
                if (sub.contains("<")) {
                    source = sub.substring(0, sub.indexOf("<")).trim();
                    target = sub.substring(sub.indexOf("<") + 1, sub.indexOf("|")).trim();
                    axioms.put(createAxiomOAEI(targetNamespace, target, sourceNamespace, source, man),
                            Double.parseDouble(sub.substring(sub.indexOf("|") + 1)));
                }
                if (status.equals("-")) {
                    if (sub.contains("=")) {
                        targetDiag.add(createAxiomOAEI(sourceNamespace, source, targetNamespace, target,man));
                        targetDiag.add(createAxiomOAEI(targetNamespace, target, sourceNamespace, source,man));
                    }
                    else if(sub.contains(">"))
                        targetDiag.add(createAxiomOAEI(sourceNamespace, source, targetNamespace, target,man));
                    else if(sub.contains("<"))
                        targetDiag.add(createAxiomOAEI(targetNamespace, target, sourceNamespace, source,man));
                }
                if (status.equals(">")) {
                    targetDiag.add(createAxiomOAEI(sourceNamespace, source, targetNamespace, target, man));
                }
                if (status.equals("<")) {
                    targetDiag.add(createAxiomOAEI(targetNamespace, target, sourceNamespace, source, man));
                }

            }
        }
    }

    public OWLOntology loadOntology(OWLOntologyManager manager, String path) {
        InputStream st = ClassLoader.getSystemResourceAsStream(path);
        try {
            return manager.loadOntologyFromOntologyDocument(st);
        } catch (OWLOntologyCreationException e) {

        }
        return null;


    }

    static OWLLogicalAxiom createAxiomOAEI(String sourceNamespace, String source, String targetNamespace, String target, OWLOntologyManager man) {
        OWLDataFactory factory = man.getOWLDataFactory();
        OWLClass clsA = factory.getOWLClass(IRI.create(sourceNamespace + "#" +  source));
        OWLClass clsB = factory.getOWLClass(IRI.create(targetNamespace + "#" +  target));
        OWLLogicalAxiom axiom = factory.getOWLSubClassOfAxiom(clsA, clsB);

        return axiom;
        // "<" + sourceNamespace + "#" + source + "> <" + targetNamespace + "#" + target + ">";
    }

    public OWLTheory createTheoryOAEI(OWLOntology ontology, boolean dual) throws SolverException, InconsistentTheoryException, OWLOntologyCreationException {
        ontology = new OWLIncoherencyExtractor(
                new Reasoner.ReasonerFactory()).getIncoherentPartAsOntology(ontology);
        Set<OWLLogicalAxiom> bax = new HashSet<OWLLogicalAxiom>();
        for (OWLIndividual ind : ontology.getIndividualsInSignature()) {
            bax.addAll(ontology.getClassAssertionAxioms(ind));
            bax.addAll(ontology.getObjectPropertyAssertionAxioms(ind));
        }

        OWLReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory();
        OWLTheory theory = null;
        if(dual)
            theory = new DualTreeOWLTheory(reasonerFactory, ontology, bax);
        else
            theory = new OWLTheory(reasonerFactory, ontology, bax);
        //assert (theory.verifyRequirements());

        return theory;
    }

    public OWLOntology createOntologyFromTxtOAEI(String file) {

        try {
            OWLOntologyManager man = OWLManager.createOWLOntologyManager();

            InputStream st = ClassLoader.getSystemResourceAsStream("oaei11/mouse.owl");
            OWLOntology mouse = man.loadOntologyFromOntologyDocument(st);
            st = ClassLoader.getSystemResourceAsStream("oaei11/human.owl");
            OWLOntology human = man.loadOntologyFromOntologyDocument(st);

            OWLOntologyMerger merger = new OWLOntologyMerger(man);
            OWLOntology merged = merger.createMergedOntology(man, IRI.create("matched" + file + ".txt"));
            Set<OWLLogicalAxiom> mappAx = getAxiomsInMappingOAEI(ClassLoader.getSystemResource("oaei11").getPath() + "/", file);
            for (OWLLogicalAxiom axiom : mappAx)
                man.applyChange(new AddAxiom(merged,axiom));

            return merged;
        } catch (OWLOntologyCreationException e) {
            return null;
        }
    }

    public Set<OWLLogicalAxiom> getLogicalAxiomsOfOntologiesOAEI() throws OWLOntologyCreationException {
        Set<OWLLogicalAxiom> r = new LinkedHashSet<OWLLogicalAxiom>();

        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        InputStream st = ClassLoader.getSystemResourceAsStream("oaei11/mouse.owl");
        OWLOntology mouse = man.loadOntologyFromOntologyDocument(st);
        st = ClassLoader.getSystemResourceAsStream("oaei11/human.owl");
        OWLOntology human = man.loadOntologyFromOntologyDocument(st);

        r.addAll(mouse.getLogicalAxioms());
        r.addAll(human.getLogicalAxioms());

        return r;
    }

    @Test
    public void searchDiags() throws
            OWLOntologyCreationException, SolverException, InconsistentTheoryException, NoConflictException {
        TreeSearch<? extends AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom> searchNormal = new HsTreeSearch<AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom>();
        searchNormal.setCostsEstimator(new SimpleCostsEstimator<OWLLogicalAxiom>());
        searchNormal.setSearchStrategy(new BreadthFirstSearchStrategy<OWLLogicalAxiom>());
        searchNormal.setSearcher(new NewQuickXplain<OWLLogicalAxiom>());

        String matching = "Aroma";

        // OWLOntology ontology = loadOntology(OWLManager.createOWLOntologyManager(), "oaei11/" + matching+".owl");

        OWLOntology merged = createOntologyFromTxtOAEI(matching);

        Set<OWLLogicalAxiom> targetDiag = getTargetDOAEI(ClassLoader.getSystemResource("oaei11").getPath() + "/", matching);

        OWLTheory theoryNormal = createTheoryOAEI(merged, false);
        searchNormal.setTheory(theoryNormal);

        LinkedHashSet<OWLLogicalAxiom> bx = new LinkedHashSet<OWLLogicalAxiom>();
        bx.addAll(getLogicalAxiomsOfOntologiesOAEI());

        Set<OWLLogicalAxiom> schn = new LinkedHashSet<OWLLogicalAxiom>();
        schn.addAll(getAxiomsInMappingOAEI(ClassLoader.getSystemResource("oaei11").getPath() + "/", "reference_2011"));
        schn.retainAll(getAxiomsInMappingOAEI(ClassLoader.getSystemResource("oaei11").getPath() + "/", matching));
        bx.addAll(schn);

        bx.retainAll(theoryNormal.getOriginalOntology().getLogicalAxioms());
        //bx.addAll(getAxiomsInSourceAndOther("c:/daten/work/tasks/oaei2011/ontos/", matching, "reference_2011"));
        theoryNormal.addBackgroundFormulas(bx);

        Runtime.getRuntime().  addShutdownHook  (
            new Thread() {
                @Override public void run() {
                    System.out.println("VM gets now closed");
                }
            });

        searchNormal.run ();
        Set<? extends AxiomSet<OWLLogicalAxiom>> resultNormal = searchNormal.getDiagnoses();
        for (Set<OWLLogicalAxiom> diagnosis : resultNormal)
            System.out.println(Utils.renderAxioms(diagnosis));

    }


    @Test
    public void createMatchedOnto() {
        String path = "c:/daten/work/tasks/oaei2011/ontos/";
        String matching = "reference_2011";

        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        try {
            OWLOntology mouse = man.loadOntologyFromOntologyDocument(new File(path + "mouse.owl"));
            OWLOntology human = man.loadOntologyFromOntologyDocument(new File(path + "human.owl"));

            OWLOntologyMerger merger = new OWLOntologyMerger(man);
            OWLOntology merged = merger.createMergedOntology(man, IRI.create("matched" + matching + ".txt"));

            Map<OWLLogicalAxiom,Double> axioms = new HashMap<OWLLogicalAxiom, Double>();
            Set<OWLLogicalAxiom> targetDiagnosis = new LinkedHashSet<OWLLogicalAxiom>();

            readDataOAEI(path + matching + ".txt", axioms, targetDiagnosis, man);

            for (OWLAxiom axiom : axioms.keySet())
                man.applyChange(new AddAxiom(merged,axiom));


            man.saveOntology(merged,IRI.create(new File(path + matching + ".owl").toURI()));


            System.out.println();

        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (OWLOntologyStorageException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


    }

    public Set<OWLLogicalAxiom> getAxiomsInMappingOAEI(String path, String source) {
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();

        Map<OWLLogicalAxiom,Double> axioms = new HashMap<OWLLogicalAxiom, Double>();
        Set<OWLLogicalAxiom> targetDiagnosis = new LinkedHashSet<OWLLogicalAxiom>();
        try {
            readDataOAEI(path + source + ".txt", axioms, targetDiagnosis, man);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return axioms.keySet();
    }

    public Set<OWLLogicalAxiom> getTargetDOAEI(String path, String source) {
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();

        Map<OWLLogicalAxiom,Double> axioms = new HashMap<OWLLogicalAxiom, Double>();
        Set<OWLLogicalAxiom> targetDiagnosis = new LinkedHashSet<OWLLogicalAxiom>();
        try {
            readDataOAEI(path + source + ".txt", axioms, targetDiagnosis, man);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return targetDiagnosis;
    }

    public Set<OWLLogicalAxiom> getAxiomsOnlyInSource(String path, String source, String destination) {
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();

        Map<OWLLogicalAxiom,Double> axiomsReference = new HashMap<OWLLogicalAxiom, Double>();
        Set<OWLLogicalAxiom> targetDiagnosisReference = new LinkedHashSet<OWLLogicalAxiom>();
        try {
            readDataOAEI(path + source + ".txt", axiomsReference, targetDiagnosisReference, man);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        Map<OWLLogicalAxiom,Double> axioms = new HashMap<OWLLogicalAxiom, Double>();
        Set<OWLLogicalAxiom> targetDiagnosis = new LinkedHashSet<OWLLogicalAxiom>();
        try {
            readDataOAEI(path + destination + ".txt", axioms, targetDiagnosis, man);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        Set<OWLLogicalAxiom> dif = new LinkedHashSet<OWLLogicalAxiom>();
        dif.addAll(axiomsReference.keySet());
        dif.removeAll(axioms.keySet());
        return dif;

    }



    @Test
    public void compareMatching() throws IOException {
        String path = "c:/daten/work/tasks/oaei2011/ontos/";
        String referenceMapping = "reference_2011";
        String map = "AgrMaker";

        getAxiomsOnlyInSource(path,map,referenceMapping);
        System.out.println ();

    }



}
