package at.ainf.diagnosis.quickxplain;

import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 06.02.13
 * Time: 14:38
 * To change this template use File | Settings | File Templates.
 */
public interface ModuleProvider<Id> {

    /**
     * Calculates a subset of the given set which is still unsatisfiable
     * @param module unsatisfiable set
     * @return unsatisfiable subset of module
     */
    public Set<Id> getSmallerModule(Set<Id> module);

}
