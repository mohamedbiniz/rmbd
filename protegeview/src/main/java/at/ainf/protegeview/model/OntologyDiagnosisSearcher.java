package at.ainf.protegeview.model;

import at.ainf.diagnosis.model.ITheory;
import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.partitioning.CKK;
import at.ainf.diagnosis.partitioning.QueryMinimizer;
import at.ainf.diagnosis.partitioning.scoring.QSS;
import at.ainf.diagnosis.partitioning.scoring.QSSFactory;
import at.ainf.diagnosis.quickxplain.NewQuickXplain;
import at.ainf.diagnosis.storage.AxiomSet;
import at.ainf.diagnosis.storage.Partition;
import at.ainf.diagnosis.tree.CostsEstimator;
import at.ainf.diagnosis.tree.TreeSearch;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.owlapi3.costestimation.OWLAxiomKeywordCostsEstimator;
import at.ainf.owlapi3.model.OWLTheory;
import at.ainf.protegeview.gui.axiomsetviews.axiomslist.AxiomListItem;
import at.ainf.protegeview.model.configuration.SearchConfiguration;
import at.ainf.protegeview.model.configuration.SearchCreator;
import org.apache.log4j.Logger;
import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.inference.OWLReasonerManager;
import org.protege.editor.owl.model.inference.ProtegeOWLReasonerInfo;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.math.BigDecimal;
import java.util.*;

import static at.ainf.protegeview.model.OntologyDiagnosisSearcher.ErrorStatus.*;


/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 21.08.12
 * Time: 14:09
 * To change this template use File | Settings | File Templates.
 */
public class OntologyDiagnosisSearcher {

    public void updateConfig(SearchConfiguration newConfiguration) {
        getSearchCreator().updateConfig(newConfiguration);
    }

    public void updateProbab(Map<ManchesterOWLSyntax, BigDecimal> map) {
        CostsEstimator<OWLLogicalAxiom> estimator = getSearchCreator().getSearch().getCostsEstimator();
        ((OWLAxiomKeywordCostsEstimator)estimator).updateKeywordProb(map);
    }

    public static enum TestCaseType {POSITIVE_TC, NEGATIVE_TC, ENTAILED_TC, NON_ENTAILED_TC}

    public static enum ErrorStatus {NO_CONFLICT_EXCEPTION, SOLVER_EXCEPTION, INCONSISTENT_THEORY_EXCEPTION,
                                  NO_QUERY, NO_ERROR}

    public static enum QuerySearchStatus { IDLE, SEARCH_DIAG, GENERATING_QUERY, MINIMZE_QUERY, ASKING_QUERY }

    public static enum SearchStatus { IDLE, RUNNING }

    private Logger logger = Logger.getLogger(OntologyDiagnosisSearcher.class);

    private SearchStatus searchStatus = SearchStatus.IDLE;

    private ErrorStatus errorStatus = ErrorStatus.NO_ERROR;

    private QuerySearchStatus querySearchStatus = QuerySearchStatus.IDLE;

    private SearchCreator creator;

    private Set<ChangeListener> changeListeners = new LinkedHashSet<ChangeListener>();

    private Partition<OWLLogicalAxiom> actualQuery;

    private Set<OWLLogicalAxiom> axiomsMarkedEntailed = new LinkedHashSet<OWLLogicalAxiom>();

    private Set<OWLLogicalAxiom> axiomsMarkedNonEntailed = new LinkedHashSet<OWLLogicalAxiom>();

    public OntologyDiagnosisSearcher(OWLEditorKit editorKit) {
        OWLReasonerManager reasonerMan = editorKit.getModelManager().getOWLReasonerManager();
        OWLOntology ontology = editorKit.getModelManager().getActiveOntology();

        creator = new SearchCreator(ontology,reasonerMan);
    }

    protected ErrorStatus getErrorStatus() {
        return errorStatus;
    }

    public SearchCreator getSearchCreator() {
        return creator;
    }

    protected Set<OWLLogicalAxiom> extract(List selectedValuesList) {
        Set<OWLLogicalAxiom> axioms = new LinkedHashSet<OWLLogicalAxiom>();
        for (Object value : selectedValuesList)
            axioms.add(((AxiomListItem) value).getAxiom());
        return axioms;
    }

    public void removeBackgroundAxioms(List selectedValuesList) {
        Set<OWLLogicalAxiom> backgroundAxioms = extract(selectedValuesList);
        try {
            getSearchCreator().getSearch().getTheory().removeBackgroundFormulas(backgroundAxioms);
        } catch (InconsistentTheoryException e) {
            e.printStackTrace();
        } catch (SolverException e) {
            e.printStackTrace();
        }
        notifyListeners();
    }

