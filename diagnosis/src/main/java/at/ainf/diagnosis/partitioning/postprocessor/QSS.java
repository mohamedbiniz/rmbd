package at.ainf.diagnosis.partitioning.postprocessor;

import at.ainf.theory.storage.Partition;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 13.02.12
 * Time: 19:19
 * To change this template use File | Settings | File Templates.
 */
public interface QSS<T> extends Postprocessor<T>{
    void updateAnswerToLastQuery(boolean answer);

    void updateNumOfLeadingDiags(Partition<T> partition);

    void updateParameters(boolean answerToLastQuery);
}
