package at.ainf.owlcontroller.listeners;

import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 10.01.12
 * Time: 15:12
 * To change this template use File | Settings | File Templates.
 */
public interface OWLControllerConflictSetListener extends OWLControllerListener {

    void updateConflictSets (Set<Set<OWLLogicalAxiom>> conflictSets);

}
