package at.ainf.owlapi3.test;

import at.ainf.diagnosis.tree.CostNode;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import java.math.BigDecimal;
import java.util.PriorityQueue;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 06.10.11
 * Time: 13:26
 * To change this template use File | Settings | File Templates.
 */
public class CostNodesTest {

    private static Logger logger = Logger.getLogger(CostNodesTest.class.getName());

    public CostNode<OWLLogicalAxiom> createCostNode(BigDecimal costs, final int l) {
        CostNode<OWLLogicalAxiom> node = new CostNode<OWLLogicalAxiom>(null) {
            public int getPathLabelSize() {
                return l;
            }
        };
        node.setNodePathCosts(costs);
        return node;
    }

    @BeforeClass
    public static void setUp() {
        String conf = ClassLoader.getSystemResource("owlapi3-log4j.properties").getFile();
        PropertyConfigurator.configure(conf);
    }

    @Ignore
    @Test
    public void costNodesTest() {

        PriorityQueue<CostNode<OWLLogicalAxiom>> nodes = new PriorityQueue<CostNode<OWLLogicalAxiom>>();
        for (int i = 0; i < 10; i++)
            nodes.add(createCostNode( BigDecimal.valueOf(0.5) ,i));

        while(nodes.peek() != null) {
            CostNode<OWLLogicalAxiom> node = nodes.poll();
            logger.info(node.getName() + " " + node.getNodePathCosts());
        }


    }

}
