package at.ainf.protegeview;

import at.ainf.owlcontroller.OWLAxiomKeywordCostsEstimator;
import at.ainf.theory.model.ITheory;
import at.ainf.theory.model.SolverException;
import at.ainf.theory.model.InconsistentTheoryException;
import at.ainf.diagnosis.quickxplain.NewQuickXplain;
import at.ainf.theory.storage.AxiomSet;
import at.ainf.theory.storage.SimpleStorage;
import at.ainf.diagnosis.tree.BreadthFirstSearch;
import at.ainf.diagnosis.tree.TreeSearch;
import at.ainf.diagnosis.tree.UniformCostSearch;
import at.ainf.owlapi3.model.OWLTheory;
import at.ainf.protegeview.backgroundsearch.BackgroundSearcher;
import at.ainf.protegeview.configwizard.DebuggerWizard;
import at.ainf.protegeview.controlpanel.*;
import at.ainf.protegeview.debugmanager.DebugManager;
import at.ainf.protegeview.queryaskingview.QueryShowPanel;
import at.ainf.protegeview.testcasesentailmentsview.SectionType;
import at.ainf.protegeview.testcasesentailmentsview.TcaeFrameSection;
import at.ainf.protegeview.testcasesentailmentsview.TcaeFrameSectionItem;
import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax;
import org.protege.editor.core.ui.wizard.Wizard;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.inference.ProtegeOWLReasonerInfo;
import org.protege.editor.owl.ui.OWLWorkspaceViewsTab;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.*;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.util.InferredAxiomGenerator;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 10.02.11
 * Time: 13:51
 * To change this template use File | Settings | File Templates.
 */