    public void addBackgroundAxioms(List selectedValuesList) {
        Set<OWLLogicalAxiom> axioms = extract(selectedValuesList);
        try {
            getSearchCreator().getSearch().getTheory().addBackgroundFormulas(axioms);
        } catch (InconsistentTheoryException e) {
            e.printStackTrace();
        } catch (SolverException e) {
            e.printStackTrace();
        }
        notifyListeners();
    }

    public class SearchThrea extends Thread implements ChangeListener {

        private TreeSearch<AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom> search;

        private int number;

        private ErrorHandler errorHandler;

        public SearchThrea(TreeSearch<AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom> search, int number, ErrorHandler errorHandler) {
            this.number = number;
            this.errorHandler = errorHandler;
            this.search = search;
        }

        @Override
        public void run() {

            search.addSearchListener(this);
            searchStatus = SearchStatus.RUNNING;
            notifyListeners();
            try {
                search.run(number);
                errorStatus = NO_ERROR;
            } catch (SolverException e) {
                errorStatus = SOLVER_EXCEPTION;
            } catch (NoConflictException e) {
                errorStatus = NO_CONFLICT_EXCEPTION;
            } catch (InconsistentTheoryException e) {
                errorStatus = INCONSISTENT_THEORY_EXCEPTION;
            }
            searchStatus = SearchStatus.IDLE;
            if (!errorStatus.equals(NO_ERROR))
                errorHandler.errorHappend(errorStatus);

            notifyListeners();
            search.removeSearchListener(this);
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            notifyListeners();
        }

    }

    public SearchStatus getSearchStatus() {
        return searchStatus;
    }

    public QuerySearchStatus getQuerySearchStatus() {
        return querySearchStatus;
    }

    public void doCalculateDiagnosis(ErrorHandler errorHandler) {

        int n = creator.getConfig().numOfLeadingDiags;
        if (creator.getConfig().calcAllDiags)
            n = -1;
        new SearchThrea(creator.getSearch(),n, errorHandler).start();
        logger.debug("model: calculate diagnosis ");
    }

    public void doReset() {
        resetQuery();
        creator.reset();
        notifyListeners();
        logger.debug("model: do reset");
    }

    public boolean isTestcasesEmpty() {
        OWLTheory theory = (OWLTheory) getSearchCreator().getSearch().getTheory();
        return theory.getPositiveTests().isEmpty() && theory.getNegativeTests().isEmpty() &&
                 theory.getEntailedTests().isEmpty() && theory.getNonentailedTests().isEmpty();

    }

    public void doFullReset() {
        resetQuery();
        creator.fullReset();
        notifyListeners();
        logger.debug("model: do fullReset");
    }

    public void doAddBackgroundAxioms(Set<OWLLogicalAxiom> axioms, ErrorHandler errorHandler) {

        OWLTheory theory = (OWLTheory) getSearchCreator().getSearch().getTheory();

        try {
            theory.addBackgroundFormulas(axioms);
            errorStatus = NO_ERROR;
        } catch (InconsistentTheoryException e) {
            errorStatus = INCONSISTENT_THEORY_EXCEPTION;
        } catch (SolverException e) {
            errorStatus = SOLVER_EXCEPTION;
        }

        if(!getErrorStatus().equals(NO_ERROR))
            errorHandler.errorHappend(getErrorStatus());
        notifyListeners();

    }

    public void doAddTestcase(Set<OWLLogicalAxiom> testcase, TestCaseType type, ErrorHandler errorHandler) {
        addNewTestcase(testcase,type);
        if(!getErrorStatus().equals(NO_ERROR))
            errorHandler.errorHappend(getErrorStatus());
        notifyListeners();

    }

    protected void addCheckedPositiveTest(ITheory<OWLLogicalAxiom> theory, Set<OWLLogicalAxiom> axioms) {
        theory.addPositiveTest(axioms);
        try {
            if (!theory.isTestConsistent()) {
                theory.removePositiveTest(axioms);
                errorStatus = SOLVER_EXCEPTION;
            }
            else {
                errorStatus = NO_ERROR;
            }
        } catch (SolverException e) {
            errorStatus = SOLVER_EXCEPTION;
        }
    }

    protected void addCheckedNegativeTest(ITheory<OWLLogicalAxiom> theory, Set<OWLLogicalAxiom> axioms) {
        theory.addNegativeTest(axioms);
        try {
            if (!theory.isTestConsistent()) {
                theory.removeNegativeTest(axioms);
                errorStatus = SOLVER_EXCEPTION;
            }
            else {
                errorStatus = NO_ERROR;
            }
        } catch (SolverException e) {
            errorStatus = SOLVER_EXCEPTION;
        }
    }

