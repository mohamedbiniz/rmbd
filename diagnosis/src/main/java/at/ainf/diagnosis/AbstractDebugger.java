package at.ainf.diagnosis;

import at.ainf.diagnosis.storage.FormulaRenderer;
import at.ainf.diagnosis.storage.FormulaSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 05.12.12
 * Time: 11:58
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractDebugger<T extends FormulaSet<Id>, Id> implements Debugger<T, Id> {

    private static Logger logger = LoggerFactory.getLogger(AbstractDebugger.class.getName());

    protected FormulaRenderer<Id> formulaRenderer;



}
