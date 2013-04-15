package at.ainf.owlapi3.test.modules;

import org.semanticweb.owlapi.model.OWLClass;

import java.util.Comparator;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 28.02.13
 * Time: 11:33
 * To change this template use File | Settings | File Templates.
 */
public interface OWLClassComparator extends Comparator<OWLClass> {

    public Integer getMeasure(OWLClass unsatClass);

}
