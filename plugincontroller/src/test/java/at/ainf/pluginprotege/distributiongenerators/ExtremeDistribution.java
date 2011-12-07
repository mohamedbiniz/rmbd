package at.ainf.pluginprotege.distributiongenerators;

import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: pr8
 * Date: 24.05.11
 * Time: 15:26
 * To change this template use File | Settings | File Templates.
 */
public class ExtremeDistribution implements DistributionGeneratorI {

    ExtremeDistributionMode mode;
    private Random randomGenerator;
    private double param = 1.75d;

    public ExtremeDistribution(){
        this.mode = ExtremeDistributionMode.ONE_DIV_PARAM_POWER_X;
        randomGenerator = new Random(1);
    }

    public ExtremeDistribution(long seed, ExtremeDistributionMode mode, double param){
        this.mode = mode;
        randomGenerator = new Random(seed);
        this.param = param;
    }

    public double [] getProbabilities(int num){
        double[] probs = new double[num];
        double normalizationFactor = 0d;
        for (int i = 0; i < probs.length; i++) {
            double weight = getWeight(mode,i);
            probs[i] = randomGenerator.nextDouble() * weight;
            normalizationFactor += probs[i];
        }
        for(int i = 0; i<probs.length; i++){
            probs[i] /= normalizationFactor;
        }
        return probs;
    }


    private double getWeight(ExtremeDistributionMode mode, int i){
        double weight;
        switch(mode){
            case ONE_DIV_PARAM_POWER_X:
                weight = 1d/Math.pow(param,(double)i);
                break;
            case ONE_DIV_X_POWER_PARAM:
                weight = 1d/Math.pow((double)i+1d,param);        // (+1d) damit nicht division durch 0
                break;
            default:
                throw new IllegalArgumentException("Illegal ExtremeDistribution Mode selected!");
        }
        return weight;
    }
}