    protected void addCheckedEntailedTest(ITheory<OWLLogicalAxiom> theory, Set<OWLLogicalAxiom> axioms) {
        theory.addEntailedTest(axioms);
        try {
            if (!theory.isTestConsistent()) {
                theory.removeEntailedTest(axioms);
                errorStatus = SOLVER_EXCEPTION;
            }
            else {
                errorStatus = NO_ERROR;
            }
        } catch (SolverException e) {
            errorStatus = SOLVER_EXCEPTION;
        }
    }

    protected void addCheckedNonEntailedTest(ITheory<OWLLogicalAxiom> theory, Set<OWLLogicalAxiom> axioms) {
        theory.addNonEntailedTest(axioms);
        try {
            if (!theory.isTestConsistent()) {
                theory.removeNonEntailedTest(axioms);
                errorStatus = SOLVER_EXCEPTION;
            }
            else {
                errorStatus = NO_ERROR;
            }
        } catch (SolverException e) {
            errorStatus = SOLVER_EXCEPTION;
        }
    }


    protected void addNewTestcase(Set<OWLLogicalAxiom> testcase, TestCaseType type) {

        OWLTheory theory = (OWLTheory) getSearchCreator().getSearch().getTheory();

            switch(type) {
                case POSITIVE_TC:
                    addCheckedPositiveTest(theory,testcase);
                    break;
                case NEGATIVE_TC:
                    addCheckedNegativeTest(theory,testcase);
                case ENTAILED_TC:
                    addCheckedEntailedTest(theory,testcase);
                case NON_ENTAILED_TC:
                    addCheckedNonEntailedTest(theory,testcase);
            }

    }

    public void doUpdateTestcase(Set<OWLLogicalAxiom> oldTest, Set<OWLLogicalAxiom> newTest, TestCaseType type, ErrorHandler errorHandler) {
        doRemoveTestcase(oldTest, type);
        addNewTestcase(newTest, type);
        if(getErrorStatus().equals(INCONSISTENT_THEORY_EXCEPTION)) {
            addNewTestcase(oldTest, type);
            errorHandler.errorHappend(INCONSISTENT_THEORY_EXCEPTION);
        }
        notifyListeners();

    }

    public void doRemoveTestcase(Set<OWLLogicalAxiom> testcase, TestCaseType type) {

        OWLTheory theory = (OWLTheory) getSearchCreator().getSearch().getTheory();

        switch(type) {
            case POSITIVE_TC:
                theory.removePositiveTest(testcase);
                break;
            case NEGATIVE_TC:
                theory.removeNegativeTest(testcase);
                break;
            case ENTAILED_TC:
                theory.removeEntailedTest(testcase);
                break;
            case NON_ENTAILED_TC:
                theory.removeNonEntailedTest(testcase);
                break;
        }
        notifyListeners();


    }

    public class QueryGenerationThread extends Thread {

        private TreeSearch<AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom> search;

        private SearchConfiguration searchConfig;

        private ErrorHandler errorHandler;

        public QueryGenerationThread(SearchCreator searchCreator, ErrorHandler errorHandler) {
            searchConfig = searchCreator.getConfig();
            this.errorHandler = errorHandler;
            search = searchCreator.getSearch();

        }

