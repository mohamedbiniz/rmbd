package at.ainf.diagnosis.debugger;

import at.ainf.diagnosis.Searchable;
import at.ainf.diagnosis.partitioning.CKK;
import at.ainf.diagnosis.partitioning.QueryMinimizer;
import at.ainf.diagnosis.partitioning.scoring.Scoring;
import at.ainf.diagnosis.quickxplain.QuickXplain;
import at.ainf.diagnosis.tree.HsTreeSearch;
import at.ainf.diagnosis.tree.SimpleCostsEstimator;
import at.ainf.diagnosis.tree.TreeSearch;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.diagnosis.tree.searchstrategy.BreadthFirstSearchStrategy;
import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.storage.*;

import java.util.Set;
import java.util.TreeSet;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 18.11.11
 * Time: 08:37
 * To change this template use File | Settings | File Templates.
 */
public class SimpleQueryDebugger<Id> implements QueryDebugger<Id> {


    protected Searchable<Id> theory;

    protected TreeSearch<? extends AxiomSet<Id>, Id> search;

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
        search = new HsTreeSearch<AxiomSet<Id>,Id>();
        search.setCostsEstimator(new SimpleCostsEstimator<Id>());
        search.setSearchStrategy(new BreadthFirstSearchStrategy<Id>());
        search.setSearcher(new QuickXplain<Id>());
        search.setSearchable(getTheory());
        //start.setSearcher(new QuickXplain<Id>());
        //start.setSearchable(getSearchable());

    }

    public Set<? extends AxiomSet<Id>> getConflictSets() {
        return search.getConflicts();
    }

    public Set<? extends AxiomSet<Id>> getHittingSets() {
        return search.getDiagnoses();
    }

    public Set<? extends AxiomSet<Id>> getValidHittingSets() {
        return search.getDiagnoses();
    }

    public void set_Theory(Searchable<Id> theory) {
        this.theory = theory;
    }

    public Searchable<Id> getTheory() {
        return theory;
    }

    private void minimizePartitionAx(Partition<Id> query) {
        if (query.partition == null) return;
        QueryMinimizer<Id> mnz = new QueryMinimizer<Id>(query, getTheory());
        QuickXplain<Id> q = new QuickXplain<Id>();
        try {
            query.partition = q.search(mnz, query.partition, null);
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

        TreeSet<AxiomSet<Id>> set = new TreeSet<AxiomSet<Id>>(getValidHittingSets());

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

    public boolean debug() {
        try {
            search.start();
        } catch (SolverException e) {
            return false;
        } catch (NoConflictException e) {
            return false;
        } catch (InconsistentTheoryException e) {
            return false;
        }
        return true;

    }

    public boolean resume() {
        try {
            search.setMaxDiagnosesNumber(maxDiags);
            search.resume();
        } catch (SolverException e) {
            return false;
        } catch (NoConflictException e) {
            return false;
        } catch (InconsistentTheoryException e) {
            return false;
        }
        return true;

    }

    //
    public void reset() {
        maxDiags = 9;
        init();


    }

}
