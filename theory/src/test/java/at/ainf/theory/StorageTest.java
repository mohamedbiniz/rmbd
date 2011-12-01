package at.ainf.theory;

import at.ainf.theory.storage.HittingSet;
import at.ainf.theory.storage.HittingSetImpl;
import at.ainf.theory.storage.SimpleStorage;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 30.05.11
 * Time: 11:17
 * To change this template use File | Settings | File Templates.
 */
public class StorageTest {

    private static Logger logger = Logger.getLogger(StorageTest.class.getName());

       @BeforeClass
       public static void setUp() {
           String conf = ClassLoader.getSystemResource("theory-log4j.properties").getFile();
           PropertyConfigurator.configure(conf);
       }

    private final int HS_COUNT = 50;
    @Test
    public void testHittingSets() {
        Random random = new Random();

        SimpleStorage<Integer> st = new SimpleStorage<Integer>();

        int count = 0;
        for (int i = 0; i < HS_COUNT; i++) {
            Set<Integer> hs = new HashSet<Integer>();
            hs.add(count++);
            hs.add(count);
            HittingSetImpl<Integer> hittingSet = new HittingSetImpl<Integer>("ID" + i, random.nextDouble(), hs, new HashSet<Integer>());
            hittingSet.setValid(random.nextBoolean());
            st.addHittingSet(hittingSet);
        }
        int valid = st.getValidHittingSets().size();
        validateSet(st);
        for (int i = 0; i < HS_COUNT; i++) {
            logger.debug("Computing test " + i);
            Set<HittingSet<Integer>> hittingSets = new TreeSet<HittingSet<Integer>>(st.getHittingSets());
            assertEquals(HS_COUNT, hittingSets.size());
            assertEquals(valid, st.getValidHittingSets().size());
            for (HittingSet<Integer> hs : hittingSets) {
                hs.setMeasure(random.nextDouble());
            }
            validateSet(st);
        }
    }

    private void validateSet(SimpleStorage<? extends Object> st) {
        for (HittingSet hs : st.getHittingSets()) {
            boolean contains = st.getHittingSets().contains(hs);
            assertTrue(contains);
        }
    }
}
