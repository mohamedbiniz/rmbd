package at.ainf.protegeview.debugmanager;

import at.ainf.diagnosis.storage.FormulaSet;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import java.util.EventObject;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 09.08.11
 * Time: 11:18
 * To change this template use File | Settings | File Templates.
 */
public class HittingSetsChangedEvent extends EventObject {

    private Set<? extends FormulaSet<OWLLogicalAxiom>> valHS;

    public HittingSetsChangedEvent(Object source, Set<? extends FormulaSet<OWLLogicalAxiom>> valHS) {
        super(source);

        this.valHS = valHS;
    }

    public Set<? extends FormulaSet<OWLLogicalAxiom>> getValidHS(){
        return valHS;
    }


}
