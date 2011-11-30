package at.ainf.queryselection;

import java.util.Comparator;

/**
 * Created by IntelliJ IDEA.
 * User: pr8
 * Date: 20.05.11
 * Time: 11:10
 * To change this template use File | Settings | File Templates.
 */
public class QueryScoreComparator implements Comparator<Query> {


  public int compare(Query q1, Query q2) {
       if (q1.getScore() > q2.getScore() ) {
           return 1;
       }else if (q1.getScore() < q2.getScore()) {
           return -1;
       }else {
           return 0;
       }
    }


}