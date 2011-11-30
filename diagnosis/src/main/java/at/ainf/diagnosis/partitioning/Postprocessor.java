package at.ainf.diagnosis.partitioning;

import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: kostya
 * Date: 01.06.11
 * Time: 08:44
 * To change this template use File | Settings | File Templates.
 */
public interface Postprocessor {
    <T> Partition<T> run(List<Partition<T>> partitions);
}