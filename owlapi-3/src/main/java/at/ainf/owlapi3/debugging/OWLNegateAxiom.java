package at.ainf.owlapi3.debugging;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.NNF;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by IntelliJ IDEA.
 * User: kostya
 * Date: 04.05.2010
 * Time: 12:20:03
 * To change this template use File | Settings | File Templates.
 */
public class OWLNegateAxiom extends NNF {

    private boolean negated = true;

    private OWLDataFactory dataFactory;


    public OWLNegateAxiom(final OWLDataFactory dataFactory) {
        super(dataFactory);
        this.dataFactory = dataFactory;
    }

    public boolean isNegated() {
        return negated;
    }

    public void setNegated(boolean negated) {
        this.negated = negated;
    }

    private OWLClassExpression getNegation(OWLClassExpression classExpression) {
        return dataFactory.getOWLObjectComplementOf(classExpression);
    }

    private int count = 1;

    @Override
    public OWLAxiom visit(OWLSubClassOfAxiom axiom) {
        OWLClassExpression subClass = axiom.getSubClass();
        OWLClassExpression superClass = axiom.getSuperClass();
        OWLClassExpression negatedSuperClass = getNegation(superClass);
        OWLClassExpression negatedSubClass = getNegation(subClass);


        Set<OWLClassExpression> exprs = new LinkedHashSet<OWLClassExpression>();
        exprs.add(negatedSubClass.accept(this));
        exprs.add(superClass.accept(this));
        return dataFactory.getOWLSubClassOfAxiom(dataFactory.getOWLObjectUnionOf(exprs), dataFactory.getOWLNothing());

        //String uri = "https://negation.ainf.at/debugging#";
        //OWLClass cl = dataFactory.getOWLClass(IRI.create(uri + count++));
        //return dataFactory.getOWLSubClassOfAxiom(negatedSuperClass);
        //return dataFactory.getOWLSubClassOfAxiom(superClass, dataFactory.getOWLNothing());
    }

    public OWLAxiom visit(OWLClassAssertionAxiom axiom) {
        //if (axiom.getClassExpression().isAnonymous()) {
        return dataFactory.getOWLClassAssertionAxiom(axiom.getClassExpression().accept(this), axiom.getIndividual());
        //} else {
        //    return axiom;
        //}
    }

    public OWLAxiom visit(OWLEquivalentClassesAxiom axiom) {
        Set<OWLClassExpression> ops = new LinkedHashSet<OWLClassExpression>();
        for (OWLClassExpression op : axiom.getClassExpressions()) {
            ops.add(op.accept(this));
        }
        return dataFactory.getOWLEquivalentClassesAxiom(ops);
    }


    public OWLClassExpression visit(OWLClass desc) {
        if (negated) {
            if (desc.isOWLNothing()) {
                return dataFactory.getOWLThing();
            } else if (desc.isOWLThing()) {
                return dataFactory.getOWLNothing();
            } else {
                return getNegation(desc);
            }

        } else {
            return desc;
        }
    }


    public OWLClassExpression visit(OWLObjectIntersectionOf desc) {
        Set<OWLClassExpression> ops = new LinkedHashSet<OWLClassExpression>();
        for (OWLClassExpression op : desc.getOperands()) {
            ops.add(op.accept(this));
        }
        if (negated) {
            return dataFactory.getOWLObjectUnionOf(ops);
        } else {
            return dataFactory.getOWLObjectIntersectionOf(ops);
        }
    }


    public OWLClassExpression visit(OWLObjectUnionOf desc) {
        Set<OWLClassExpression> ops = new LinkedHashSet<OWLClassExpression>();
        for (OWLClassExpression op : desc.getOperands()) {
            ops.add(op.accept(this));
        }
        if (negated) {
            // Flip to an intersection
            return dataFactory.getOWLObjectIntersectionOf(ops);
        } else {
            return dataFactory.getOWLObjectUnionOf(ops);
        }
    }


