package at.ainf.controller;

import at.ainf.diagnosis.debugger.ProbabilityQueryDebugger;
import at.ainf.diagnosis.debugger.QueryDebugger;
import at.ainf.diagnosis.debugger.SimpleQueryDebugger;
import at.ainf.diagnosis.tree.NodeCostsEstimator;
import at.ainf.diagnosis.tree.UniformCostSearch;
import at.ainf.theory.model.ITheory;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.PicoContainer;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 20.12.11
 * Time: 13:58
 * To change this template use File | Settings | File Templates.
 */
public abstract class QueryDebuggerCont {

    private DefaultPicoContainer pico = new DefaultPicoContainer();

    public QueryDebuggerCont() {
        pico.addComponent(QueryDebugger.class);
        pico.addComponent(SimpleQueryDebugger.class);
        pico.addComponent(ProbabilityQueryDebugger.class);
        pico.addComponent(NodeCostsEstimator.class);
    }

    public DefaultPicoContainer getPico() {
        return pico;
    }

}
