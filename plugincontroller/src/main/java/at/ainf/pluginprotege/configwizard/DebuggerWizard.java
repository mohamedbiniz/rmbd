package at.ainf.pluginprotege.configwizard;

import at.ainf.pluginprotege.WorkspaceTab;
import at.ainf.pluginprotege.configwizard.frames.*;
import at.ainf.pluginprotege.controlpanel.OptionsDialog;
import at.ainf.pluginprotege.controlpanel.QueryDebuggerPreference;
import org.protege.editor.core.ui.wizard.Wizard;
import org.protege.editor.owl.OWLEditorKit;

import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 18.07.11
 * Time: 15:56
 * To change this template use File | Settings | File Templates.
 */
public class DebuggerWizard extends Wizard {

    private WelcomePanel welcomePanel;

    private ChooseBoxes chooseBoxesPanel;

    private ChooseSearchType chooseSearchType;

    private ErrorProbabilityPanel errorProbabilityPanel;

    private NumLeadingDiags numLeadingDiags;

    private ChoScoringFunction choScoringFunc;

    private QueryMinimizer queryMinimizer;

    private ReduceIncoherencyIncon reduceIncoherencyInco;

    private IncludeEntailments includeEntailments;

    private OntologyAxioms ontologyAxioms;

    private FinishPanel finishPanel;

    private WorkspaceTab workspaceTab;

    public DebuggerWizard(Frame owner, OWLEditorKit editorKit, WorkspaceTab workspaceTab) {
        super(owner);
        this.workspaceTab = workspaceTab;

        setTitle("Configuration wizard");
        registerWizardPanel(WelcomePanel.ID, welcomePanel = new WelcomePanel(editorKit, 1));
        registerWizardPanel(ChooseBoxes.ID, chooseBoxesPanel = new ChooseBoxes(editorKit, 2));
        registerWizardPanel(ChooseSearchType.ID, chooseSearchType = new ChooseSearchType(editorKit, 3));
        registerWizardPanel(ErrorProbabilityPanel.ID, errorProbabilityPanel = new ErrorProbabilityPanel(editorKit, 4));
        registerWizardPanel(NumLeadingDiags.ID, numLeadingDiags = new NumLeadingDiags(editorKit, 5));
        registerWizardPanel(ChoScoringFunction.ID, choScoringFunc = new ChoScoringFunction(editorKit, 6));
        registerWizardPanel(QueryMinimizer.ID, queryMinimizer = new QueryMinimizer(editorKit, 7));
        registerWizardPanel(ReduceIncoherencyIncon.ID, reduceIncoherencyInco = new ReduceIncoherencyIncon(editorKit, 8));
        registerWizardPanel(IncludeEntailments.ID, includeEntailments = new IncludeEntailments(editorKit, 9));
        registerWizardPanel(OntologyAxioms.ID, ontologyAxioms = new OntologyAxioms(editorKit, 10));
        registerWizardPanel(FinishPanel.ID, finishPanel = new FinishPanel(editorKit, 11));
        setCurrentPanel(WelcomePanel.ID);
    }

    public void applyPreferences() {
        QueryDebuggerPreference.getInstance().setTestAbox(chooseBoxesPanel.isABoxSelected());
        QueryDebuggerPreference.getInstance().setTestTbox(chooseBoxesPanel.isTBoxSelected());
        QueryDebuggerPreference.getInstance().setSearchCommand(chooseSearchType.getSearchCmd());
        QueryDebuggerPreference.getInstance().setNumOfLeadingDiags(numLeadingDiags.getNumLeadingDiags());
        QueryDebuggerPreference.getInstance().setTestIncoherencyToInconsistency(reduceIncoherencyInco.isReductionInconsistencySelected());
        QueryDebuggerPreference.getInstance().setScoringFunction(choScoringFunc.getScoringFunctionCmd());

        /*if (includeEntailments.isSubClassAxSelected())
            axiomGenerators.add(new InferredSubClassAxiomGenerator());
        if (includeEntailments.isClassAssertionAxSelected())
            axiomGenerators.add(new InferredClassAssertionAxiomGenerator());
        if (includeEntailments.isEquivalentClassAxSelected())
            axiomGenerators.add(new InferredEquivalentClassAxiomGenerator());
        if (includeEntailments.isDisjointClassesAxSelected())
            axiomGenerators.add(new InferredDisjointClassesAxiomGenerator());
        if (includeEntailments.isPropertyAssertionAxSelected())
            axiomGenerators.add(new InferredPropertyAssertionGenerator()); */
        QueryDebuggerPreference.getInstance().setSubClassOfActivated(includeEntailments.isSubClassAxSelected());
        QueryDebuggerPreference.getInstance().setClassAssertionActivated(includeEntailments.isClassAssertionAxSelected());
        QueryDebuggerPreference.getInstance().setEquivalentClassActivated(includeEntailments.isEquivalentClassAxSelected());
        QueryDebuggerPreference.getInstance().setDisjointClassesActivated(includeEntailments.isDisjointClassesAxSelected());
        QueryDebuggerPreference.getInstance().setPropertyAssertActivated(includeEntailments.isPropertyAssertionAxSelected());


        OptionsDialog.getDialog().getProbabTableModel().updateCalcMapWithMap();


        QueryDebuggerPreference.getInstance().setQueryMinimizerActive(queryMinimizer.isMinimizerSelected());


        QueryDebuggerPreference.getInstance().setIncludeTrivialAxiomsActivated(ontologyAxioms.isTrivialAxSelected());
        QueryDebuggerPreference.getInstance().setIncludeOntologyAxiomsActivatd(ontologyAxioms.isOntologyAxiomsSelected());

    }


}
