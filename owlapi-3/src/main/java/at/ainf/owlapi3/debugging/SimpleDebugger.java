package at.ainf.owlapi3.debugging;

import at.ainf.diagnosis.Searcher;
import at.ainf.theory.model.SolverException;
import at.ainf.theory.model.UnsatisfiableFormulasException;
import at.ainf.diagnosis.quickxplain.NewQuickXplain;
import at.ainf.theory.storage.SimpleStorage;
import at.ainf.diagnosis.tree.BreadthFirstSearch;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: kostya
 * Date: 14.01.2010
 * Time: 09:44:48
 * To change this template use File | Settings | File Templates.
 */
public class SimpleDebugger extends AbstractOWLDebugger implements OWLDebugger {

    public SimpleDebugger() {
        init();
    }

    public void setMaxHittingSets(int number) {
        getStrategy().setMaxHittingSets(number);
    }

    private void init() {
        SimpleStorage<OWLLogicalAxiom> storage = new SimpleStorage<OWLLogicalAxiom>();
        BreadthFirstSearch<OWLLogicalAxiom> strategy = new BreadthFirstSearch<OWLLogicalAxiom>(storage);
        strategy.setSearcher(new NewQuickXplain<OWLLogicalAxiom>());
        setStrategy(strategy);
    }

    public void dispose() {
        init();
    }

    @Override
    public boolean debug() throws OWLException, UnsatisfiableFormulasException {
        getStrategy().setTheory(getTheory());
        try {
            getStrategy().run();
        } catch (SolverException e) {
            throw new RuntimeException(e);
        } catch (NoConflictException e) {
            return false;
        }
        return true;
    }

    public Collection<OWLLogicalAxiom> getFirstConflict(Collection<OWLLogicalAxiom> axioms)
            throws NoConflictException, SolverException {
        Searcher<OWLLogicalAxiom> qx = new NewQuickXplain<OWLLogicalAxiom>();
        return qx.search(getTheory(), axioms);
    }
}
