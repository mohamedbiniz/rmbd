package at.ainf.diagnosis.tree.splitstrategy;

import at.ainf.diagnosis.storage.AxiomSet;

import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: chicco
 * Date: 27.11.12
 * Time: 12:38
 * To change this template use File | Settings | File Templates.
 */
public interface SplitStrategy<Id> {

    public Id getSplitElement(Set<AxiomSet<Id>> conflicts);


}
