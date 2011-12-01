package at.ainf.queryselection;

import at.ainf.theory.model.SolverException;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: pr8
 * Date: 16.02.11
 * Time: 15:46
 * To change this template use File | Settings | File Templates.
 */
public interface IQueryProvider/*<T>*/ {

    public Query getQuery(boolean alternativeQuery) throws NoFurtherQueryException, SolverException, SingleDiagnosisLeftException;

    public QInfo setQueryAnswer(boolean answer);

    public List<QueryModuleDiagnosis> getCurrentDiagnoses();

    //public void setQueryAnswer(boolean answer);

    public boolean getQueryAnswer(Query q) throws AnswerNotKnownException;

    public String getStrategyName();

    public String probDistToString();

    public void setNumOfLeadingDiagnoses(int n);

    public void setDiagnosisProvider(IDiagnosisProvider dp);

    public QueryModuleDiagnosis getMostProbableDiagnosis();

    public int getTooHighRiskCounter();

    public int getAdaptationCounter();

    public int getTargetDiagWithinLeadingDiagsCounter();


    //public QInfo getPostQueryInfo();

    //public Collection<Collection<T>> getCurrentDiagnoses();

    //public void init(AbstractTheory abstractTheory);

    //public void setFaultProbabilities(Map<LogicalConstruct, Double> probs);

    //public void setMaxNumOfMostProbDiags(int n);

    //public void setMaxDiagnosisLength(int k);

    public List<Query> getCurrentQueries();
}
