package at.ainf.owlapi3.parser;

import org.apache.log4j.Logger;
//import org.protege.editor.owl.model.OWLModelManager;
//import org.protege.editor.owl.model.cache.OWLEntityRenderingCache;
//import org.protege.editor.owl.model.util.OWLDataTypeUtils;
//import org.protege.editor.owl.ui.renderer.OWLEntityRendererImpl;
//import org.protege.editor.owl.ui.renderer.OWLModelManagerEntityRenderer;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 18.04.11
 * Time: 14:14
 * To change this template use File | Settings | File Templates.
 */
public class MyOWLEntRenderCache  {

    private static final Logger logger = Logger.getLogger(MyOWLEntRenderCache.class);

    private Map<String, OWLClass> owlClassMap = new HashMap<String, OWLClass>();

    private Map<String, OWLObjectProperty> owlObjectPropertyMap = new HashMap<String, OWLObjectProperty>();

    private Map<String, OWLDataProperty> owlDataPropertyMap = new HashMap<String, OWLDataProperty>();

    private Map<String, OWLAnnotationProperty> owlAnnotationPropertyMap = new HashMap<String, OWLAnnotationProperty>();

    private Map<String, OWLNamedIndividual> owlIndividualMap = new HashMap<String, OWLNamedIndividual>();

    private Map<String, OWLDatatype> owlDatatypeMap = new HashMap<String, OWLDatatype>();

    private Map<OWLEntity, String> entityRenderingMap = new HashMap<OWLEntity, String>();

    private OWLOntology ontology;

    private OWLDataFactory factory;

    private OWLOntologyChangeListener listener = new OWLOntologyChangeListener() {
        public void ontologiesChanged(List<? extends OWLOntologyChange> changes) {
            processChanges(changes);
        }
    };


    public MyOWLEntRenderCache(OWLOntology ont) {
        this.ontology = ont;
        this.factory = ont.getOWLOntologyManager().getOWLDataFactory();
    }





    private void processChanges(List<? extends OWLOntologyChange> changes) {
        Set<OWLEntity> changedEntities = new TreeSet<OWLEntity>();
        for (OWLOntologyChange change : changes) {
            if (change instanceof OWLAxiomChange) {
                OWLAxiomChange chg = (OWLAxiomChange) change;
                changedEntities.addAll(chg.getEntities());
            }
        }
        for (OWLEntity ent : changedEntities) {
            updateRendering(ent);
        }
    }


    public void rebuild() {
        clear();
        MyOWLEntityRendererI entityRenderer = new MyOWLEntityRendererI();


        OWLClass thing = factory.getOWLThing();
        owlClassMap.put(entityRenderer.render(thing), thing);
        entityRenderingMap.put(thing, entityRenderer.render(thing));
        OWLClass nothing = factory.getOWLNothing();
        entityRenderingMap.put(nothing, entityRenderer.render(nothing));
        owlClassMap.put(entityRenderer.render(nothing), nothing);


        for (OWLClass cls : ontology.getClassesInSignature()) {
            addRendering(cls, owlClassMap);
        }
        for (OWLObjectProperty prop : ontology.getObjectPropertiesInSignature()) {
            addRendering(prop, owlObjectPropertyMap);
        }
        for (OWLDataProperty prop : ontology.getDataPropertiesInSignature()) {
            addRendering(prop, owlDataPropertyMap);
        }
        for (OWLIndividual ind : ontology.getIndividualsInSignature()) {
            if (!ind.isAnonymous()) {
                addRendering(ind.asOWLNamedIndividual(), owlIndividualMap);
            }
        }
        for (OWLAnnotationProperty prop : ontology.getAnnotationPropertiesInSignature()) {
            addRendering(prop, owlAnnotationPropertyMap);
        }


        // standard annotation properties
        for (IRI uri : OWLRDFVocabulary.BUILT_IN_ANNOTATION_PROPERTY_IRIS) {
            addRendering(factory.getOWLAnnotationProperty(uri), owlAnnotationPropertyMap);
        }

        // datatypes
        final MyOWLDataTypeUtils datatypeUtils = new MyOWLDataTypeUtils(ontology.getOWLOntologyManager());
        TreeSet<OWLOntology> set = new TreeSet<OWLOntology>();
        set.add(ontology);
        for (OWLDatatype dt : datatypeUtils.getKnownDatatypes(set)) {
            addRendering(dt, owlDatatypeMap);
        }
    }


    public void dispose() {
        clear();
        // owlModelManager.removeOntologyChangeListener(listener);
    }


    private void clear() {
        owlClassMap.clear();
        owlObjectPropertyMap.clear();
        owlDataPropertyMap.clear();
        owlAnnotationPropertyMap.clear();
        owlIndividualMap.clear();
        owlDatatypeMap.clear();
        entityRenderingMap.clear();
    }


    public OWLClass getOWLClass(String rendering) {
        return owlClassMap.get(rendering);
    }


