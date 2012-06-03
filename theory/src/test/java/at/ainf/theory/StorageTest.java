package at.ainf.theory;

import at.ainf.theory.storage.AxiomSet;
import at.ainf.theory.storage.AxiomSetFactory;
import at.ainf.theory.storage.SimpleStorage;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.*;

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
    public void testAXcreation(){
        Set<String> hs = new HashSet<String>();
        hs.add("one");
        hs.add("two");
        hs.add("three");
        AxiomSet<String> ax1 = AxiomSetFactory.createConflictSet(0.1, hs, new HashSet<String>());
        AxiomSet<String> ax2 = AxiomSetFactory.createConflictSet(0.1, hs, new HashSet<String>());
        Set<String> um = Collections.unmodifiableSet(hs);
        
        Set<String> hs2 = new HashSet<String>();
        hs2.add("four");
        AxiomSet<String> ax3 = AxiomSetFactory.createConflictSet(0.1, hs2, new HashSet<String>());
        
        assertTrue(ax1.equals(ax2));
        assertTrue(ax1.equals(hs));
        assertTrue(ax1.equals(um));
        assertTrue(!ax3.equals(ax1));
    }

    @Test
    public void testHittingSets() {
        Random random = new Random();

        SimpleStorage<Integer> st = new SimpleStorage<Integer>();

        int count = 0;
        for (int i = 0; i < HS_COUNT; i++) {
            Set<Integer> hs = new HashSet<Integer>();
            hs.add(count++);
            hs.add(count);
            AxiomSet<Integer> hittingSet = AxiomSetFactory.createHittingSet(random.nextDouble(), hs, new HashSet<Integer>());
            hittingSet.setValid(random.nextBoolean());
            st.addHittingSet(hittingSet);
        }
        int valid = st.getDiagnoses().size();
        validateSet(st);
        for (int i = 0; i < HS_COUNT; i++) {
            logger.debug("Computing test " + i);
            Set<AxiomSet<Integer>> axiomSets = new TreeSet<AxiomSet<Integer>>(st.getHittingSets());
            assertEquals(HS_COUNT, axiomSets.size());
            assertEquals(valid, st.getDiagnoses().size());
            for (AxiomSet<Integer> hs : axiomSets) {
                hs.setMeasure(random.nextDouble());
            }
            validateSet(st);
        }
    }

    private void validateSet(SimpleStorage<? extends Object> st) {
        for (AxiomSet hs : st.getHittingSets()) {
            boolean contains = st.getHittingSets().contains(hs);
            assertTrue(contains);
        }
    }
}
