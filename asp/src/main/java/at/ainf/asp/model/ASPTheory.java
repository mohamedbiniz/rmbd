package at.ainf.asp.model;

import java.util.LinkedHashSet;
import java.util.Set;

import at.ainf.diagnosis.model.AbstractReasoner;
import at.ainf.diagnosis.model.BaseSearchableObject;
import at.ainf.diagnosis.model.IKnowledgeBase;
import at.ainf.diagnosis.model.IReasoner;
import at.ainf.diagnosis.model.SolverException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Melanie Fruehstueck
 *
 */
public class ASPTheory extends BaseSearchableObject<IProgramElement> {

    private static Logger logger = LoggerFactory.getLogger(ASPTheory.class.getName());

    @Override
    public ReasonerASP getReasoner() {
        return (ReasonerASP) super.getReasoner();
    }

    @Override
    public boolean verifyConsistency() throws SolverException {
        LinkedHashSet<IProgramElement> formulasToAdd = new LinkedHashSet<IProgramElement>(getKnowledgeBase().getBackgroundFormulas());
        logger.info("\nFormulas to add:");
        for (IProgramElement pe : formulasToAdd) {
            logger.info(pe.getString());
        }
        Set<IProgramElement> cache = getReasoner().getFormulasCache();
        logger.info("\nFormulas cached:");
        for (IProgramElement pe : cache) {
            logger.info(pe.getString());
        }
        formulasToAdd.removeAll(getReasoner().getFormulasCache());
        getReasoner().addFormulasToCache(formulasToAdd);
        boolean isConsistent = getReasoner().isConsistent();
        ASPOutput output = ASPOutput.getASPOutputInstance();
        if (output.isUnknown()) throw new SolverException("The solver returned UNKNOWN. Probably there are some syntax errors in the ASP file.");
        getReasoner().removeFormulasFromCache(formulasToAdd);
        return isConsistent;
    }



}
