package at.ainf.owlcontroller.listeners;

import at.ainf.theory.storage.HittingSet;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 10.01.12
 * Time: 15:11
 * To change this template use File | Settings | File Templates.
 */
public interface OWLControllerHittingSetListener extends OWLControllerListener {

    void updateValidHittingSets (Set<? extends HittingSet<OWLLogicalAxiom>> validHittingSets);

}
