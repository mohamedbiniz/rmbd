package at.ainf.diagnosis.debugger;

import at.ainf.diagnosis.Debugger;
import at.ainf.diagnosis.Searchable;
import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.partitioning.CKK;
import at.ainf.diagnosis.partitioning.QueryMinimizer;
import at.ainf.diagnosis.partitioning.scoring.Scoring;
import at.ainf.diagnosis.quickxplain.QuickXplain;
import at.ainf.diagnosis.storage.AxiomSet;
import at.ainf.diagnosis.storage.Partition;
import at.ainf.diagnosis.tree.HsTreeSearch;
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
public class SimpleQueryDebugger<Id> implements Debugger<AxiomSet<Id>, Id> {


    protected Searchable<Id> theory;

    protected TreeSearch<AxiomSet<Id>, Id> search;

    private int maxDiags = 9;

    protected SimpleQueryDebugger(Searchable<Id> theory, boolean init) {
        this.theory = theory;
        if (init)
            init();
    }

    public SimpleQueryDebugger(Searchable<Id> theory) {
        this(theory, true);
    }

    public void init() {
        //SimpleStorage<Id> storage = new SimpleStorage<Id>();
        if (theory != null) getTheory().reset();
        search = new HsTreeSearch<AxiomSet<Id>, Id>();
        search.setCostsEstimator(new SimpleCostsEstimator<Id>());
        search.setSearchStrategy(new BreadthFirstSearchStrategy<Id>());
        search.setSearcher(new QuickXplain<Id>());
        search.setSearchable(getTheory());
        //start.setSearcher(new QuickXplain<Id>());
        //start.setSearchable(getSearchable());

    }

    public Set<AxiomSet<Id>> getConflicts() {
        return search.getConflicts();
    }

    public Set<AxiomSet<Id>> getDiagnoses() {
        return search.getDiagnoses();
    }

    public void set_Theory(Searchable<Id> theory) {
        this.theory = theory;
    }

    private Searchable<Id> getTheory() {
        return theory;
    }

    private void minimizePartitionAx(Partition<Id> query) {
        if (query.partition == null) return;
        QueryMinimizer<Id> mnz = new QueryMinimizer<Id>(query, getTheory());
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

        for (AxiomSet<Id> hs : query.dx) {
            if (!hs.getEntailments().containsAll(query.partition) && !search.getSearchable().diagnosisEntails(hs, query.partition))
                throw new IllegalStateException("DX diagnosis is not entailing a query");
        }


        for (
                AxiomSet<Id> hs
                : query.dnx)

        {
            if (search.getSearchable().diagnosisConsistent(hs, query.partition))
                throw new IllegalStateException("DNX diagnosis might entail a query");
        }

        for (
                AxiomSet<Id> hs
                : query.dz)

        {
            if (search.getSearchable().diagnosisEntails(hs, query.partition) || hs.getEntailments().containsAll(query.partition))
                throw new IllegalStateException("DZ diagnosis entails a query");
            if (!search.getSearchable().diagnosisConsistent(hs, query.partition))
                throw new IllegalStateException("DZ diagnosis entails a query complement");
        }
    }


    public Partition<Id> getQuery(Scoring<Id> func, boolean minimize, double acceptanceThreshold) {
        CKK<Id> ckk = new CKK<Id>(theory, func);
        ckk.setThreshold(acceptanceThreshold);

        TreeSet<AxiomSet<Id>> set = new TreeSet<AxiomSet<Id>>(this.getDiagnoses());

        Partition<Id> best = null;
        try {
            best = ckk.generatePartition(set);
        } catch (SolverException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InconsistentTheoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        if (minimize)
            minimizePartitionAx(best);

        return best;
    }

    public void updateMaxHittingSets(int number) {
        maxDiags = number;
        search.setMaxDiagnosesNumber(maxDiags);

    }

    @Override
    public Set<AxiomSet<Id>> start() throws SolverException, NoConflictException, InconsistentTheoryException {
        return search.start();
    }

    @Override
    public void setMaxDiagnosesNumber(int number) {
        search.setMaxDiagnosesNumber(number);
    }

    @Override
    public int getMaxDiagnosesNumber() {
        return search.getMaxDiagnosesNumber();  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Set<AxiomSet<Id>> resume() throws SolverException, NoConflictException, InconsistentTheoryException {
        search.setMaxDiagnosesNumber(maxDiags);
        return search.resume();
    }

    //
    public void reset() {
        maxDiags = 9;
        init();


    }

}
