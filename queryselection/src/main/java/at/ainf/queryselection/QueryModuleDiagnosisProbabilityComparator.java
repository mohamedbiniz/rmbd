package at.ainf.queryselection;

import java.util.Comparator;

/**
 * Created by IntelliJ IDEA.
 * User: pr8
 * Date: 31.05.11
 * Time: 15:51
 * To change this template use File | Settings | File Templates.
 */
public class QueryModuleDiagnosisProbabilityComparator implements Comparator<QueryModuleDiagnosis> {


      public int compare(QueryModuleDiagnosis d1, QueryModuleDiagnosis d2) {
           if (d1.getProbability() > d2.getProbability() ) {
               return 1;
           }else if (d1.getProbability() < d2.getProbability() ) {
               return -1;
           }else {
               return 0;
           }
      }


}
