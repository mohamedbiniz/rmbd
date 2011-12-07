package at.ainf.pluginprotege.debugmanager;

import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import java.util.EventObject;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 23.08.11
 * Time: 13:50
 * To change this template use File | Settings | File Templates.
 */
public class ConflictSetSelectedEvent extends EventObject {

    private int number;

    public ConflictSetSelectedEvent(Object source, int number) {
        super(source);

        this.number = number;
    }

    public int getConflictSetNumber(){
        return number;
    }
}