    public OWLClassExpression visit(OWLObjectComplementOf desc) {
        if (negated) {
            // Cancels out.
            // Save and then restore.
            boolean neg = negated;
            negated = false;
            OWLClassExpression negDesc = desc.getOperand().accept(this);
            negated = neg;
            return negDesc;
        } else {
            // Save and then restore
            boolean neg = negated;
            negated = true;
            OWLClassExpression negDesc = desc.getOperand().accept(this);
            negated = neg;
            return negDesc;
        }
    }


    public OWLClassExpression visit(OWLObjectSomeValuesFrom desc) {
        OWLClassExpression filler = desc.getFiller().accept(this);
        if (negated) {
            return dataFactory.getOWLObjectAllValuesFrom(desc.getProperty(), filler);
        } else {
            return dataFactory.getOWLObjectSomeValuesFrom(desc.getProperty(), filler);
        }
    }


    public OWLClassExpression visit(OWLObjectAllValuesFrom desc) {
        OWLClassExpression filler = desc.getFiller().accept(this);
        if (negated) {
            return dataFactory.getOWLObjectSomeValuesFrom(desc.getProperty(), filler);
        } else {
            return dataFactory.getOWLObjectAllValuesFrom(desc.getProperty(), filler);
        }
    }


    public OWLClassExpression visit(OWLObjectHasValue desc) {
        return desc.asSomeValuesFrom().accept(this);
    }


    public OWLClassExpression visit(OWLObjectMinCardinality desc) {
        boolean neg = negated;
        int card = desc.getCardinality();
        if (negated) {
            card = desc.getCardinality() - 1;
            if (card < 0) {
                card = 0;
            }
        }
        negated = false;
        OWLClassExpression filler = desc.getFiller().accept(this);
        OWLClassExpression nnf = null;
        if (neg) {
            nnf = dataFactory.getOWLObjectMaxCardinality(card, desc.getProperty(), filler);
        } else {
            nnf = dataFactory.getOWLObjectMinCardinality(card, desc.getProperty(), filler);
        }
        negated = neg;
        return nnf;
    }


    public OWLClassExpression visit(OWLObjectExactCardinality desc) {
        return desc.asIntersectionOfMinMax().accept(this);
    }


    public OWLClassExpression visit(OWLObjectMaxCardinality desc) {
        boolean neg = negated;
        int card = desc.getCardinality();
        if (negated) {
            card = desc.getCardinality() + 1;
        }
        negated = false;
        OWLClassExpression filler = desc.getFiller().accept(this);
        OWLClassExpression nnf = null;
        if (neg) {
            nnf = dataFactory.getOWLObjectMinCardinality(card, desc.getProperty(), filler);
        } else {
            nnf = dataFactory.getOWLObjectMaxCardinality(card, desc.getProperty(), filler);
        }
        negated = neg;
        return nnf;
    }


    public OWLClassExpression visit(OWLObjectHasSelf desc) {
        if (negated) {
            return getNegation(desc);
        } else {
            return desc;
        }
    }


    public OWLClassExpression visit(OWLObjectOneOf desc) {
        if (desc.getIndividuals().size() == 1) {
            if (negated) {
                return getNegation(desc);
            } else {
                return desc;
            }
        } else {
            return desc.asObjectUnionOf().accept(this);
        }
    }


    public OWLClassExpression visit(OWLDataSomeValuesFrom desc) {
        OWLDataRange filler = desc.getFiller().accept(this);
        if (negated) {
            return dataFactory.getOWLDataAllValuesFrom(desc.getProperty(), filler);
        } else {
            return dataFactory.getOWLDataSomeValuesFrom(desc.getProperty(), filler);
        }
    }


