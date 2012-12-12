/*
 * Copyright (c) 2009 Kostyantyn Shchekotykhin
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * http://www.gnu.org/licenses/gpl.txt
 */

/*
 * Created on 02.12.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package at.ainf.diagnosis.quickxplain;

import at.ainf.diagnosis.Searchable;
import at.ainf.diagnosis.model.AbstractReasoner;
import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.storage.FormulaRenderer;
import at.ainf.diagnosis.storage.FormulaSet;
import at.ainf.diagnosis.storage.FormulaSetImpl;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import static _dev.TimeLog.start;

/**
 * @author kostya TODO To change the template for this generated type comment go
 *         to Window - Preferences - Java - Code Style - Code Templates
 */
public class QuickXplain<Id> extends BaseQuickXplain<Id> {

    private int iterations = 0;

    private static Logger logger = LoggerFactory.getLogger(QuickXplain.class.getName());

    private FormulaRenderer<Id> formulaRenderer;
    private QXAxiomListener<Id> axiomListener = null;

    public QuickXplain() {
    }

    @Override
    protected Collection<Id> applyChanges(Searchable<Id> c, Collection<Id> formulas, Set<Id> changes)
            throws InconsistentTheoryException, SolverException {
        if (changes != null) {
            for (Id axiom : changes)
                formulas.remove(axiom);
            //if (logger.isDebugEnabled())
            //    logger.start("Removing labels from the list: " + changes);
        }
        return formulas;
    }

    @Override
    protected void rollbackChanges(Searchable<Id> c, Collection<Id> formulas, Set<Id> changes) throws InconsistentTheoryException, SolverException {
        // nothing to rollback here;
    }

    /**
     * @return conflict
     * @throws at.ainf.diagnosis.tree.exceptions.NoConflictException
     *
     */

    public FormulaSet<Id> quickXplain(final Searchable<Id> c, final Collection<Id> u)
            throws NoConflictException, SolverException, InconsistentTheoryException {
        iterations = 0;
        try {
            if (!c.verifyRequirements())
                throw new InconsistentTheoryException("Background theory or test cases are inconsistent! Finding conflicts is impossible!");
            ((AbstractReasoner<Id>) c.getReasoner()).addFormulasToCache(u);
            final boolean isCons = c.verifyRequirements();
            ((AbstractReasoner<Id>) c.getReasoner()).removeFormulasFromCache(u);

            if (isCons) {
                throw new NoConflictException("The theory is satisfiable!");
            }
            if (u.isEmpty()) {
                return new FormulaSetImpl<Id>(new BigDecimal(1), new TreeSet<Id>(), new TreeSet<Id>());
            }
            start("Conflict", "qx");
            Set<Id> ids = qqXPlain(c, ((AbstractReasoner<Id>) c.getReasoner()).getFormulasCache(), new FormulaList<Id>(u));
            return new FormulaSetImpl<Id>(new BigDecimal(1), ids, new TreeSet<Id>());

        } finally {
            if (getAxiomListener() != null)
                getAxiomListener().release();
        }
    }

    private QXAxiomListener<Id> getAxiomListener() {
        return this.axiomListener;
    }

    private Set<Id> qqXPlain(Searchable<Id> b, Collection<Id> d, FormulaList<Id> c)
            throws SolverException {
        if (formulaRenderer != null)
            logger.info("B = {" + formulaRenderer.renderAxioms(b.getKnowledgeBase().getBackgroundFormulas()) + "}, \n D={" + formulaRenderer.renderAxioms(((AbstractReasoner<Id>) b.getReasoner()).getFormulasCache()) + "}, \n Delta = {" + formulaRenderer.renderAxioms(d) + "}, \n OD = {" + formulaRenderer.renderAxioms(c) + "}");
        iterations++;
        if (d != null && d.size() != 0 && !b.verifyRequirements())
            return null;

        if (c.size() == 1) {
            if (getAxiomListener() != null)
                getAxiomListener().setFoundAxiom(c.get(0));
            return new TreeSet<Id>(c);
        }
        int k = split(c.size());

        FormulaList<Id> c1 = c.setBounds(0, k - 1);
        FormulaList<Id> c2 = c.setBounds(k, c.size() - 1);

        boolean res = ((AbstractReasoner<Id>) b.getReasoner()).addFormulasToCache(c1);
        Set<Id> d2 = qqXPlain(b, c1, c2);
        if (formulaRenderer != null)
            logger.info("D2 = {" + formulaRenderer.renderAxioms(d2) + "}");
        if (res) ((AbstractReasoner<Id>) b.getReasoner()).removeFormulasFromCache(c1);
        res = ((AbstractReasoner<Id>) b.getReasoner()).addFormulasToCache(d2);
        Set<Id> d1 = qqXPlain(b, d2, c1);
        if (formulaRenderer != null)
            logger.info("D1 = {" + formulaRenderer.renderAxioms(d1) + "}");
        if (res) ((AbstractReasoner<Id>) b.getReasoner()).removeFormulasFromCache(d2);

        if (d2 != null)
            if (d1 == null)
                return d2;
            else
                d1.addAll(d2);
        if (formulaRenderer != null)
            logger.info("return D = {" + formulaRenderer.renderAxioms(d1) + "}");
        return d1;
    }

    public void setFormulaRenderer(FormulaRenderer<Id> formulaRenderer) {
        this.formulaRenderer = formulaRenderer;
    }

    public int getIterations() {
        return iterations;
    }

    void setAxiomListener(QXAxiomListener<Id> axiomListener) {
        this.axiomListener = axiomListener;
    }
}
