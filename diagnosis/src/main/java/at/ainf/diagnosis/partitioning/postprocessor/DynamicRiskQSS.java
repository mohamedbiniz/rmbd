package at.ainf.diagnosis.partitioning.postprocessor;

import java.util.LinkedList;

/**
 * Created by IntelliJ IDEA.
 * User: pr8
 * Date: 13.02.12
 * Time: 20:55
 * To change this template use File | Settings | File Templates.
 */
public class DynamicRiskQSS<T> extends StaticRiskQSS<T> {

    double cMin;
    double cMax;

    public DynamicRiskQSS(double cMin, double c, double cMax) {
        super(c);
        this.cMin = cMin;
        this.cMax = cMax;
    }

    protected void preprocessC(){
	    	double maxPossibleCMax;
            if ((maxPossibleCMax = (double)this.getMaxPossibleNumOfDiagsToEliminate() / (double)getNumOfLeadingDiags()) < cMax) {
	            cMax = maxPossibleCMax;
	        }
            if (cMin < 0d) cMin = 0d;
            if (cMin > cMax) cMin = cMax = (cMin + cMax)/2d;
            if (c < cMin) c = cMin;
            if (c > cMax) c = cMax;
    }

    protected double getCAdjust(int numOfElimDiags){
        double interval = cMax - cMin;
        double epsilon = 0.01d;
        double adjust = ((Math.floor( ( (double)getNumOfLeadingDiags() / 2d ) - epsilon ) - (double)numOfElimDiags) / (double)getNumOfLeadingDiags());
        return adjust * interval * 2d;
    }

    public void updateC(int numOfElimDiags) {

        double cAdjust = getCAdjust(numOfElimDiags);
        if (c + cAdjust > cMax) {
            c = cMax;
        } else if (c + cAdjust < cMin) {
            c = cMin;
        } else {
            c += cAdjust;
        }

    }
}
