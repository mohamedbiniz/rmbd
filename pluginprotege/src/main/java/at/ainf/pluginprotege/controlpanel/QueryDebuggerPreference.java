package at.ainf.pluginprotege.controlpanel;

import org.protege.editor.core.prefs.Preferences;
import org.protege.editor.core.prefs.PreferencesManager;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 14.09.11
 * Time: 17:31
 * To change this template use File | Settings | File Templates.
 */
public class QueryDebuggerPreference {
    private static QueryDebuggerPreference instance;

    private static final String KEY = "at.ainf.querydebugger";

    private static final String TEST_ABOX = "TestABox";

    private static final String TEST_TBOX = "TestTBox";

    private static final String SEARCH_COMMAND = "SearchCommand";

    private static final String NUM_OF_LEADING_DIAGS = "NumOfLeadingDiags";

    private static final String TEST_INCOHERENCY_INCONSISTENCY = "TestIncoherencyInconsistency";

    private static final String CALC_ALL_DIAGS = "CalcAllDiags";

    private static final String QUERY_MINIMIZER_ACT = "QueryMinimizerAct";

    private static final String SUB_CLASS_OF = "SubClassOf";

    private static final String CLASS_ASSERTION = "ClassAssertion";

    private static final String EQUIVALENT_CLASS = "EquivalentClass";

    private static final String DISJOINT_CLASS = "DisjointClass";

    private static final String PROPERTY_ASSERTION = "PropertyAssertion";

    private static final String INCLUDE_ONTOLOGY_AXIOMS = "IncludeOntologyAxioms";

    private static final String INCLUDE_AXIOMS_REF_THING = "IncludeAxiomsRefThing";

    private static final String SCORING_FUNCTION = "ScoringFunction";

    private static final String PARTITIONING_THRESHOLD = "PartitioningThreshold";


    public static synchronized QueryDebuggerPreference getInstance() {
        if (instance == null) {
            instance = new QueryDebuggerPreference();
        }
        return instance;
    }

    private Preferences getPrefs() {
        return PreferencesManager.getInstance().getApplicationPreferences(KEY);
    }


    public boolean isTestAbox() {
        return getPrefs().getBoolean(TEST_ABOX, true);
    }


    public void setTestAbox(boolean b) {
        getPrefs().putBoolean(TEST_ABOX, b);
    }


    public boolean isTestTbox() {
        return getPrefs().getBoolean(TEST_TBOX, false);
    }

    public void setTestTbox(boolean b) {
        getPrefs().putBoolean(TEST_TBOX, b);
    }

    public void setSearchCommand(String command) {
        getPrefs().putString(SEARCH_COMMAND, command);
    }

    public String getSearchCommand() {
        return getPrefs().getString(SEARCH_COMMAND, "BestFirst");
    }

    public void setNumOfLeadingDiags(int numOfLeadingDiags) {
        getPrefs().putInt(NUM_OF_LEADING_DIAGS, numOfLeadingDiags);
    }

    public int getNumOfLeadingDiags() {
        return getPrefs().getInt(NUM_OF_LEADING_DIAGS, 9);
    }

    public boolean isTestIncoherencyInconsistency() {
        return getPrefs().getBoolean(TEST_INCOHERENCY_INCONSISTENCY, false);
    }

    public void setTestIncoherencyToInconsistency(boolean b) {
        getPrefs().putBoolean(TEST_INCOHERENCY_INCONSISTENCY, b);
    }

    public boolean isCalcAllDiags() {
        return getPrefs().getBoolean(CALC_ALL_DIAGS, false);
    }

    public void setCalcAllDiags(boolean b) {
        getPrefs().putBoolean(CALC_ALL_DIAGS, b);
    }

    public boolean isQueryMinimizerActive() {
        return getPrefs().getBoolean(QUERY_MINIMIZER_ACT, true);
    }

    public void setQueryMinimizerActive(boolean b) {
        getPrefs().putBoolean(QUERY_MINIMIZER_ACT, b);
    }

    public boolean isSubClassOfActivated() {
        return getPrefs().getBoolean(SUB_CLASS_OF, true);
    }

    public void setSubClassOfActivated(boolean b) {
        getPrefs().putBoolean(SUB_CLASS_OF, b);
    }

    public boolean isClassAssertionActivated() {
        return getPrefs().getBoolean(CLASS_ASSERTION, true);
    }

    public void setClassAssertionActivated(boolean b) {
        getPrefs().putBoolean(CLASS_ASSERTION, b);
    }

    public boolean isEquivalentClassActivated() {
        return getPrefs().getBoolean(EQUIVALENT_CLASS, false);
    }

    public void setEquivalentClassActivated(boolean b) {
        getPrefs().putBoolean(EQUIVALENT_CLASS, b);
    }

    public boolean isDisjointClassesActivated() {
        return getPrefs().getBoolean(DISJOINT_CLASS, false);
    }

    public void setDisjointClassesActivated(boolean b) {
        getPrefs().putBoolean(DISJOINT_CLASS, b);
    }

    public boolean isPropertyAssertActivated() {
        return getPrefs().getBoolean(PROPERTY_ASSERTION, false);
    }

    public void setPropertyAssertActivated(boolean b) {
        getPrefs().putBoolean(PROPERTY_ASSERTION, b);
    }

    public boolean isIncludeOntologyAxiomsActivated() {
        return getPrefs().getBoolean(INCLUDE_ONTOLOGY_AXIOMS, true);
    }

    public void setIncludeOntologyAxiomsActivatd(boolean b) {
        getPrefs().putBoolean(INCLUDE_ONTOLOGY_AXIOMS, b);
    }

    public boolean isIncludeTrivialAxiomsActivated() {
        return getPrefs().getBoolean(INCLUDE_AXIOMS_REF_THING, false);
    }

    public void setIncludeTrivialAxiomsActivated(boolean b) {
        getPrefs().putBoolean(INCLUDE_AXIOMS_REF_THING, b);
    }

    public void setScoringFunction(String func) {
        getPrefs().putString(SCORING_FUNCTION, func);
    }

    public String getScoringFunction() {
        return getPrefs().getString(SCORING_FUNCTION, "Entropy");
    }

    public void setPartitioningThres(double func) {
        getPrefs().putDouble(PARTITIONING_THRESHOLD, func);
    }

    public double getPartitioningThres() {
        return getPrefs().getDouble(PARTITIONING_THRESHOLD, 0.01d);
    }

}
