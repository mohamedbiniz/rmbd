package at.ainf.diagnosis.partitioning.scoring;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 15.02.12
 * Time: 10:08
 * To change this template use File | Settings | File Templates.
 */
public class QSSFactory {
    
    public static <T> QSS<T> createMinScoreQSS() {
        return new MinScoreQSS<T>();
    }
    
    public static <T> QSS<T> createSplitInHalfQSS() {
        return new SplitInHalfQSS<T>();
    }
    
    public static <T> QSS<T> createPenaltyQSS(double maxPenalty) {
        return new PenaltyQSS<T>(maxPenalty);
    }
    
    public static <T> QSS<T> createStaticRiskQSS(double c) {
        return new StaticRiskQSS<T>(c);
    }

    public static <T> QSS<T> createDynamicRiskQSS(double cMin, double cMax, double c) {
        return new DynamicRiskQSS<T>(cMin,c,cMax);
    }
    
}
