package at.ainf.sat4j.model;

import org.sat4j.core.VecInt;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.IteratorInt;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 20.01.12
 * Time: 17:49
 * To change this template use File | Settings | File Templates.
 */
public class VecIntComparable extends VecInt implements IVecIntComparable {

    public VecIntComparable(int[] vector) {
        super(vector);
    }

    public VecIntComparable() {

        super();

    }

    public int compareTo(IVecIntComparable o) {
        if (size() < o.size())
            return -1;
        else if (size() > o.size())
            return 1;
        else {
            for (int j = 0; j < size(); j++) {
                if (get(j) < o.get(j))
                    return -1;
                else if (get(j) > o.get(j))
                    return 1;
            }
            return 0;

        }
    }

}
