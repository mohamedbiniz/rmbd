package at.ainf.queryselection.distributiongenerators;

import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: pr8
 * Date: 24.05.11
 * Time: 17:24
 * To change this template use File | Settings | File Templates.
 */
public class ModerateDistribution implements DistributionGeneratorI{

    ModerateDistributionMode mode;
    private Random randomGenerator;
    private double param = (double)2;

    public ModerateDistribution(){
        this.mode = ModerateDistributionMode.NO_WEIGHTS;
        randomGenerator = new Random(1);
    }

    public ModerateDistribution(long seed, ModerateDistributionMode mode, double param){
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


    private double getWeight(ModerateDistributionMode mode, int i){
        double weight;
        switch(mode){
            case NO_WEIGHTS:
                weight = 1d;
                break;
            case ONE_DIV_PARAM_TH_ROOT_OF_X:
                weight = 1d/Math.pow((double)i,1d/param);
                break;
            default:
                throw new IllegalArgumentException("Illegal ModerateDistribution Mode selected!");
        }
        return weight;
    }
}
