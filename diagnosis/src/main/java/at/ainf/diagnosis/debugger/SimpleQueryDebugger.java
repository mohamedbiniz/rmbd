package at.ainf.diagnosis.debugger;

import at.ainf.diagnosis.Debugger;
import at.ainf.diagnosis.Searchable;
import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.partitioning.CKK;
import at.ainf.diagnosis.partitioning.QueryMinimizer;
import at.ainf.diagnosis.partitioning.scoring.DynamicRiskQSS;
import at.ainf.diagnosis.partitioning.scoring.MinScoreQSS;
import at.ainf.diagnosis.partitioning.scoring.Scoring;
import at.ainf.diagnosis.partitioning.scoring.SplitInHalfQSS;
import at.ainf.diagnosis.quickxplain.DirectDiagnosis;
import at.ainf.diagnosis.quickxplain.QuickXplain;
import at.ainf.diagnosis.storage.FormulaSet;
import at.ainf.diagnosis.storage.Partition;
import at.ainf.diagnosis.tree.HsTreeSearch;
import at.ainf.diagnosis.tree.InvHsTreeSearch;
import at.ainf.diagnosis.tree.SimpleCostsEstimator;
import at.ainf.diagnosis.tree.TreeSearch;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.diagnosis.tree.searchstrategy.BreadthFirstSearchStrategy;

import java.util.Set;
import java.util.TreeSet;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 18.11.11
 * Time: 08:37
 * To change this template use File | Settings | File Templates.
 */
public class SimpleQueryDebugger<Id> {

    public enum Mode {HS_TREE, INV_HS_TREE, HS_TREE_QUERY}

    public enum ScoringFunc {MINSCORE, SPLIT, DYNAMIC};

    protected Searchable<Id> theory;

    protected TreeSearch<FormulaSet<Id>, Id> search;

    private int maxDiags = 9;

    private Mode mode;

    private double thresholdQuery = 0.95;

    private ScoringFunc scoringFunc = ScoringFunc.MINSCORE;

    public SimpleQueryDebugger(Searchable<Id> theory) {
        this (theory, Mode.HS_TREE);
    }

    public SimpleQueryDebugger(Searchable<Id> theory, Mode mode) {
        this.theory = theory;
        this.mode = mode;
        init();
    }

    public void init() {
        //SimpleStorage<Id> storage = new SimpleStorage<Id>();
        if (theory != null) get_Theory().reset();
        if (mode.equals(Mode.HS_TREE) || mode.equals(Mode.HS_TREE_QUERY)) {
            search = new HsTreeSearch<FormulaSet<Id>, Id>();
            search.setSearcher(new QuickXplain<Id>());
        }
        if (mode.equals(Mode.INV_HS_TREE)) {
            search = new InvHsTreeSearch<FormulaSet<Id>, Id>();
            search.setSearcher(new DirectDiagnosis<Id>());
        }
        search.setCostsEstimator(new SimpleCostsEstimator<Id>());
        search.setSearchStrategy(new BreadthFirstSearchStrategy<Id>());
        search.setSearchable(get_Theory());
        //start.setSearcher(new QuickXplain<Id>());
        //start.setSearchable(getSearchable());

    }

    public TreeSearch<FormulaSet<Id>, Id> getSearch() {
        return search;
    }

    public double getThresholdQuery() {
        return thresholdQuery;
    }

    public void setThresholdQuery(double thresholdQuery) {
        this.thresholdQuery = thresholdQuery;
    }

    public ScoringFunc getScoringFunc() {
        return scoringFunc;
    }

    public void setScoringFunc(ScoringFunc scoringFunc) {
        this.scoringFunc = scoringFunc;
    }

    public Set<FormulaSet<Id>> getConflicts() {
        return search.getConflicts();
    }

    public Set<FormulaSet<Id>> getDiagnoses() {
        return search.getDiagnoses();
    }

    public void set_Theory(Searchable<Id> theory) {
        this.theory = theory;
    }

    public Searchable<Id> get_Theory() {
        return theory;
    }

    private void minimizePartitionAx(Partition<Id> query) {
        if (query.partition == null) return;
        QueryMinimizer<Id> mnz = new QueryMinimizer<Id>(query, get_Theory());
        QuickXplain<Id> q = new QuickXplain<Id>();
        try {
            query.partition = q.search(mnz, query.partition, null).iterator().next();
        } catch (NoConflictException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (SolverException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InconsistentTheoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        for (FormulaSet<Id> hs : query.dx) {
            if (!hs.getEntailments().containsAll(query.partition) && !search.getSearchable().diagnosisEntails(hs, query.partition))
                throw new IllegalStateException("DX diagnosis is not entailing a query");
        }


        for (
                FormulaSet<Id> hs
                : query.dnx)

        {
            if (search.getSearchable().diagnosisConsistent(hs, query.partition))
                throw new IllegalStateException("DNX diagnosis might entail a query");
        }

        for (
                FormulaSet<Id> hs
                : query.dz)

        {
            if (search.getSearchable().diagnosisEntails(hs, query.partition) || hs.getEntailments().containsAll(query.partition))
                throw new IllegalStateException("DZ diagnosis entails a query");
            if (!search.getSearchable().diagnosisConsistent(hs, query.partition))
                throw new IllegalStateException("DZ diagnosis entails a query complement");
        }
    }


    public Partition<Id> getQuery() {
        CKK<Id> ckk = new CKK<Id>(theory, getScoring(getScoringFunc()));
        ckk.setThreshold(getThresholdQuery());

        TreeSet<FormulaSet<Id>> set = new TreeSet<FormulaSet<Id>>(this.getDiagnoses());

        Partition<Id> best = null;
        try {
            best = ckk.generatePartition(set);
        } catch (SolverException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InconsistentTheoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


        minimizePartitionAx(best);

        return best;
    }

    private Scoring<Id> getScoring(ScoringFunc func) {
        switch(func) {
            case MINSCORE:
                return new MinScoreQSS<Id>();
            case SPLIT:
                return new SplitInHalfQSS<Id>();
            case DYNAMIC:
                return new DynamicRiskQSS<Id>(0,0.4,0.5);
            default:
                throw new IllegalStateException("");
        }
    }

    public void updateMaxHittingSets(int number) {
        maxDiags = number;
        search.setMaxDiagnosesNumber(maxDiags);

    }

    public Set<FormulaSet<Id>> start() throws SolverException, NoConflictException, InconsistentTheoryException {
        return search.start();
    }

    public void setMaxDiagnosesNumber(int number) {
        search.setMaxDiagnosesNumber(number);
    }

    public int getMaxDiagnosesNumber() {
        return search.getMaxDiagnosesNumber();  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Set<FormulaSet<Id>> resume() throws SolverException, NoConflictException, InconsistentTheoryException {
        search.setMaxDiagnosesNumber(maxDiags);
        return search.start();
    }

    //
    public void reset() {
        maxDiags = 9;
        init();


    }

}
