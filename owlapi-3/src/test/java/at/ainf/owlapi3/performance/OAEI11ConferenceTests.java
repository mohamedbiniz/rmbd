package at.ainf.owlapi3.performance;

import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.quickxplain.DirectDiagnosis;
import at.ainf.diagnosis.quickxplain.QuickXplain;
import at.ainf.diagnosis.storage.FormulaSet;
import at.ainf.diagnosis.tree.HsTreeSearch;
import at.ainf.diagnosis.tree.InvHsTreeSearch;
import at.ainf.diagnosis.tree.SimpleCostsEstimator;
import at.ainf.diagnosis.tree.TreeSearch;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.diagnosis.tree.searchstrategy.BreadthFirstSearchStrategy;
import at.ainf.owlapi3.base.OAEI11ConferenceSession;
import at.ainf.owlapi3.base.SimulatedSession;
import at.ainf.owlapi3.base.tools.TableList;
import at.ainf.owlapi3.costestimation.OWLAxiomCostsEstimator;
import at.ainf.owlapi3.model.DualTreeOWLTheory;
import at.ainf.owlapi3.model.OWLIncoherencyExtractor;
import at.ainf.owlapi3.model.OWLTheory;
import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxOntologyFormat;
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.FileDocumentTarget;
import org.semanticweb.owlapi.io.OWLOntologyDocumentTarget;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.math.BigDecimal;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;

import static at.ainf.owlapi3.util.SetUtils.createIntersection;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 06.08.12
 * Time: 07:52
 * To change this template use File | Settings | File Templates.
 */
public class OAEI11ConferenceTests extends OAEI11ConferenceSession {

    private static Logger logger = LoggerFactory.getLogger(OAEI11ConferenceTests.class.getName());