    public OWLObjectProperty getOWLObjectProperty(String rendering) {
        return owlObjectPropertyMap.get(rendering);
    }


    public OWLDataProperty getOWLDataProperty(String rendering) {
        return owlDataPropertyMap.get(rendering);
    }


    public OWLAnnotationProperty getOWLAnnotationProperty(String rendering) {
        return owlAnnotationPropertyMap.get(rendering);
    }


    public OWLNamedIndividual getOWLIndividual(String rendering) {
        return owlIndividualMap.get(rendering);
    }


    public OWLDatatype getOWLDatatype(String rendering) {
        return owlDatatypeMap.get(rendering);
    }


    public String getRendering(OWLEntity owlEntity) {
        return entityRenderingMap.get(owlEntity);
    }


    public OWLEntity getOWLEntity(String rendering) {
        OWLEntity entity = getOWLClass(rendering);
        if (entity != null) {
            return entity;
        }
        entity = getOWLObjectProperty(rendering);
        if (entity != null) {
            return entity;
        }
        entity = getOWLDataProperty(rendering);
        if (entity != null) {
            return entity;
        }
        entity = getOWLIndividual(rendering);
        if (entity != null) {
            return entity;
        }
        entity = getOWLDatatype(rendering);
        if (entity != null) {
            return entity;
        }
        entity = getOWLAnnotationProperty(rendering);
        if (entity != null) {
            return entity;
        }
        return null;
    }


    public void addRendering(OWLEntity owlEntity) {
        owlEntity.accept(new OWLEntityVisitor() {
            public void visit(OWLDataProperty entity) {
                addRendering(entity, owlDataPropertyMap);
            }

            public void visit(OWLObjectProperty entity) {
                addRendering(entity, owlObjectPropertyMap);
            }

            public void visit(OWLAnnotationProperty owlAnnotationProperty) {
                addRendering(owlAnnotationProperty, owlAnnotationPropertyMap);
            }

            public void visit(OWLNamedIndividual entity) {
                addRendering(entity, owlIndividualMap);
            }

            public void visit(OWLClass entity) {
                addRendering(entity, owlClassMap);
            }

            public void visit(OWLDatatype entity) {
                addRendering(entity, owlDatatypeMap);
            }
        });
    }


    private <T extends OWLEntity> void addRendering(T entity, Map<String, T> map) {
        if (!entityRenderingMap.containsKey(entity)) {
            String rendering = new MyOWLEntityRendererI().render(entity);
            map.put(rendering, entity);
            entityRenderingMap.put(entity, rendering);
        }
    }


    public void removeRendering(OWLEntity owlEntity) {
        final String oldRendering = entityRenderingMap.get(owlEntity);
        entityRenderingMap.remove(owlEntity);

        owlEntity.accept(new OWLEntityVisitor() {

            public void visit(OWLClass entity) {
                owlClassMap.remove(oldRendering);
            }

            public void visit(OWLDataProperty entity) {
                owlDataPropertyMap.remove(oldRendering);
            }

            public void visit(OWLObjectProperty entity) {
                owlObjectPropertyMap.remove(oldRendering);
            }

            public void visit(OWLAnnotationProperty owlAnnotationProperty) {
                owlAnnotationPropertyMap.remove(oldRendering);
            }

            public void visit(OWLNamedIndividual entity) {
                owlIndividualMap.remove(oldRendering);
            }

            public void visit(OWLDatatype entity) {
                owlDatatypeMap.remove(oldRendering);
            }
        });
    }


    public void updateRendering(final OWLEntity ent) {
        boolean updateRendering = false;
        if (ontology.containsEntityInSignature(ent)) {
            updateRendering = true;

        }

        removeRendering(ent);
        if (updateRendering) {
            addRendering(ent);
        }
    }


    public Set<String> getOWLClassRenderings() {
        return owlClassMap.keySet();
    }


    public Set<String> getOWLObjectPropertyRenderings() {
        return owlObjectPropertyMap.keySet();
    }


    public Set<String> getOWLDataPropertyRenderings() {
        return owlDataPropertyMap.keySet();
    }


    public Set<String> getOWLAnnotationPropertyRenderings() {
        return owlAnnotationPropertyMap.keySet();
    }


    public Set<String> getOWLIndividualRenderings() {
        return owlIndividualMap.keySet();
    }


    public Set<String> getOWLDatatypeRenderings() {
        return owlDatatypeMap.keySet();
    }


    public Set<String> getOWLEntityRenderings() {
        Set<String> renderings = new TreeSet<String>();
        renderings.addAll(owlClassMap.keySet());
        renderings.addAll(owlObjectPropertyMap.keySet());
        renderings.addAll(owlDataPropertyMap.keySet());
        renderings.addAll(owlAnnotationPropertyMap.keySet());
        renderings.addAll(owlIndividualMap.keySet());
        renderings.addAll(owlDatatypeMap.keySet());
        return renderings;
    }
}
