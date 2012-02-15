package at.ainf.diagnosis.partitioning.postprocessor;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 13.02.12
 * Time: 19:19
 * To change this template use File | Settings | File Templates.
 */
public interface QSS<T> extends Postprocessor<T>{
    void updateAnswerToLastQuery(boolean answer);

    void updateNumOfCurrentLeadingDiags(int num);

    void updateNumOfEliminatedLeadingDiags(int num);
}