    public OWLClassExpression visit(OWLDataAllValuesFrom desc) {
        OWLDataRange filler = desc.getFiller().accept(this);
        if (negated) {
            return dataFactory.getOWLDataSomeValuesFrom(desc.getProperty(), filler);
        } else {
            return dataFactory.getOWLDataAllValuesFrom(desc.getProperty(), filler);
        }
    }


    public OWLClassExpression visit(OWLDataHasValue desc) {
        return desc.asSomeValuesFrom().accept(this);
    }


    public OWLClassExpression visit(OWLDataExactCardinality desc) {
        return desc.asIntersectionOfMinMax().accept(this);
    }

    public OWLClassExpression visit(OWLDataMaxCardinality desc) {
        boolean neg = negated;
        int card = desc.getCardinality();
        if (negated) {
            card = desc.getCardinality() + 1;
        }
        negated = false;
        OWLDataRange filler = desc.getFiller().accept(this);
        OWLClassExpression nnf = null;
        if (neg) {
            nnf = dataFactory.getOWLDataMinCardinality(card, desc.getProperty(), filler);
        } else {
            nnf = dataFactory.getOWLDataMaxCardinality(card, desc.getProperty(), filler);
        }
        negated = neg;
        return nnf;
    }


    public OWLClassExpression visit(OWLDataMinCardinality desc) {
        boolean neg = negated;
        int card = desc.getCardinality();
        if (negated) {
            card = desc.getCardinality() - 1;
            if (card < 0) {
                card = 0;
            }
        }
        negated = false;
        OWLDataRange filler = desc.getFiller().accept(this);
        OWLClassExpression nnf = null;
        if (neg) {
            nnf = dataFactory.getOWLDataMaxCardinality(card, desc.getProperty(), filler);
        } else {
            nnf = dataFactory.getOWLDataMinCardinality(card, desc.getProperty(), filler);
        }
        negated = neg;
        return nnf;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////


    public OWLDataRange visit(OWLDatatype node) {
        if (negated) {
            return dataFactory.getOWLDataComplementOf(node);
        } else {
            return node;
        }
    }


    public OWLDataRange visit(OWLDataComplementOf node) {
        if (negated) {
            return node.getDataRange();
        } else {
            return node;
        }
    }


    public OWLDataRange visit(OWLDataOneOf node) {
        if (node.getValues().size() == 1) {
            if (negated) {
                return dataFactory.getOWLDataComplementOf(node);
            } else {
                return node;
            }
        } else {
            // Encode as a data union of and return result
            Set<OWLDataOneOf> oneOfs = new LinkedHashSet<OWLDataOneOf>();
            for (OWLLiteral lit : node.getValues()) {
                oneOfs.add(dataFactory.getOWLDataOneOf(lit));
            }
            return dataFactory.getOWLDataUnionOf(oneOfs).accept(this);
        }

    }

    public OWLDataRange visit(OWLDataIntersectionOf node) {
        Set<OWLDataRange> ops = new LinkedHashSet<OWLDataRange>();
        for (OWLDataRange op : node.getOperands()) {
            ops.add(op.accept(this));
        }
        if (negated) {
            return dataFactory.getOWLDataUnionOf(ops);
        } else {
            return dataFactory.getOWLDataIntersectionOf(ops);
        }
    }

    public OWLDataRange visit(OWLDataUnionOf node) {
        Set<OWLDataRange> ops = new LinkedHashSet<OWLDataRange>();
        for (OWLDataRange op : node.getOperands()) {
            ops.add(op.accept(this));
        }
        if (negated) {
            // Flip to an intersection
            return dataFactory.getOWLDataIntersectionOf(ops);
        } else {
            return dataFactory.getOWLDataUnionOf(ops);
        }
    }

    public OWLAxiom visit(OWLHasKeyAxiom axiom) {
        return null;
    }

    public OWLDataRange visit(OWLDatatypeRestriction node) {
        if (negated) {
            return dataFactory.getOWLDataComplementOf(node);
        } else {
            return node;
        }
    }

}

