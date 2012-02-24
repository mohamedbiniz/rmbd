package at.ainf.queryselection.sim;

import at.ainf.queryselection.*;

import java.util.LinkedList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: pr8
 * Date: 20.05.11
 * Time: 11:24
 * To change this template use File | Settings | File Templates.
 */
public class SortTest {

    @Test
    public void compareTest(){
        QueryScoreComparator comp = new QueryScoreComparator();
        List<Query> queries = new LinkedList<Query>();
        for(int i=0; i<10; i++){
            Query q = new Query();

        }
        queries.add(new Query());
    }

    @Test
    public void testBubbleSort(){
        QueryModuleDiagnosis d1 = new Diagnosis("d1",0.12d, false);
        QueryModuleDiagnosis d2 = new Diagnosis("d2",0.25d, false);
        QueryModuleDiagnosis d3 = new Diagnosis("d3",0.1d, false);
        QueryModuleDiagnosis d4 = new Diagnosis("d4",0.3d, false);
        QueryModuleDiagnosis d5 = new Diagnosis("d5",0.08d, false);
        QueryModuleDiagnosis [] qmdArray = new QueryModuleDiagnosis[5];
        qmdArray[0] = d1;
        qmdArray[1] = d2;
        qmdArray[2] = d3;
        qmdArray[3] = d4;
        qmdArray[4] = d5;
        QueryModuleDiagnosisProbabilityComparator comp = new QueryModuleDiagnosisProbabilityComparator();
        BubbleSort<QueryModuleDiagnosis> bs = new BubbleSort<QueryModuleDiagnosis>(comp);
        bs.sort(qmdArray);
        assertTrue(qmdArray[0].equals(d4));
        assertTrue(qmdArray[1].equals(d2));
        for(int i = 0; i < qmdArray.length; i++){
            System.out.println(qmdArray[i].getName());
        }
    }

}
