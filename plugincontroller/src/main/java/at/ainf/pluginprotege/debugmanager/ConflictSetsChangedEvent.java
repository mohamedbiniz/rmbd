package at.ainf.pluginprotege.debugmanager;

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

    private Set<Set<OWLLogicalAxiom>> conflictSets;

    public ConflictSetsChangedEvent(Object source, Set<Set<OWLLogicalAxiom>> conflictSets) {
        super(source);

        this.conflictSets = conflictSets;
    }

    public Set<Set<OWLLogicalAxiom>> getConflictSets(){
        return conflictSets;
    }


}
