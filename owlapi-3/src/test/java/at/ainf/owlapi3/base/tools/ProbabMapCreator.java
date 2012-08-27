package at.ainf.owlapi3.base.tools;

import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax;

import java.math.BigDecimal;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 06.08.12
 * Time: 11:06
 * To change this template use File | Settings | File Templates.
 */
public class ProbabMapCreator {

    private static ManchesterOWLSyntax[] keywords = {ManchesterOWLSyntax.SOME,
                ManchesterOWLSyntax.ONLY,
                ManchesterOWLSyntax.MIN,
                ManchesterOWLSyntax.MAX,
                ManchesterOWLSyntax.EXACTLY,
                ManchesterOWLSyntax.AND,
                ManchesterOWLSyntax.OR,
                ManchesterOWLSyntax.NOT,
                ManchesterOWLSyntax.VALUE,
                ManchesterOWLSyntax.INVERSE,
                ManchesterOWLSyntax.SUBCLASS_OF,
                ManchesterOWLSyntax.EQUIVALENT_TO,
                ManchesterOWLSyntax.DISJOINT_CLASSES,
                ManchesterOWLSyntax.DISJOINT_WITH,
                ManchesterOWLSyntax.FUNCTIONAL,
                ManchesterOWLSyntax.INVERSE_OF,
                ManchesterOWLSyntax.SUB_PROPERTY_OF,
                ManchesterOWLSyntax.SAME_AS,
                ManchesterOWLSyntax.DIFFERENT_FROM,
                ManchesterOWLSyntax.RANGE,
                ManchesterOWLSyntax.DOMAIN,
                ManchesterOWLSyntax.TYPE,
                ManchesterOWLSyntax.TRANSITIVE,
                ManchesterOWLSyntax.SYMMETRIC
        };

    public static HashMap<ManchesterOWLSyntax, BigDecimal> getProbabMap() {
        HashMap<ManchesterOWLSyntax, BigDecimal> map = new HashMap<ManchesterOWLSyntax, BigDecimal>();

        for (ManchesterOWLSyntax keyword : keywords) {
            map.put(keyword, BigDecimal.valueOf(0.01));
        }

        map.put(ManchesterOWLSyntax.SOME, BigDecimal.valueOf(0.05));
        map.put(ManchesterOWLSyntax.ONLY, BigDecimal.valueOf(0.05));
        map.put(ManchesterOWLSyntax.AND, BigDecimal.valueOf(0.001));
        map.put(ManchesterOWLSyntax.OR, BigDecimal.valueOf(0.001));
        map.put(ManchesterOWLSyntax.NOT, BigDecimal.valueOf(0.01));

        return map;
    }

}
