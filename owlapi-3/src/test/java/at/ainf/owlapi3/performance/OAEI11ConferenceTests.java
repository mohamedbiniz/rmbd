package at.ainf.owlapi3.performance;

import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.quickxplain.DirectDiagnosis;
import at.ainf.diagnosis.quickxplain.NewQuickXplain;
import at.ainf.diagnosis.storage.AxiomSet;
import at.ainf.diagnosis.tree.HsTreeSearch;
import at.ainf.diagnosis.tree.InvHsTreeSearch;
import at.ainf.diagnosis.tree.SimpleCostsEstimator;
import at.ainf.diagnosis.tree.TreeSearch;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.diagnosis.tree.searchstrategy.BreadthFirstSearchStrategy;
import at.ainf.owlapi3.base.OAEI11ConferenceSession;
import at.ainf.owlapi3.base.SimulatedSession;
import at.ainf.owlapi3.costestimation.OWLAxiomCostsEstimator;
import at.ainf.owlapi3.model.DualTreeOWLTheory;
import at.ainf.owlapi3.model.OWLIncoherencyExtractor;
import at.ainf.owlapi3.model.OWLTheory;
import at.ainf.owlapi3.performance.table.TableList;
import at.ainf.owlapi3.base.tools.LogUtil;
import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxOntologyFormat;
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
import java.util.*;
import java.util.concurrent.*;

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

        SimulatedSession session = new SimulatedSession();

        String matchingsDir = "oaei11conference/matchings/";
        String directory = "incoherent";
        //String mapd = matchingsDir + directory;
        File incl = new File(ClassLoader.getSystemResource(matchingsDir + "includedIncoher.txt").getFile());
        MyFilenameFilter filter = new MyFilenameFilter(incl);
        File[] f = new File(ClassLoader.getSystemResource(matchingsDir + directory)
                .getFile()).listFiles(filter);
        String directory2 = "inconsistent";
        File incl2 = new File(ClassLoader.getSystemResource(matchingsDir + "included.txt").getFile());
        MyFilenameFilter filter2 = new MyFilenameFilter(incl2);
        File[] f2 = new File(ClassLoader.getSystemResource(matchingsDir + directory2)
                .getFile()).listFiles(filter2);
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

        session.setShowElRates(false);

        SimulatedSession.QSSType[] qssTypes = new SimulatedSession.QSSType[]
                {SimulatedSession.QSSType.MINSCORE, SimulatedSession.QSSType.SPLITINHALF};

        for (SimulatedSession.TargetSource targetSource : new SimulatedSession.TargetSource[]{SimulatedSession.TargetSource.FROM_30_DIAGS}) {

            for (File file : files) {
                logger.info("processing " + file.getName());

                String out = "STAT, " + file;


                Set<OWLLogicalAxiom> targetDg = getRandomDiag(file, map.get(file));

                for (SimulatedSession.QSSType type : qssTypes) {


                    String fileName = file.getName();
                    StringTokenizer t = new StringTokenizer(fileName, "-");
                    String matcher = t.nextToken();

                    String o1 = t.nextToken();
                    String o2 = t.nextToken();
                    o2 = o2.substring(0, o2.length() - 4);

                    String n = file.getName().substring(0, file.getName().length() - 4);
                    OWLOntology merged = getOntology("oaei11conference/ontology",
                            o1, o2, matchingsDir + map.get(file), n + ".rdf");

                    long preprocessModulExtract = System.currentTimeMillis();
                    OWLOntology ontology = new OWLIncoherencyExtractor(
                            new Reasoner.ReasonerFactory()).getIncoherentPartAsOntology(merged);
                    preprocessModulExtract = System.currentTimeMillis() - preprocessModulExtract;
                    OWLTheory theory = getExtendTheory(ontology, true);
                    TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = getSearch(theory, true);

                    LinkedHashSet<OWLLogicalAxiom> bx = new LinkedHashSet<OWLLogicalAxiom>();
                    OWLOntology ontology1 = getOntologySimple("oaei11conference/ontology", o1 + ".owl");
                    OWLOntology ontology2 = getOntologySimple("oaei11conference/ontology", o2 + ".owl");
                    bx.addAll(getIntersection(ontology.getLogicalAxioms(), ontology1.getLogicalAxioms()));
                    bx.addAll(getIntersection(ontology.getLogicalAxioms(), ontology2.getLogicalAxioms()));
                    theory.addBackgroundFormulas(bx);

                    Map<OWLLogicalAxiom, BigDecimal> map1 = OAEI11ConferenceSession.readRdfMapping(matchingsDir + map.get(file), n + ".rdf");

                    OWLAxiomCostsEstimator es = new OWLAxiomCostsEstimator(theory, map1);


                    search.setCostsEstimator(es);

                    search.reset();


                    TableList e = new TableList();
                    out += "," + type + ",";
                    String message = "act," + file.getName() + "," + map.get(file) + "," + targetSource
                            + "," + type + "," + preprocessModulExtract + "," + randomDiagNr;
                    out += session.simulateQuerySession(search, theory, targetDg, e, type, message, null, null, null);

                }
                logger.info(out);


            }
        }
    }

    private class MyFilenameFilter implements FilenameFilter {
        private Set<String> acceptedNames;

        public MyFilenameFilter(File includedNames) {
            acceptedNames = new LinkedHashSet<String>();
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(includedNames)));
                String strLine;
                while ((strLine = br.readLine()) != null) {
                    if (!strLine.startsWith("#") || !strLine.endsWith(".rdf"))
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

    private Set<OWLLogicalAxiom> getRandomDiag(File file, String directory) throws SolverException, InconsistentTheoryException {
        String matchingsDir = "oaei11conference/matchings/";
        String mapd = matchingsDir + directory;

        String fileName = file.getName();
        StringTokenizer t = new StringTokenizer(fileName, "-");
        String matcher = t.nextToken();

        String o1 = t.nextToken();
        String o2 = t.nextToken();
        o2 = o2.substring(0, o2.length() - 4);

        String n = file.getName().substring(0, file.getName().length() - 4);
        OWLOntology merged = getOntology("oaei11conference/ontology",
                o1, o2, mapd, n + ".rdf");

        OWLOntology ontology = new OWLIncoherencyExtractor(
                new Reasoner.ReasonerFactory()).getIncoherentPartAsOntology(merged);
        OWLTheory theory = getExtendTheory(ontology, true);
        TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = getSearch(theory, true);

        LinkedHashSet<OWLLogicalAxiom> bx = new LinkedHashSet<OWLLogicalAxiom>();
        OWLOntology ontology1 = getOntologySimple("oaei11conference/ontology", o1 + ".owl");
        OWLOntology ontology2 = getOntologySimple("oaei11conference/ontology", o2 + ".owl");
        bx.addAll(getIntersection(ontology.getLogicalAxioms(), ontology1.getLogicalAxioms()));
        bx.addAll(getIntersection(ontology.getLogicalAxioms(), ontology2.getLogicalAxioms()));
        theory.addBackgroundFormulas(bx);

        Map<OWLLogicalAxiom, BigDecimal> map1 = OAEI11ConferenceSession.readRdfMapping(mapd, n + ".rdf");

        OWLAxiomCostsEstimator es = new OWLAxiomCostsEstimator(theory, map1);


        search.setCostsEstimator(es);

        search.reset();


        Set<OWLLogicalAxiom> targetDg = null;


        OWLTheory th30 = getExtendTheory(ontology, true);
        TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search30 = getSearch(th30, true);
        th30.addBackgroundFormulas(bx);
        OWLAxiomCostsEstimator es30 = new OWLAxiomCostsEstimator(th30, OAEI11ConferenceSession.readRdfMapping(mapd, n + ".rdf"));
        search30.setCostsEstimator(es30);

        try {
            search30.run(30);
        } catch (NoConflictException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        Set<AxiomSet<OWLLogicalAxiom>> diagnoses = search30.getDiagnoses();
        int rnd = random.nextInt(diagnoses.size());
        randomDiagNr = rnd;
        logger.info(file.getName() + ",diagnosis selected as target," + rnd);
        targetDg = new LinkedHashSet<OWLLogicalAxiom>((AxiomSet<OWLLogicalAxiom>) diagnoses.toArray()[rnd]);
        logger.info(file.getName() + ",target diagnosis axioms," + LogUtil.renderAxioms(targetDg));

        search30.reset();
        return targetDg;
    }

    int randomDiagNr = -1;

    Random random = new Random(12311);

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
            Set<OWLLogicalAxiom> correctMappingAxioms = OAEI11ConferenceSession.readRdfMapping(refmatchPath, refMatch).keySet();
            ontoBackground.addAll(getIntersection(extracted.getLogicalAxioms(), correctMappingAxioms));

            OWLOntology ontology1 = getOntologySimple("oaei11conference/ontology", o1 + ".owl");
            OWLOntology ontology2 = getOntologySimple("oaei11conference/ontology", o2 + ".owl");
            ontoBackground.addAll(getIntersection(extracted.getLogicalAxioms(), ontology1.getLogicalAxioms()));
            ontoBackground.addAll(getIntersection(extracted.getLogicalAxioms(), ontology2.getLogicalAxioms()));

            TreeSearch<AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom> searchDual;
            if (dual) {
                searchDual = new InvHsTreeSearch<AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom>();
                searchDual.setCostsEstimator(new SimpleCostsEstimator<OWLLogicalAxiom>());
                searchDual.setSearchStrategy(new BreadthFirstSearchStrategy<OWLLogicalAxiom>());
                searchDual.setSearcher(new DirectDiagnosis<OWLLogicalAxiom>());
            }
            else {
                searchDual = new HsTreeSearch<AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom>();
                searchDual.setCostsEstimator(new SimpleCostsEstimator<OWLLogicalAxiom>());
                searchDual.setSearchStrategy(new BreadthFirstSearchStrategy<OWLLogicalAxiom>());
                searchDual.setSearcher(new NewQuickXplain<OWLLogicalAxiom>());
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

            searchDual.setTheory(theory);
            //if (dual) searchDual.setLogic(new DualTreeLogic<AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom>());

            long start = System.currentTimeMillis();
            try {
                searchDual.run(1);
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
                searchDual.run(9);
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
                searchDual.run(30);
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
            for (AxiomSet<OWLLogicalAxiom> diagnosis : searchDual.getDiagnoses())
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

    private class MyFilenameFilter2 implements FilenameFilter {
        private Set<String> acceptedNames;

        public MyFilenameFilter2(File includedNames) {
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

    @Test
    public void searchOneDiagTime() throws SolverException, InconsistentTheoryException, NoConflictException {
        String d = "incoherent";
        File incl = new File(ClassLoader.getSystemResource("oaei11conference/matchings/included.txt").getFile());
        MyFilenameFilter2 filter = new MyFilenameFilter2(incl);
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

    @Test
    public void testSearchDiagnosisTimes() throws SolverException, InconsistentTheoryException, NoConflictException {
        String d = "incoherent";
        File incl = new File(ClassLoader.getSystemResource("oaei11conference/matchings/includedIncoher.txt").getFile());
        MyFilenameFilter2 filter = new MyFilenameFilter2(incl);
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
        filter = new MyFilenameFilter2(incl);
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
            OWLOntology merged = OAEI11ConferenceSession.mergeOntologies(ontology1, ontology2);
            String n = file.getName().substring(0,file.getName().length()-4);
            Set<OWLLogicalAxiom> mapping = OAEI11ConferenceSession.readRdfMapping("oaei11conference/matchings", n + ".rdf").keySet();
            for (OWLLogicalAxiom axiom : mapping)
                merged.getOWLOntologyManager().applyChange(new AddAxiom(merged, axiom));

            OWLOntology extracted = new OWLIncoherencyExtractor(new Reasoner.ReasonerFactory()).getIncoherentPartAsOntology(merged);

            TreeSearch<? extends AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom> search = new InvHsTreeSearch<AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom>();
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

            Set<OWLLogicalAxiom> ontology1CutExtracted = getIntersection(extracted.getLogicalAxioms(), ontology1.getLogicalAxioms());
            Set<OWLLogicalAxiom> ontology2CutExtracted = getIntersection(extracted.getLogicalAxioms(), ontology2.getLogicalAxioms());
            theory.addBackgroundFormulas(ontology1CutExtracted);
            theory.addBackgroundFormulas(ontology2CutExtracted);

            search.setSearcher(new DirectDiagnosis<OWLLogicalAxiom>());
            search.setTheory(theory);

            try {
                search.run(9);
                logger.info(file.getName() + " " + search.getDiagnoses().size());
            } catch (NoConflictException e) {
                logger.info(file.getName() + " cons");
                //e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

        }
    }

}
