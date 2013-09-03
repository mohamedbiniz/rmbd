package at.ainf.protegeview.debugmanager;

import at.ainf.protegeview.views.ResultsListSection;

import java.util.EventObject;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 30.09.11
 * Time: 10:25
 * To change this template use File | Settings | File Templates.
 */
public class EntailmentsShowEvent extends EventObject {

    private Set<ResultsListSection> entHS;

    public EntailmentsShowEvent(Object source, Set<ResultsListSection> entHS) {
        super(source);

        this.entHS = entHS;

    }

    public Set<ResultsListSection> getEntHSets(){
        return entHS;
    }
}