public class

        WorkspaceTab extends OWLWorkspaceViewsTab {

    private DefaultListModel testcasesModel;

    private HashMap<SectionType, TcaeFrameSection> listSections;

    private HashMap<TcaeFrameSection, List<TcaeFrameSectionItem>> sectionItems;

    private boolean init = true;

    private TreeSearch<? extends AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom> search;

    private ITheory<OWLLogicalAxiom> theory;

    private boolean testcasesChange = true;

    private HashMap<ManchesterOWLSyntax, Double> calcMap = new HashMap<ManchesterOWLSyntax, Double>();

    private QueryShowPanel queryShowPanel;


    public void setCalcMap(HashMap<ManchesterOWLSyntax, Double> calcMap) {
        this.calcMap = calcMap;
    }

    private boolean test_Tbox = false;

    private boolean test_Abox = true;





    public void doCalculateHittingSet() {

        //if (getWS().getOwlTheory() == null)  getWS().createOWLTheory();

        //try {

        if (!QueryDebuggerPreference.getInstance().isCalcAllDiags()) {
            getSearch().setMaxHittingSets(QueryDebuggerPreference.getInstance().getNumOfLeadingDiags());
        } else {
            getSearch().setMaxHittingSets(-1);
        }
        //getWS().getSearch().setTheory(getWS().getOwlTheory());

        //OwlControllerMngr.getOWLController().calculateDiags();


        Frame parent = (Frame) SwingUtilities.getAncestorOfClass(Frame.class, this);

        BackgroundSearcher searcher = new BackgroundSearcher(getSearch(), parent);
        //getWS().getSearch().runPostprocessor();
        switch (searcher.doBackgroundSearch()) {
            case FINISHED:
                Collection<? extends AxiomSet<OWLLogicalAxiom>> hittingsets = getSearch().getStorage().getDiagnoses();
                TreeSet<? extends AxiomSet<OWLLogicalAxiom>> hsTree = new TreeSet<AxiomSet<OWLLogicalAxiom>>(hittingsets);
                Set<? extends AxiomSet<OWLLogicalAxiom>> hsReverse = hsTree.descendingSet();
                Collection<? extends AxiomSet<OWLLogicalAxiom>> conflSets = getSearch().getStorage().getConflictSets();
                // addAxiomToResultsList(getConflictSetListModel(), "Conflict Set ", conflSets);
                //addAxiomToResultsList(getHittingSetListModel(), "Diagnosis", hsReverse);


                break;
            case NO_CONFLICT_EXCEPTION:
                JOptionPane.showMessageDialog(null, "There is no conflict left", "NoConflict Exception", JOptionPane.ERROR_MESSAGE);
                break;
            case SOLVER_EXCEPTION:
                JOptionPane.showMessageDialog(null, "There was a solver exception", "SolverException", JOptionPane.ERROR_MESSAGE);
                break;
            case CANCELED:
                break;
            default:
        }
    }



    private JCheckBox test_incoherency_inconsistency_Checkbox = new JCheckBox("incoherency to inconsistency", false);

    private ButtonGroup searchTypeButtonGroup = new ButtonGroup();




    private int numOfLeadingDiagnoses = 9;

    public void doConfigOptions() {
        OptionsDialog.showOptionsDialog();
    }

    public void doConfigurationWizard() {
        DebuggerWizard w = new DebuggerWizard(null, getOWLEditorKit(), this);
        int result = w.showModalDialog();
        if (result == Wizard.FINISH_RETURN_CODE) {
            w.applyPreferences();
            //getConflictSetListModel().clear();
            DebugManager.getInstance().setValidHittingSets(null);
            DebugManager.getInstance().notifyHittingSetsChanged();
            DebugManager.getInstance().setConflictSets(null);
            DebugManager.getInstance().notifyConflictSetsChanged();
            resetSearch();
            createOWLTheory();
            //doAskQueries();
        }
    }

    public void doResetAfterOpt() {
        setTestcasesChange(true);
        DebugManager.getInstance().setConflictSets(null);
        DebugManager.getInstance().notifyConflictSetsChanged();
        //getConflictSetListModel().clear();

        DebugManager.getInstance().setValidHittingSets(null);
        DebugManager.getInstance().notifyHittingSetsChanged();
        //getHittingSetListModel().clear();

        DebugManager.getInstance().notifyResetReq();
        resetSearch();

        resetButtonGroup();
        createOWLTheory();
    }

    public void loadTestcasesAction() {
        try {


            if (getOwlTheory() == null) {

                createOWLTheory();
            }
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            // getOwlTheory().getOntology().getOWLOntologyManager();


            OWLOntology orig = (OWLOntology) getOwlTheory().getOriginalOntology();
            // String prefix = "\"TestcasesAndEnt ";
            // String ss = null;
            // for (OWLAnnotation annotation : orig.getAnnotations()) {

            String b = null;
            for (Iterator<OWLAnnotation> iter = orig.getAnnotations().iterator(); iter.hasNext(); ) {
                OWLAnnotation ann = iter.next();
                if (ann.getValue() instanceof OWLLiteral) {
                    String s = ((OWLLiteral) ann.getValue()).getLiteral();
                    if (s.startsWith("Testcases ")) {
                        b = s.substring("Testcases ".length());

                    }
                }
            }
            if (b == null) {
                return;
            }

            // if (value.startsWith(prefix)) {
            //ss = "\"" + value.subSequence(prefix.length(), value.length()).toString();
            /* break;
            }*/
            /* }
            if (ss == null) {
                return;
            } */
            OWLOntologyDocumentSource source = new StringDocumentSource(b);
            OWLOntology ontology = manager.loadOntologyFromOntologyDocument(source);

            // OWLOntology ontology = manager.loadOntologyFromOntologyDocument(file);

            for (SectionType t : SectionType.values()) {
                getSectionItems().put(getListSections().get(t), new LinkedList<TcaeFrameSectionItem>());
            }

            HashMap<SectionType, HashMap<Integer, Set<OWLLogicalAxiom>>> axioms;
            axioms = new HashMap<SectionType, HashMap<Integer, Set<OWLLogicalAxiom>>>();
            for (SectionType t : SectionType.values()) {
                axioms.put(t, new HashMap<Integer, Set<OWLLogicalAxiom>>());
            }
            for (OWLLogicalAxiom axiom : ontology.getLogicalAxioms()) {
                OWLAnnotation annotation = axiom.getAnnotations().iterator().next();
                OWLAnnotationValue value = annotation.getValue();
                SectionType ty = null;
                Integer number;

                for (SectionType secType : SectionType.values()) {
                    if (((OWLLiteral) value).getLiteral().startsWith(secType.toString())) {
                        ty = secType;
                        break;
                    }
                }
                number = Integer.parseInt(((OWLLiteral) value).getLiteral().substring(ty.toString().length()));

                if (axioms.get(ty).get(number) == null) {
                    axioms.get(ty).put(number, new LinkedHashSet<OWLLogicalAxiom>());
                }
                axioms.get(ty).get(number).add((OWLLogicalAxiom) axiom.getAxiomWithoutAnnotations());
            }
            for (SectionType t : SectionType.values()) {
                for (Integer key : axioms.get(t).keySet()) {
                    addAxiomToSection(axioms.get(t).get(key), getListSections().get(t));
                }
            }

            displaySection(); //workspacetab.tcaeFramelist.displaySection();
            manager.removeOntology(ontology);
            getOWLModelManager().removeOntology(ontology);
        } catch (OWLOntologyCreationException ex) {
            System.out.println("Could not load ontology: " + ex.getMessage());
        }
    }

    public void doResetAct2() {
        setTestcasesChange(true);
        DebugManager.getInstance().setConflictSets(null);
        DebugManager.getInstance().notifyConflictSetsChanged();
        DebugManager.getInstance().setValidHittingSets(null);
        DebugManager.getInstance().notifyHittingSetsChanged();
        DebugManager.getInstance().notifyResetReq();
        resetSearch();
        resetButtonGroup();
        createOWLTheory();
    }

    public void doResetAct() {

        setTestcasesChange(true);
        DebugManager.getInstance().setConflictSets(null);
        DebugManager.getInstance().notifyConflictSetsChanged();
        //getConflictSetListModel().clear();

        DebugManager.getInstance().setValidHittingSets(null);
        DebugManager.getInstance().notifyHittingSetsChanged();
        //getHittingSetListModel().clear();

        DebugManager.getInstance().notifyResetReq();
        resetSearch();

        resetButtonGroup();
        createOWLTheory();
    }

    public int getNumOfLeadingDiagnoses() {
        return numOfLeadingDiagnoses;
    }

    public void setQueryShowPanel(QueryShowPanel queryShowPanel) {
        this.queryShowPanel = queryShowPanel;
    }

    public QueryShowPanel getPanel() {
        return queryShowPanel;
    }

    public void loadProbabilites(HashMap<ManchesterOWLSyntax, Double> map, ManchesterOWLSyntax[] keywords) {


            if (getOwlTheory() == null) {

                createOWLTheory();
            }
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            // getOwlTheory().getOntology().getOWLOntologyManager();


            OWLOntology orig = (OWLOntology) getOwlTheory().getOriginalOntology();
            // String prefix = "\"TestcasesAndEnt ";
            // String ss = null;
            // for (OWLAnnotation annotation : orig.getAnnotations()) {

            String b = null;
            for (Iterator<OWLAnnotation> iter = orig.getAnnotations().iterator(); iter.hasNext(); ) {
                OWLAnnotation ann = iter.next();
                if (ann.getValue() instanceof OWLLiteral) {
                    String s = ((OWLLiteral) ann.getValue()).getLiteral();
                    if (s.startsWith("Probabilities ")) {
                        b = s.substring("Probabilities ".length());

                    }
                }
            }
            if (b == null) {
                return;
            }

            // if (value.startsWith(prefix)) {
            //ss = "\"" + value.subSequence(prefix.length(), value.length()).toString();
            /* break;
            }*/
            /* }
            if (ss == null) {
                return;
            } */

            Map<String,ManchesterOWLSyntax> keywordMaps = new HashMap<String, ManchesterOWLSyntax>();
            for (ManchesterOWLSyntax keyword : keywords)
                keywordMaps.put(keyword.toString(),keyword);

            String probs[] = b.substring(1,b.length()-1).split(", ");
            for (String prob : probs) {
                ManchesterOWLSyntax k = keywordMaps.get(prob.split("=")[0]);
                if(k != null) {
                    map.put(k,Double.parseDouble( prob.split("=")[1])); }
            }




            //String probs[] = map.toString().substring(1,map.toString().length()-1).split(",");

            //probs[0].split("=")[0]


    }

    public void saveProbabilites(HashMap<ManchesterOWLSyntax, Double> map, ManchesterOWLSyntax[] keywords) {


            if (getOwlTheory() == null) {

                createOWLTheory();
            }
            OWLOntologyManager manager = ((OWLOntology)getOwlTheory().getOntology()).getOWLOntologyManager();




            String result = "Probabilities " + map.toString();

            for (Iterator<OWLAnnotation> iter = ((OWLOntology)getOwlTheory().getOriginalOntology()).getAnnotations().iterator(); iter.hasNext(); ) {
                OWLAnnotation ann = iter.next();
                if (ann.getValue() instanceof OWLLiteral) {
                    String s = ((OWLLiteral) ann.getValue()).getLiteral();
                    if (s.startsWith("Probabilities ")) {
                        manager.applyChange(new RemoveOntologyAnnotation(((OWLOntology)getOwlTheory().getOriginalOntology()), ann));
                    }

                }
            }

            OWLLiteral lit = manager.getOWLDataFactory().getOWLLiteral(result);
            IRI iri = OWLRDFVocabulary.RDFS_COMMENT.getIRI();   //  IRI.create("http://www.ainf.at/isbi#comment");
            OWLAnnotation anno = manager.getOWLDataFactory().getOWLAnnotation(manager.getOWLDataFactory().getOWLAnnotationProperty(iri), lit);
            manager.applyChange(new AddOntologyAnnotation((OWLOntology)getOwlTheory().getOriginalOntology(), anno));
            getOWLModelManager().setDirty((OWLOntology)getOwlTheory().getOriginalOntology());



    }




    public void saveTestcasesAction() {
        try {


            HashMap<SectionType, Collection<Set<OWLLogicalAxiom>>> items = new HashMap<SectionType, Collection<Set<OWLLogicalAxiom>>>();
            for (SectionType type : SectionType.values()) {
                items.put(type, getOWLLogicalAxioms(type));
            }

            if (getOwlTheory() == null) {

                createOWLTheory();
            }
            OWLOntologyManager manager = ((OWLOntology)getOwlTheory().getOntology()).getOWLOntologyManager();
            OWLOntology on = null;

            try {
                on = manager.createOntology();
            } catch (OWLOntologyCreationException ex) {
                throw new OWLRuntimeException(ex);
            }

            for (SectionType type : SectionType.values()) {
                Collection<Set<OWLLogicalAxiom>> itemslist = items.get(type);
                int c = 0;
                for (Collection<OWLLogicalAxiom> axiomCollection : itemslist) {
                    for (OWLLogicalAxiom axiom : axiomCollection) {
                        OWLDataFactory factory = manager.getOWLDataFactory();
                        OWLAnnotation annot = factory.getOWLAnnotation(
                                factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_COMMENT.getIRI()),
                                factory.getOWLLiteral(type.toString() + c, "en"));
                        LinkedHashSet<OWLAnnotation> set = new LinkedHashSet<OWLAnnotation>();
                        set.add(annot);
                        axiom = (OWLLogicalAxiom) axiom.getAnnotatedAxiom(set);
                        AddAxiom addAxiom = new AddAxiom(on, axiom);
                        manager.applyChange(addAxiom);
                    }
                    c++;
                }
            }

            /* File file = null;
            JFileChooser fc = workspacetab.fc;

            switch(fc.showSaveDialog(workspacetab)) {
                case JFileChooser.APPROVE_OPTION:
                    file = fc.getSelectedFile();
                    break;
                default:
                    return;
            } */


            RDFXMLOntologyFormat rdfxmlOntologyFormat = new RDFXMLOntologyFormat();
            OWLOntologyDocumentTarget documentTarget = new StringDocumentTarget();

            // manager.saveOntology(on, rdfxmlFormat, IRI.create(file.toURI()));
            manager.saveOntology(on, rdfxmlOntologyFormat, documentTarget);

            manager.removeOntology(on);
            String result = "Testcases " + ((StringDocumentTarget) documentTarget).toString();

            for (Iterator<OWLAnnotation> iter = ((OWLOntology)getOwlTheory().getOriginalOntology()).getAnnotations().iterator(); iter.hasNext(); ) {
                OWLAnnotation ann = iter.next();
                if (ann.getValue() instanceof OWLLiteral) {
                    String s = ((OWLLiteral) ann.getValue()).getLiteral();
                    if (s.startsWith("Testcases ")) {
                        manager.applyChange(new RemoveOntologyAnnotation((OWLOntology)getOwlTheory().getOriginalOntology(), ann));
                    }

                }
            }

            OWLLiteral lit = manager.getOWLDataFactory().getOWLLiteral(result);
            IRI iri = OWLRDFVocabulary.RDFS_COMMENT.getIRI();   //  IRI.create("http://www.ainf.at/isbi#comment");
            OWLAnnotation anno = manager.getOWLDataFactory().getOWLAnnotation(manager.getOWLDataFactory().getOWLAnnotationProperty(iri), lit);
            manager.applyChange(new AddOntologyAnnotation((OWLOntology)getOwlTheory().getOriginalOntology(), anno));
            getOWLModelManager().setDirty((OWLOntology)getOwlTheory().getOriginalOntology());


        } catch (OWLOntologyStorageException ex) {
            System.out.println("Could not save ontology: " + ex.getMessage());
        }
    }


    public void setTestcasesChange(boolean testcasesChange) {
        this.testcasesChange = testcasesChange;
    }

    boolean queryMinimizerActivated = true;

    public void createOWLTheory() {

        SimpleStorage<OWLLogicalAxiom> storage = new SimpleStorage<OWLLogicalAxiom>();

        if (QueryDebuggerPreference.getInstance().getSearchCommand().equals("BestFirst")) {


            search = new UniformCostSearch<OWLLogicalAxiom>(storage);
        } else if (QueryDebuggerPreference.getInstance().getSearchCommand().equals("BreadthFirst")) {
            //SimpleStorage<OWLLogicalAxiom> storage = new SimpleStorage<OWLLogicalAxiom>();

            search = new BreadthFirstSearch<OWLLogicalAxiom>(storage);
        }
        search.setSearcher(new NewQuickXplain<OWLLogicalAxiom>());

        Collection<Set<OWLLogicalAxiom>> posTests = getOWLLogicalAxioms(SectionType.PT);
        Collection<Set<OWLLogicalAxiom>> negTests = getOWLLogicalAxioms(SectionType.NT);
        Collection<Set<OWLLogicalAxiom>> entailedTests = getOWLLogicalAxioms(SectionType.ET);
        Collection<Set<OWLLogicalAxiom>> nentailedTests = getOWLLogicalAxioms(SectionType.NET);

        OWLOntology ontology = getOWLModelManager().getActiveOntology();

        Set<OWLLogicalAxiom> bax = new LinkedHashSet<OWLLogicalAxiom>();

        if (QueryDebuggerPreference.getInstance().isTestAbox()) {
            for (OWLIndividual ind : ontology.getIndividualsInSignature()) {
                bax.addAll(ontology.getClassAssertionAxioms(ind));
                bax.addAll(ontology.getObjectPropertyAssertionAxioms(ind));
            }
        }
        if (QueryDebuggerPreference.getInstance().isTestTbox()) {
            for (OWLAxiom axiom : ontology.getTBoxAxioms(false)) {
                bax.add((OWLLogicalAxiom) axiom);
            }
        }

        if (QueryDebuggerPreference.getInstance().isTestIncoherencyInconsistency()) {
            String iri = "http://ainf.at/testiri#";

            for (OWLClass ind : ontology.getClassesInSignature()) {
                OWLDataFactory fac = getOWLModelManager().getOWLDataFactory();
                OWLIndividual test_individual = fac.getOWLNamedIndividual(IRI.create(iri + "{" + ind.getIRI().getFragment() + "}"));

                bax.add(fac.getOWLClassAssertionAxiom(ind, test_individual));
            }
        }

        OWLReasonerFactory reasonerFactory = null;

        reasonerFactory = getOWLModelManager().getOWLReasonerManager().getCurrentReasonerFactory().getReasonerFactory();
        if (reasonerFactory.getReasonerName().equals("Null Reasoner")) {
            //JOptionPane.showMessageDialog(null, "There is no reasoner selected. ", "No Reasoner Selected", JOptionPane.ERROR_MESSAGE);
            for (ProtegeOWLReasonerInfo reasonerIn : getOWLModelManager().getOWLReasonerManager().getInstalledReasonerFactories()) {
                if (reasonerIn.getReasonerName().startsWith("HermiT "))
                    reasonerFactory = reasonerIn.getReasonerFactory();
            }
            if (reasonerFactory.getReasonerName().startsWith("HermiT")) {
                //JOptionPane.showMessageDialog(null, "The plugin uses HermiT Reasoner for now. To prevent this select a reasoner in the menu", "Using HermiT", JOptionPane.INFORMATION_MESSAGE);
            }
            //return;
        }
        //OWLReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory();

        if (theory != null) {
            ((OWLOntology)theory.getOntology()).getOWLOntologyManager().removeOntology((OWLOntology)theory.getOntology());
            getOWLModelManager().removeOntology((OWLOntology)theory.getOntology());
        }


        try {
            /*test_incoherency_inconsistency_Checkbox.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent itemEvent) {
                    setIncoher_Inconsis_theory(test_incoherency_inconsistency_Checkbox.isSelected());
                } }); */

            HashMap<ManchesterOWLSyntax, Double> map = calcMap;
            List<InferredAxiomGenerator<? extends OWLLogicalAxiom>> axiomGenerators;
            /*if (theory != null) {
                //boolean trivial = theory.isIncludeTrivialEntailments();
                //boolean axioms = theory.isIncludeOntAxioms();
                // axiomGenerators = theory.getAxiomGenerators();
                theory = new ConfEntailmentOwlTheory(reasonerFactory, ontology, bax);

                //getOWLModelManager().getDirtyOntologies().remove(theory.getOntology());
                // theory.setAxiomGenerators(axiomGenerators);
                //theory.setIncludeTrivialEntailments(trivial);  //theory.setIncludeOntAxioms(axioms);

            } else {*/
                theory = new OWLTheory(reasonerFactory, ontology, bax);
                if (QueryDebuggerPreference.getInstance().isTestIncoherencyInconsistency())
                    ((OWLTheory)theory).activateReduceToUns();
                ((OWLTheory)theory).setIncludeSubClassOfAxioms(QueryDebuggerPreference.getInstance().isSubClassOfActivated());
                ((OWLTheory)theory).setIncludeClassAssertionAxioms(QueryDebuggerPreference.getInstance().isClassAssertionActivated());
                ((OWLTheory)theory).setIncludeEquivalentClassAxioms(QueryDebuggerPreference.getInstance().isEquivalentClassActivated());
                ((OWLTheory)theory).setIncludeDisjointClassAxioms(QueryDebuggerPreference.getInstance().isDisjointClassesActivated());
                ((OWLTheory)theory).setIncludePropertyAssertAxioms(QueryDebuggerPreference.getInstance().isPropertyAssertActivated());
                ((OWLTheory)theory).setIncludeReferencingThingAxioms(QueryDebuggerPreference.getInstance().isIncludeTrivialAxiomsActivated());
                ((OWLTheory)theory).setIncludeOntologyAxioms(QueryDebuggerPreference.getInstance().isIncludeOntologyAxiomsActivated());
                /* getOWLModelManager().getDirtyOntologies().remove(theory.getOntology());
            }*/

            search.setTheory(theory);
            OWLAxiomKeywordCostsEstimator es = new OWLAxiomKeywordCostsEstimator(theory);
            es.updateKeywordProb(map);
            ((UniformCostSearch<OWLLogicalAxiom>)search).setCostsEstimator(es);
            for (Set<OWLLogicalAxiom> axiom : posTests) {
                theory.addPositiveTest(axiom);
            }
            for (Set<OWLLogicalAxiom> axiom : negTests) {
                theory.addNegativeTest(axiom);
            }
            for (Set<OWLLogicalAxiom> axiom : entailedTests) {
                theory.addEntailedTest(axiom);
            }
            for (Set<OWLLogicalAxiom> axiom : nentailedTests) {
                theory.addNonEntailedTest(axiom);
            }
        } catch (SolverException e) {
            JOptionPane.showMessageDialog(null, "There was a solver exception", "SolverException", JOptionPane.ERROR_MESSAGE);

        } catch (InconsistentTheoryException e) {
            JOptionPane.showMessageDialog(null, "There was an UnsatisfiableFormulas exception", "InconsistentTheoryException", JOptionPane.ERROR_MESSAGE);


        }


    }

    /*private void setIncoher_Inconsis_theory(boolean incoher_inconsis_th) {
        ((ConfEntailmentOwlTheory) theory).setReduceIncoherencyInconsistency(incoher_inconsis_th); }*/

    public void resetButtonGroup() {
        for (Enumeration<AbstractButton> buttons = searchTypeButtonGroup.getElements(); buttons.hasMoreElements(); ) {
            JRadioButton button = ((JRadioButton) buttons.nextElement());
            if (button.getActionCommand().equals("BestFirst"))
                button.setSelected(true);
            else {
                button.setSelected(false);
            }
        }
    }


    public ITheory<OWLLogicalAxiom> getOwlTheory() {
        return theory;
    }

    public TreeSearch<? extends AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom> getSearch() {
        return search;
    }

    public void resetSearch() {
        search = null;
    }

    public void displaySection() {
        // List<OWLFrameObject> list = new ArrayList<OWLFrameObject>();

        /* TcaeFrameSection.SectionType[] sections = { TcaeFrameSection.SectionType.PT,
       TcaeFrameSection.SectionType.NT,
       TcaeFrameSection.SectionType.ET,
       TcaeFrameSection.SectionType.NET}; */

        testcasesModel.clear();
        for (SectionType sec : SectionType.values()) {
            TcaeFrameSection section = listSections.get(sec);
            testcasesModel.addElement(section);
            for (TcaeFrameSectionItem r : sectionItems.get(section)) {
                testcasesModel.addElement(r);
            }
        }

    }

    public Map<String,String> getStrTestcases() {

        Map<String,String> res = new HashMap<String, String>();

        res.put("PT","");
        res.put("NT","");
        res.put("ET","");
        res.put("NET","");
        String k = "";
        for (int i = 0; i < testcasesModel.size(); i++) {
            String row = testcasesModel.get(i).toString();
            if(row.startsWith("Positive Test Case"))
                k = "PT";
            else if (row.startsWith("Negative Test Case"))
                k = "NT";
            else if (row.startsWith("Entailed"))
                k = "ET";
            else if (row.startsWith("Not entailed"))
                k = "NET";
            else {
                res.put(k,res.get(k) + testcasesModel.get(i).toString() + ";");
            }
        }
        for (String key : res.keySet()) {
            String value = res.get(key);
            if (value.contains("\n")) {
                res.put(key,value.replaceAll("\n",""));
            }
        }
        return res;
    }


    public void addAxiomToSection(Set<OWLLogicalAxiom> axiom, TcaeFrameSection sec) {


        sectionItems.get(sec).add(new TcaeFrameSectionItem(sec.getOWLEditorKit(), sec, null, null, axiom));
        testcasesChange = true;
    }

    // public final JFileChooser fc = new JFileChooser();

    public HashMap<SectionType, TcaeFrameSection> getListSections() {
        return listSections;
    }

    public HashMap<TcaeFrameSection, List<TcaeFrameSectionItem>> getSectionItems() {
        return sectionItems;
    }

    public DefaultListModel getTestcasesModel() {
        return testcasesModel;
    }

    /* public DefaultListModel getConflictSetListModel() {
        return DebugManager.getInstance().getConflictSetListModel();
    }*/

    public boolean isFirst() {
        if (init) {
            init = false;
            return true;
        } else {
            return false;
        }
    }

    public WorkspaceTab() {
        listSections = new HashMap<SectionType, TcaeFrameSection>();
        sectionItems = new HashMap<TcaeFrameSection, List<TcaeFrameSectionItem>>();
        testcasesModel = new DefaultListModel();
        //conflictSetListModel = new DefaultListModel();
        //hittingSetListModel = new DefaultListModel();

        OptionsDialog.createOptionsDialog(this);
        //createOWLTheory();
    }


    public Collection<Set<OWLLogicalAxiom>> getOWLLogicalAxioms(SectionType type) {

        HashSet<Set<OWLLogicalAxiom>> result = new HashSet<Set<OWLLogicalAxiom>>();
        List<TcaeFrameSectionItem> items = sectionItems.get(listSections.get(type));
        if (items == null)
            return result;
        for (TcaeFrameSectionItem item : items)
            result.add(item.getAxioms());
        return result;
    }


}
