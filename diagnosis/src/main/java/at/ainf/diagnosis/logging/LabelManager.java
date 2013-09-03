package at.ainf.diagnosis.logging;

import com.codahale.metrics.MetricRegistry;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 04.06.13
 * Time: 15:39
 * To change this template use File | Settings | File Templates.
 */
public class LabelManager {

    private Map<String,Integer> countOfLabels = new HashMap<String, Integer>();

    private List<String> labels = new LinkedList<String>();

    public LabelManager(String standardLabel) {
        labels.add(standardLabel);
    }

    private int increaseCounter(String identifier) {
        if (!countOfLabels.containsKey(identifier))
            countOfLabels.put(identifier,0);

        Integer counter = countOfLabels.get(identifier);
        countOfLabels.put(identifier,counter+1);
        return counter;
    }

    private int getActualCounter(String identifier) {
        return countOfLabels.get(identifier) - 1;
    }

    public String getActualLabel() {
        return labels.get(labels.size() - 1);
    }

    public String getLabelsConc() {
        StringBuilder full = new StringBuilder();
        for (String label : labels) {
            full.append(label);
            full.append("_");
        }
        return full.toString();
    }

    public String addLabel (String label) {
        String labelFull = label + "-" + increaseCounter(label);
        labels.add(labelFull);
        return labelFull;
    }

    public String removeLabel (String label) {
        String labelFull = label + "-" + getActualCounter(label);
        labels.remove(labelFull);
        return labelFull;
    }

}
