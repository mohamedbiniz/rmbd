package at.ainf.protegeview.debugmanager;

import at.ainf.diagnosis.storage.FormulaSet;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import java.util.EventObject;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 10.08.11
 * Time: 18:36
 * To change this template use File | Settings | File Templates.
 */
public class ConflictSetsChangedEvent extends EventObject {

    private Set<? extends FormulaSet<OWLLogicalAxiom>> conflictSets;

    public ConflictSetsChangedEvent(Object source, Set<? extends FormulaSet<OWLLogicalAxiom>> conflictSets) {
        super(source);

        this.conflictSets = conflictSets;
    }

    public Set<? extends FormulaSet<OWLLogicalAxiom>> getConflictSets(){
        return conflictSets;
    }


}
