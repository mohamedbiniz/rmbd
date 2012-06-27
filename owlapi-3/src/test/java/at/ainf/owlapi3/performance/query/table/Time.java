package at.ainf.owlapi3.performance.query.table;

/**
 * Created by IntelliJ IDEA.
 * User: kostya
 * Date: 02.10.11
 * Time: 17:15
 * To change this template use File | Settings | File Templates.
 */
public class Time {
    long min = 0;
    long max = 0;
    long total = 0;
    long avg = 0;

    public void setTime(long time) {
        if (min > time)
            min = time;
        if (max < time)
            max = time;
        total += time;
    }

    public void setCalls(int calls) {
        this.avg = this.total / calls;
    }
}
