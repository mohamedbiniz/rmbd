package at.ainf.querygen.partitioning;

import at.ainf.theory.storage.Partition;

import java.util.List;

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