package at.ainf.owlapi3.module.iterative.diag;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 06.05.13
 * Time: 13:15
 * To change this template use File | Settings | File Templates.
 */
public class Average {

    public void reset() {
        data = new LinkedList<List<Long>>();
    }

    public enum Mode {MEAN, SUM}

    private Mode mode;

    private List<List<Long>> data = new LinkedList<List<Long>>();

    public Average() {
        this.mode = Mode.MEAN;
    }

    public Average(Mode mode) {
        this.mode = mode;
    }

    private Long doModeOp(Collection<Long> set) {
        if (mode.equals(Mode.MEAN)) {
            return mean(set);
        }
        else if (mode.equals(Mode.SUM)) {
            Long sum = 0L;
            for (Long value : set)
                sum += value;
            return sum;
        }
        else
            throw new UnsupportedOperationException("operation unknown ");
    }

    public static Long mean (Collection<Long> set) {
        if (set.isEmpty())
            return 0L;
        Long sum = 0L;
        for (Long i : set)
            sum += i;
        return sum / set.size();
    }

    public void createNewValueGroup() {
        data.add(new LinkedList<Long>());
    }

    public void addValue(Long value) {
        if (!data.isEmpty())
            data.get(data.size()-1).add(value);
    }

    public Collection<Long> toAverageCollection() {
        List<Long> averages = new LinkedList<Long>();
        for (List<Long> set : data)
            averages.add(doModeOp(set));
        return averages;
    }

}