        private void minimizePartitionAx(Partition<OWLLogicalAxiom> query) {
            if (query.partition == null) return;
            QueryMinimizer<OWLLogicalAxiom> mnz = new QueryMinimizer<OWLLogicalAxiom>(query, search.getTheory());
            NewQuickXplain<OWLLogicalAxiom> q = new NewQuickXplain<OWLLogicalAxiom>();
            try {
                query.partition = q.search(mnz, query.partition, null);
            } catch (NoConflictException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (SolverException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (InconsistentTheoryException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            for (AxiomSet<OWLLogicalAxiom> hs : query.dx) {
                if (!hs.getEntailments().containsAll(query.partition) && !search.getTheory().diagnosisEntails(hs, query.partition))
                    throw new IllegalStateException("DX diagnosis is not entailing a query");
            }


            for (AxiomSet<OWLLogicalAxiom> hs : query.dnx) {
                if (search.getTheory().diagnosisConsistent(hs, query.partition))
                    throw new IllegalStateException("DNX diagnosis might entail a query");
            }

            for (AxiomSet<OWLLogicalAxiom> hs : query.dz) {
                if (search.getTheory().diagnosisEntails(hs, query.partition) || hs.getEntailments().containsAll(query.partition))
                    throw new IllegalStateException("DZ diagnosis entails a query");
                if (!search.getTheory().diagnosisConsistent(hs, query.partition))
                    throw new IllegalStateException("DZ diagnosis entails a query complement");
            }
        }

        @Override
        public void run() {
            querySearchStatus = QuerySearchStatus.SEARCH_DIAG;
            notifyListeners();
            SearchThrea searchThread = new SearchThrea(search,searchConfig.numOfLeadingDiags,errorHandler);
            searchThread.start();
            try {
                searchThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            querySearchStatus = QuerySearchStatus.GENERATING_QUERY;
            notifyListeners();
            QSS<OWLLogicalAxiom> qss;
            switch (searchConfig.qss) {
                case MINSCORE:
                    qss = QSSFactory.createMinScoreQSS();
                    break;
                case SPLIT:
                    qss = QSSFactory.createSplitInHalfQSS();
                    break;
                case DYNAMIC:
                    qss = QSSFactory.createDynamicRiskQSS(0, 0.5, 0.4);
                    break;
                default:
                    throw new IllegalStateException("qss unknown");
            }

            CKK<OWLLogicalAxiom> ckk = new CKK<OWLLogicalAxiom>(search.getTheory(), qss);
            ckk.setThreshold(searchConfig.entailmentCalThres);

            TreeSet<AxiomSet<OWLLogicalAxiom>> set = new TreeSet<AxiomSet<OWLLogicalAxiom>>(search.getDiagnoses());

            if (set.isEmpty()) {
                errorStatus = NO_QUERY;
                resetQuery();
                notifyListeners();
                errorHandler.errorHappend(NO_QUERY);
                return;
            }

            Partition<OWLLogicalAxiom> best = null;
            try {
                best = ckk.generatePartition(set);
            } catch (SolverException e) {
                // e.printStackTrace();
            } catch (InconsistentTheoryException e) {
                // e.printStackTrace();
            }

            if (best==null) {
                errorStatus = NO_QUERY;
                resetQuery();
                notifyListeners();
                errorHandler.errorHappend(NO_QUERY);
                return;
            }

            if (searchConfig.minimizeQuery) {
                querySearchStatus = QuerySearchStatus.MINIMZE_QUERY;
                notifyListeners();
                minimizePartitionAx(best);
            }

            actualQuery = best;
            querySearchStatus = QuerySearchStatus.ASKING_QUERY;
            errorStatus = NO_ERROR;
            notifyListeners();

        }
    }

    public void doRemoveAxiomsMarkedEntailed(OWLLogicalAxiom axiom) {
        axiomsMarkedEntailed.remove(axiom);
        notifyListeners();
    }

    public void doRemoveAxiomsMarkedNonEntailed(OWLLogicalAxiom axiom) {
        axiomsMarkedNonEntailed.remove(axiom);
        notifyListeners();
    }

    public Partition<OWLLogicalAxiom> getActualQuery() {
        return actualQuery;
    }

    public void doGetQuery(ErrorHandler errorHandler) {
        new QueryGenerationThread(getSearchCreator(), errorHandler).start();

    }

    public void doGetAlternativeQuery() {
        JOptionPane.showMessageDialog(null, "The function is not implemented yet", "Not Implemented", JOptionPane.INFORMATION_MESSAGE);
    }

    public boolean isMarkedEntailed(OWLLogicalAxiom axiom) {
        return axiomsMarkedEntailed.contains(axiom);
    }

    public boolean isMarkedNonEntailed(OWLLogicalAxiom axiom) {
        return axiomsMarkedNonEntailed.contains(axiom);
    }

    public void doAddAxiomsMarkedEntailed(OWLLogicalAxiom axiom) {
        axiomsMarkedEntailed.add(axiom);
        notifyListeners();
    }

    public void doAddAxiomsMarkedNonEntailed(OWLLogicalAxiom axiom) {
        axiomsMarkedNonEntailed.add(axiom);
        notifyListeners();
    }

    public void doCommitQuery() {
        doAddTestcase(new LinkedHashSet<OWLLogicalAxiom>(axiomsMarkedEntailed),TestCaseType.ENTAILED_TC, new ErrorHandler());
        doAddTestcase(new LinkedHashSet<OWLLogicalAxiom>(axiomsMarkedNonEntailed), TestCaseType.NON_ENTAILED_TC, new ErrorHandler());
        resetQuery();
        notifyListeners();
    }

    protected void resetQuery() {
        axiomsMarkedEntailed.clear();
        axiomsMarkedNonEntailed.clear();
        actualQuery=null;
        querySearchStatus = QuerySearchStatus.IDLE;
    }

    public void doCommitAndGetNewQuery(ErrorHandler errorHandler) {
        doCommitQuery();
        doGetQuery(errorHandler);
    }

    public void addChangeListener(ChangeListener listener) {
        changeListeners.add(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeListeners.remove(listener);
    }

    protected void notifyListeners() {
        for (ChangeListener listener : changeListeners)
            listener.stateChanged(new ChangeEvent(this));
    }

}