    /*@BeforeClass
    public static void setUp() {
        String conf = ClassLoader.getSystemResource("owlapi3-log4j.properties").getFile();
        PropertyConfigurator.configure(conf); }*/
    @Ignore
    @Test
    public void saveOntologiesConference2011()
            throws SolverException, InconsistentTheoryException, IOException, OWLOntologyCreationException {

        String matchingsDir = "oaei11conference/matchings/";
        String directory = "incoherent";
        File[] f = new File(ClassLoader.getSystemResource(matchingsDir + directory)
                .getFile()).listFiles();
        String directory2 = "inconsistent";
        File[] f2 = new File(ClassLoader.getSystemResource(matchingsDir + directory2)
                .getFile()).listFiles();
        Set<File> files = new LinkedHashSet<File>();
        Map<File, String> map = new HashMap<File, String>();
        for (File file : f) {
            files.add(file);
            map.put(file, "incoherent");
        }
        for (File file : f2) {
            files.add(file);
            map.put(file, "inconsistent");
        }

        for (File file : files) {
            String fileName = file.getName();
            StringTokenizer t = new StringTokenizer(fileName, "-");
            t.nextToken();

            String o1 = t.nextToken();
            String o2 = t.nextToken();
            o2 = o2.substring(0, o2.length() - 4);

            String n = file.getName().substring(0, file.getName().length() - 4);
            OWLOntology merged = getOntology("oaei11conference/ontology",
                    o1, o2, matchingsDir + map.get(file), n + ".rdf");

            String s = merged.getOntologyID().getOntologyIRI().toString();
            logger.info(s);
            File fileSave = new File("C:\\Daten\\Work\\tasks\\Papers\\ISWC12dualRev\\ontologies\\"+n+".owl");
            try {
                ManchesterOWLSyntaxOntologyFormat rdfxmlOntologyFormat = new ManchesterOWLSyntaxOntologyFormat();
                OWLOntologyDocumentTarget documentTarget = new FileDocumentTarget(fileSave);

                OWLManager.createOWLOntologyManager().saveOntology(merged, rdfxmlOntologyFormat, documentTarget);

            } catch (OWLOntologyStorageException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

        }
    }

    @Test
    public void doTestsOAEIConference()
            throws SolverException, InconsistentTheoryException, IOException, OWLOntologyCreationException {


        String matchingsDir = "oaei11conference/matchings/";
        String ontologyDir = "oaei11conference/ontology";

        File[] f = getMappingFiles(matchingsDir, "incoherent", "includedIncoher.txt");
        File[] f2 = getMappingFiles(matchingsDir, "inconsistent", "included.txt");

        Set<File> files = new LinkedHashSet<File>();
        Map<File, String> map = new HashMap<File, String>();
        for (File file : f) {
            files.add(file);
            map.put(file, "incoherent");
        }
        for (File file : f2) {
            files.add(file);
            map.put(file, "inconsistent");
        }

        runOaeiConfereneTests(matchingsDir, ontologyDir, files, map);
    }

    protected void runOaeiConfereneTests(String matchingsDir, String ontologyDir, Set<File> files, Map<File, String> map) throws SolverException, InconsistentTheoryException {
        SimulatedSession session = new SimulatedSession();

        session.setShowElRates(false);

        QSSType[] qssTypes = new QSSType[]{QSSType.MINSCORE};
        //QSSType[] qssTypes = new QSSType[]{QSSType.MINSCORE, QSSType.SPLITINHALF};

        for (TargetSource targetSource : new TargetSource[]{TargetSource.FROM_30_DIAGS}) {

            for (File file : files) {
                logger.info("processing " + file.getName());

                String out = "STAT, " + file;


                Random random = new Random(12311);
                Set<FormulaSet<OWLLogicalAxiom>> targetDgSet = getRandomDiagSet(file, map.get(file));
                int randomNr = chooseRandomNum(targetDgSet,random);
                Set<OWLLogicalAxiom> targetDg = chooseRandomDiag(targetDgSet,file,randomNr);


                for (QSSType qssType : qssTypes) {


                    String fileName = file.getName();
                    StringTokenizer t = new StringTokenizer(fileName, "-");
                    String matcher = t.nextToken();

                    String o1 = t.nextToken();
                    String o2 = t.nextToken();
                    o2 = o2.substring(0, o2.length() - 4);

                    String n = file.getName().substring(0, file.getName().length() - 4);
                    OWLOntology merged = getOntology(ontologyDir,
                            o1, o2, matchingsDir + map.get(file), n + ".rdf");

                    long preprocessModulExtract = System.currentTimeMillis();
                    OWLOntology ontology = new OWLIncoherencyExtractor(
                            new Reasoner.ReasonerFactory()).getIncoherentPartAsOntology(merged);
                    preprocessModulExtract = System.currentTimeMillis() - preprocessModulExtract;
                    OWLTheory theory = getExtendTheory(ontology, false);
                    //Define Treesearch here
                    TreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = getUniformCostSearch(theory, false);
                   /* BinaryTreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom> search = new BinaryTreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom>();
                    search.setCostsEstimator(new SimpleCostsEstimator<OWLLogicalAxiom>());
                    search.setSearchStrategy(new BreadthFirstSearchStrategy<OWLLogicalAxiom>());
                    MultiQuickXplain<OWLLogicalAxiom> searcher = new MultiQuickXplain<OWLLogicalAxiom>(2,10,10);
                    //searcher.setAxiomListener(new QXSingleAxiomListener<OWLLogicalAxiom>(true));
                    searcher.setAxiomListener(new QXAxiomSetListener<OWLLogicalAxiom>(true));
                    // QuickXplain<OWLLogicalAxiom> searcher = new QuickXplain<OWLLogicalAxiom>();
                    search.setSearcher(searcher);
                    search.setSearchable(theory);     */
                    //Copy of Search initialisation ends here


                    LinkedHashSet<OWLLogicalAxiom> bx = new LinkedHashSet<OWLLogicalAxiom>();
                    OWLOntology ontology1 = getOntologySimple(ontologyDir, o1 + ".owl");
                    OWLOntology ontology2 = getOntologySimple(ontologyDir, o2 + ".owl");
                    Set<OWLLogicalAxiom> onto1Axioms = createIntersection(ontology.getLogicalAxioms(), ontology1.getLogicalAxioms());
                    Set<OWLLogicalAxiom> onto2Axioms = createIntersection(ontology.getLogicalAxioms(), ontology2.getLogicalAxioms());
                    int modOnto1Size = onto1Axioms.size();
                    int modOnto2Size = onto2Axioms.size();
                    int modMappingSize = ontology.getLogicalAxioms().size() - modOnto1Size - modOnto2Size;
                    bx.addAll(onto1Axioms);
                    bx.addAll(onto2Axioms);
                    theory.getKnowledgeBase().addBackgroundFormulas(bx);

                    Map<OWLLogicalAxiom, BigDecimal> map1 = readRdfMapping(matchingsDir + map.get(file), n + ".rdf");

                    OWLAxiomCostsEstimator es = new OWLAxiomCostsEstimator(theory, map1);


                    search.setCostsEstimator(es);


                       search.reset();


                    TableList e = new TableList();
                    out += "," + qssType + ",";
                    String message = "act," + file.getName() + "," + map.get(file) + "," + targetSource
                            + "," + qssType + "," + preprocessModulExtract + "," + randomNr
                            + modOnto1Size + "," + modOnto2Size + "," + modMappingSize + "";
                    session.setEntry(e);
                    session.setMessage(message);
                    session.setScoringFunct(qssType);
                    session.setTargetD(targetDg);
                    session.setTheory(theory);
                    session.setSearch(search);
                    out += session.simulateQuerySession();
                    //logger.info ("done " + file.getName() + " with qss " + qssType + "result " + out );

                }
                logger.info(out);


            }
        }
    }

    protected File[] getMappingFiles(String matchingsDir, String dir, String exclusionFile) {
        URL exclusionFileUrl = ClassLoader.getSystemResource(matchingsDir + exclusionFile);
        File folder = new File(ClassLoader.getSystemResource(matchingsDir + dir).getFile());
        if (exclusionFileUrl == null)
            return folder.listFiles();
        File incl = new File(exclusionFileUrl.getFile());
        MyFilenameFilter filter = new MyFilenameFilter(incl);
        return folder.listFiles(filter);
    }

    private class SearchThread implements Callable<String> {
        private boolean dual;
        private File file;
        private String d;

        public SearchThread(File file, boolean dual, String dir) {
            this.file =  file;
            this.dual = dual;
            this.d =  dir;
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
            OWLOntology merged = getOntology("oaei11conference/ontology", o1, o2,
                    "oaei11conference/matchings/" + d, n + ".rdf");

            long extractionTime = System.currentTimeMillis();
            OWLOntology extracted = new OWLIncoherencyExtractor(
                    new Reasoner.ReasonerFactory()).getIncoherentPartAsOntology(merged);
            extractionTime = System.currentTimeMillis() - extractionTime;

            Set<OWLLogicalAxiom> ontoBackground = new LinkedHashSet<OWLLogicalAxiom>();

            String refmatchPath = "oaei11conference/matchings/references";
            String refMatch = o1 + "-" + o2 + ".rdf";
            Set<OWLLogicalAxiom> correctMappingAxioms = readRdfMapping(refmatchPath, refMatch).keySet();
            ontoBackground.addAll(createIntersection(extracted.getLogicalAxioms(), correctMappingAxioms));

            OWLOntology ontology1 = getOntologySimple("oaei11conference/ontology", o1 + ".owl");
            OWLOntology ontology2 = getOntologySimple("oaei11conference/ontology", o2 + ".owl");
            ontoBackground.addAll(createIntersection(extracted.getLogicalAxioms(), ontology1.getLogicalAxioms()));
            ontoBackground.addAll(createIntersection(extracted.getLogicalAxioms(), ontology2.getLogicalAxioms()));

            TreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom> searchDual;
            if (dual) {
                searchDual = new InvHsTreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom>();
                searchDual.setCostsEstimator(new SimpleCostsEstimator<OWLLogicalAxiom>());
                searchDual.setSearchStrategy(new BreadthFirstSearchStrategy<OWLLogicalAxiom>());
                searchDual.setSearcher(new DirectDiagnosis<OWLLogicalAxiom>());
            }
            else {
                searchDual = new HsTreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom>();
                searchDual.setCostsEstimator(new SimpleCostsEstimator<OWLLogicalAxiom>());
                searchDual.setSearchStrategy(new BreadthFirstSearchStrategy<OWLLogicalAxiom>());
                searchDual.setSearcher(new QuickXplain<OWLLogicalAxiom>());
            }


            Set<OWLLogicalAxiom> bax = new HashSet<OWLLogicalAxiom>();
            for (OWLIndividual ind : extracted.getIndividualsInSignature()) {
                bax.addAll(extracted.getClassAssertionAxioms(ind));
                bax.addAll(extracted.getObjectPropertyAssertionAxioms(ind));
                bax.addAll(ontoBackground);
            }
            OWLReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory();
            OWLTheory theory = null;
            try {
                if (dual)
                    theory = new DualTreeOWLTheory(reasonerFactory, extracted, bax);
                else
                    theory = new OWLTheory(reasonerFactory, extracted, bax);
            } catch (InconsistentTheoryException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (SolverException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            searchDual.setSearchable(theory);
            //if (dual) searchDual.setLogic(new DualTreeLogic<AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom>());

            long start = System.currentTimeMillis();
            try {
                searchDual.setMaxDiagnosesNumber(1);
                searchDual.start();
            } catch (SolverException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (NoConflictException e) {
                logger.info(",dual: " + dual + ",no conflicts 1: " + file.getName());
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (InconsistentTheoryException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            long time1 = System.currentTimeMillis() - start;
            int conflicts1 = searchDual.getConflicts().size();
            logger.info(",dual: " + dual + "," + matcher + o1 + o2 + ","+matcher + "," + o1 + "," + o2 + ",diagnosis 1 found " + time1+ ","
                    + conflicts1 + "," + extractionTime);
            try {
                searchDual.setMaxDiagnosesNumber(9);
                searchDual.start();
            } catch (SolverException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (NoConflictException e) {
                logger.info(",dual: " + dual + ",no conflicts 9: " + file.getName());
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (InconsistentTheoryException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            long time9 = System.currentTimeMillis() - start;
            int conflicts9 = searchDual.getConflicts().size();
            logger.info(",dual: " + dual + "," + matcher + o1 + o2 + ","+matcher + "," + o1 + "," + o2 + ",diagnosis 9 found " + time9+ ","
                    + conflicts9 + "," + extractionTime);
            try {
                searchDual.setMaxDiagnosesNumber(30);
                searchDual.start();
            } catch (SolverException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (NoConflictException e) {
                logger.info(",dual: " + dual + ",no conflicts 30: " + file.getName());
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (InconsistentTheoryException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            long time30 = System.currentTimeMillis() - start;
            int conflicts30 = searchDual.getConflicts().size();
            logger.info(",dual: " + dual + "," + matcher + o1 + o2 + ","+matcher + "," + o1 + "," + o2 + ",diagnosis 30 found "
                    + time30+ "," + conflicts30 + "," + extractionTime);

            int numDiags = searchDual.getDiagnoses().size();
            int numC = searchDual.getConflicts().size();

            Set<Integer> sizes = new LinkedHashSet<Integer>();
            for (FormulaSet<OWLLogicalAxiom> diagnosis : searchDual.getDiagnoses())
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
                    ;//logger.info("diag written file: " + file.getName());
                else logger.info("error " + file.getName());
                cnt++;
            }*/

            /*Set<AxiomSet<OWLLogicalAxiom>> diagnoses = searchDual.getStorage().getDiagnoses();
            boolean found = false;
             Set<OWLLogicalAxiom> targetDg =  CreationUtils.readDiagnosisFromFile(file.getName().substring(0,file.getName().length()-4) + "_1");
            for (AxiomSet<OWLLogicalAxiom> diagnosis : diagnoses) {
                if (diagnosis.size() == targetDg.size() && diagnosis.containsAll(targetDg) &&
                        targetDg.containsAll(diagnosis))
                    found = true;
            }
            logger.info(file.getName() + ",found," + found);*/


            logger.info(", act," + matcher + o1 + o2 + ","+matcher + "," + o1 + "," + o2 + "," + dual +","
                    + time1 + "," + time9 + "," + time30 + ","
                    + conflicts1 + "," + conflicts9 + "," + conflicts30 + "," + extractionTime
                    + "," + numDiags + "," + numC + ","+ numOfMinCardDiags + "," + minCardSize );

            return "";

        }
    }

    private class MyFilenameFilter implements FilenameFilter {
        private Set<String> acceptedNames;

        public MyFilenameFilter(File includedNames) {
            acceptedNames = new LinkedHashSet<String>();
            try{
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(includedNames)));
                String strLine;
                while ((strLine = br.readLine()) != null)   {
                    if (!strLine.startsWith("#") && strLine.endsWith(".rdf"))
                        acceptedNames.add(strLine);
                }
                br.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

        public boolean accept(File dir, String name) {
            return acceptedNames.contains(name);
        }
    }
    @Ignore
    @Test
    public void searchOneDiagTime() throws SolverException, InconsistentTheoryException, NoConflictException {
        String d = "incoherent";
        File incl = new File(ClassLoader.getSystemResource("oaei11conference/matchings/included.txt").getFile());
        MyFilenameFilter filter = new MyFilenameFilter(incl);
        File[] f = new File(ClassLoader.getSystemResource("oaei11conference/matchings/"+d)
                .getFile()).listFiles(filter);

        //Set<String> excluded = new LinkedHashSet<String>();
        //excluded.add("ldoa-conference-iasted.rdf");

        ExecutorService executor = Executors.newCachedThreadPool();

        for (File file : f) {
            if (file.isDirectory())
                continue;

            SearchThread search = new SearchThread(file,true,d);

            Future future = executor.submit(search);
        }

        try {
            executor.awaitTermination(2, TimeUnit.HOURS);
            logger.info("timeout");

        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Ignore
    @Test
    public void testSearchDiagnosisTimes() throws SolverException, InconsistentTheoryException, NoConflictException {
        String d = "incoherent";
        File incl = new File(ClassLoader.getSystemResource("oaei11conference/matchings/includedIncoher.txt").getFile());
        MyFilenameFilter filter = new MyFilenameFilter(incl);
        File[] f = new File(ClassLoader.getSystemResource("oaei11conference/matchings/"+d)
                .getFile()).listFiles(filter);

        //Set<String> excluded = new LinkedHashSet<String>();
        //excluded.add("ldoa-conference-iasted.rdf");

        ExecutorService executor = Executors.newCachedThreadPool();

        for (boolean dual : new boolean[]{true, false}) {
            for (File file : f) {
                if (file.isDirectory())
                    continue;

                SearchThread search = new SearchThread(file, dual, d);

                Future future = executor.submit(search);
            }
        }

        d = "inconsistent";
        incl = new File(ClassLoader.getSystemResource("oaei11conference/matchings/included.txt").getFile());
        filter = new MyFilenameFilter(incl);
        f = new File(ClassLoader.getSystemResource("oaei11conference/matchings/"+d)
                .getFile()).listFiles(filter);

        for (boolean dual : new boolean[]{true, false}) {
            for (File file : f) {
                if (file.isDirectory())
                    continue;

                SearchThread search = new SearchThread(file, dual, d);

                Future future = executor.submit(search);
            }
        }

        try {
            executor.awaitTermination(2,TimeUnit.HOURS);
            logger.info("timeout");

        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Ignore
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
            OWLOntology merged = getOntology("oaei11conference/ontology", o1, o2,
                    "oaei11conference/matchings/incoherent", n + ".rdf");

            /*OWLOntology ontology1 = CreationUtils.createOwlOntology2("oaei11conference/ontology",o1);
            OWLOntology ontology2 = CreationUtils.createOwlOntology2("oaei11conference/ontology",o2);
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
   @Ignore
    @Test
    public void testParser() throws SolverException, InconsistentTheoryException{
        for (File file : new File(ClassLoader.getSystemResource("oaei11conference/matchings").getFile()).listFiles()) {
            String fileName = file.getName();
            StringTokenizer t = new StringTokenizer(fileName,"-");
            String matcher = t.nextToken();
            String o1 = t.nextToken();
            String o2 = t.nextToken();
            o2 = o2.substring(0,o2.length()-4);
            OWLOntology ontology1 = getOntologySimple("oaei11conference/ontology", o1 + ".owl");
            OWLOntology ontology2 = getOntologySimple("oaei11conference/ontology", o2 + ".owl");
            OWLOntology merged = mergeOntologies(ontology1, ontology2);
            String n = file.getName().substring(0,file.getName().length()-4);
            Set<OWLLogicalAxiom> mapping = readRdfMapping("oaei11conference/matchings", n + ".rdf").keySet();
            for (OWLLogicalAxiom axiom : mapping)
                merged.getOWLOntologyManager().applyChange(new AddAxiom(merged, axiom));

            OWLOntology extracted = new OWLIncoherencyExtractor(new Reasoner.ReasonerFactory()).getIncoherentPartAsOntology(merged);

            TreeSearch<? extends FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom> search = new InvHsTreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom>();
            search.setCostsEstimator(new SimpleCostsEstimator<OWLLogicalAxiom>());
            search.setSearchStrategy(new BreadthFirstSearchStrategy<OWLLogicalAxiom>());
            DualTreeOWLTheory theory = null;
            try {
                theory = new DualTreeOWLTheory(new Reasoner.ReasonerFactory(),extracted, Collections.<OWLLogicalAxiom>emptySet());
            }
            catch(InconsistentTheoryException e) {
                logger.info(file.getName() + " cons");
                continue;
            }

            Set<OWLLogicalAxiom> ontology1CutExtracted = createIntersection(extracted.getLogicalAxioms(), ontology1.getLogicalAxioms());
            Set<OWLLogicalAxiom> ontology2CutExtracted = createIntersection(extracted.getLogicalAxioms(), ontology2.getLogicalAxioms());
            theory.getKnowledgeBase().addBackgroundFormulas(ontology1CutExtracted);
            theory.getKnowledgeBase().addBackgroundFormulas(ontology2CutExtracted);

            search.setSearcher(new DirectDiagnosis<OWLLogicalAxiom>());
            search.setSearchable(theory);

            try {
                search.setMaxDiagnosesNumber(9);
                search.start();
                logger.info(file.getName() + " " + search.getDiagnoses().size());
            } catch (NoConflictException e) {
                logger.info(file.getName() + " cons");
                //e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

        }
    }

}
