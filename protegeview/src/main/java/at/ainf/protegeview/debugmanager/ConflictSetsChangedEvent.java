package at.ainf.protegeview.debugmanager;

import at.ainf.theory.storage.AxiomSet;
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

    private Set<? extends AxiomSet<OWLLogicalAxiom>> conflictSets;

    public ConflictSetsChangedEvent(Object source, Set<? extends AxiomSet<OWLLogicalAxiom>> conflictSets) {
        super(source);

        this.conflictSets = conflictSets;
    }

    public Set<? extends AxiomSet<OWLLogicalAxiom>> getConflictSets(){
        return conflictSets;
    }


}
