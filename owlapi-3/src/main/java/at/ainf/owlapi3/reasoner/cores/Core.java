package at.ainf.owlapi3.reasoner.cores;

import at.ainf.owlapi3.reasoner.HornSatReasoner;
import com.hp.hpl.jena.sparql.engine.QueryEngineRegistry;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.IteratorInt;
import org.semanticweb.owlapi.model.OWLClass;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: kostya
 * Date: 14.05.13
 * Time: 20:15
 * To change this template use File | Settings | File Templates.
 */
public class Core extends AbstractCore<CoreSymbol> {

    public Core(HornSatReasoner reasoner, int symbols) {
        super(reasoner, symbols);
    }

    public Core(HornSatReasoner reasoner) {
        super(reasoner);
    }

    @Override
    protected CoreSymbol createSymbol(int lit, CoreSymbol parent, IVecInt clause){
        return new CoreSymbol(lit, parent);
    }

    @Override
    protected CoreSymbol createSymbol(Integer literal, int rootsNumber) {
        return new CoreSymbol(literal, rootsNumber);
    }

    @Override
    protected Collection<CoreSymbol> createFringe() {
        return new LinkedList<CoreSymbol>();
    }

    @Override
    protected CoreSymbol getNextSymbol(Collection<CoreSymbol> fringe) {
        LinkedList<CoreSymbol> fr = (LinkedList<CoreSymbol>) fringe;
        return fr.poll();
    }

}
