package at.ainf.theory.watchedset;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 19.01.12
 * Time: 10:12
 * To change this template use File | Settings | File Templates.
 */
public interface MeasureUpdatedListener<X> {

    public void notifiyMeasureUpdated(WatchedElement<X> element, X value);

}
