package at.ainf.diagnosis.watchedset;


/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 19.01.12
 * Time: 19:19
 * To change this template use File | Settings | File Templates.
 */
public interface WatchedElement<X> {

    public void addMeasureUpdatedListener(MeasureUpdatedListener<X> listener);

    public void removeMeasureUpdatedListener(MeasureUpdatedListener<X> listener);

    public void setWatchedElementMeasure(X value);

}
