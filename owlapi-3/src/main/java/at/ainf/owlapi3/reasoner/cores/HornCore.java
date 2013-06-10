package at.ainf.owlapi3.reasoner.cores;

import at.ainf.owlapi3.reasoner.HornSatReasoner;
import org.sat4j.specs.IVecInt;

/**
 * Created with IntelliJ IDEA.
 * User: kostya
 * Date: 14.05.13
 * Time: 20:15
 * To change this template use File | Settings | File Templates.
 */
public class HornCore extends Core {


    public HornCore(HornSatReasoner reasoner, int symbols, int constraints) {
        super(reasoner, symbols, constraints);
    }

    public HornCore(HornSatReasoner reasoner) {
        super(reasoner);
    }

    protected boolean isExpandable(IVecInt clause) {
        if (!isBCHornClause(clause)) {
            isHornComplete = false;
            return false;
        }
        return true;
    }
}
