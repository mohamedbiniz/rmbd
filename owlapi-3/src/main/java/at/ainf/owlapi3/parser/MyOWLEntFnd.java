package at.ainf.owlapi3.parser;

import org.apache.log4j.Logger;
//import org.protege.editor.owl.model.OWLModelManagerImpl;
//import org.protege.editor.owl.model.cache.OWLEntityRenderingCache;
//import org.protege.editor.owl.model.find.OWLEntityFinder;
//import org.protege.editor.owl.model.find.OWLEntityFinderPreferences;
//import org.protege.editor.owl.model.util.OWLDataTypeUtils;
import org.semanticweb.owlapi.expression.OWLEntityChecker;
import org.semanticweb.owlapi.model.*;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 18.04.11
 * Time: 13:06
 * To change this template use File | Settings | File Templates.
 */
public class MyOWLEntFnd implements OWLEntityChecker {

    private static final Logger logger = Logger.getLogger(MyOWLEntFnd.class);

    private MyOWLEntRenderCache renderingCache;

    //private OWLModelManagerImpl mngr;

    private static final String WILDCARD = "*";

    private OWLOntology ont;

    public MyOWLEntFnd(OWLOntology ont) {

        MyOWLEntRenderCache renderingCache = new MyOWLEntRenderCache(ont);
        renderingCache.rebuild();
        this.renderingCache = renderingCache;
        this.ont = ont;
    }


    private static final String ESCAPE_CHAR = "'";

    public OWLClass getOWLClass(String rendering) {
        OWLClass cls = renderingCache.getOWLClass(rendering);
        if (cls == null && !rendering.startsWith(ESCAPE_CHAR) && !rendering.endsWith(ESCAPE_CHAR)) {
            cls = renderingCache.getOWLClass(ESCAPE_CHAR + rendering + ESCAPE_CHAR);
        }
        return cls;
    }


    public OWLObjectProperty getOWLObjectProperty(String rendering) {
        OWLObjectProperty prop = renderingCache.getOWLObjectProperty(rendering);
        if (prop == null && !rendering.startsWith(ESCAPE_CHAR) && !rendering.endsWith(ESCAPE_CHAR)) {
            prop = renderingCache.getOWLObjectProperty(ESCAPE_CHAR + rendering + ESCAPE_CHAR);
        }
        return prop;
    }


    public OWLDataProperty getOWLDataProperty(String rendering) {
        OWLDataProperty prop = renderingCache.getOWLDataProperty(rendering);
        if (prop == null && !rendering.startsWith(ESCAPE_CHAR) && !rendering.endsWith(ESCAPE_CHAR)) {
            prop = renderingCache.getOWLDataProperty(ESCAPE_CHAR + rendering + ESCAPE_CHAR);
        }
        return prop;
    }


    public OWLAnnotationProperty getOWLAnnotationProperty(String rendering) {
        OWLAnnotationProperty prop = renderingCache.getOWLAnnotationProperty(rendering);
        if (prop == null && !rendering.startsWith(ESCAPE_CHAR) && !rendering.endsWith(ESCAPE_CHAR)) {
            prop = renderingCache.getOWLAnnotationProperty(ESCAPE_CHAR + rendering + ESCAPE_CHAR);
        }
        return prop;
    }


    public OWLNamedIndividual getOWLIndividual(String rendering) {
        OWLNamedIndividual individual = renderingCache.getOWLIndividual(rendering);
        if (individual == null && !rendering.startsWith(ESCAPE_CHAR) && !rendering.endsWith(ESCAPE_CHAR)) {
            individual = renderingCache.getOWLIndividual(ESCAPE_CHAR + rendering + ESCAPE_CHAR);
        }
        return individual;
    }


    public OWLDatatype getOWLDatatype(String rendering) {
        OWLDatatype dataType = renderingCache.getOWLDatatype(rendering);
        if (dataType == null && !rendering.startsWith(ESCAPE_CHAR) && !rendering.endsWith(ESCAPE_CHAR)) {
            dataType = renderingCache.getOWLDatatype(ESCAPE_CHAR + rendering + ESCAPE_CHAR);
        }
        return dataType;
    }


    public OWLEntity getOWLEntity(String rendering) {
        OWLEntity entity = renderingCache.getOWLEntity(rendering);
        if (entity == null && !rendering.startsWith(ESCAPE_CHAR) && !rendering.endsWith(ESCAPE_CHAR)) {
            entity = renderingCache.getOWLEntity(ESCAPE_CHAR + rendering + ESCAPE_CHAR);
        }
        return entity;
    }


    public Set<String> getOWLEntityRenderings() {
        return renderingCache.getOWLEntityRenderings();
    }






    private <T extends OWLEntity> Set<T> getEntities(String match, Class<T> type, boolean fullRegExp, int flags) {
        if (match.length() == 0) {
            return Collections.emptySet();
        }

        if (fullRegExp) {
            return doRegExpSearch(match, type, flags);
        } else {
            return doWildcardSearch(match, type);
        }
    }


    private <T extends OWLEntity> Set<T> doRegExpSearch(String match, Class<T> type, int flags) {
        Set<T> results = new TreeSet<T>();
        try {
            Pattern pattern = Pattern.compile(match, flags);
            for (String rendering : getRenderings(type)) {
                Matcher m = pattern.matcher(rendering);
                if (m.find()) {
                    T ent = getEntity(rendering, type);
                    if (ent != null) {
                        results.add(ent);
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Invalid regular expression: " + e.getMessage());
        }
        return results;
    }

    /* @fix wildcard searching - it does not handle the usecases correctly
     * eg A*B will not work, and endsWith is implemented the same as contains
     * (probably right but this should not be implemented separately)
     */
    private <T extends OWLEntity> Set<T> doWildcardSearch(String match, Class<T> type) {
        Set<T> results = new TreeSet<T>();

        if (match.equals(WILDCARD)) {
            results = getAllEntities(type);
        } else {
            SimpleWildCardMatcher matcher;
            if (match.startsWith(WILDCARD)) {
                if (match.length() > 1 && match.endsWith(WILDCARD)) {
                    // Contains
                    matcher = new SimpleWildCardMatcher() {
                        public boolean matches(String rendering, String s) {
                            return rendering.indexOf(s) != -1;
                        }
                    };
                    match = match.substring(1, match.length() - 1);
                } else {
                    // Ends with
                    matcher = new SimpleWildCardMatcher() {
                        public boolean matches(String rendering, String s) {
                            return rendering.indexOf(s) != -1;
                        }
                    };
                    match = match.substring(1, match.length());
                }
            } else {
                // Starts with
                if (match.endsWith(WILDCARD) && match.length() > 1) {
                    match = match.substring(0, match.length() - 1);
                }
                // handle matches exactly?
                matcher = new SimpleWildCardMatcher() {
                    public boolean matches(String rendering, String s) {
                        return rendering.startsWith(s) || rendering.startsWith("'" + s);
                    }
                };
            }

            if (match.trim().length() == 0) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Attempt to match the empty string (no results)");
                }
            } else {
                match = match.toLowerCase();
                if (logger.isDebugEnabled()) {
                    logger.debug("Match: " + match);
                }
                for (String rendering : getRenderings(type)) {
                    if (rendering.length() > 0) {
                        if (matcher.matches(rendering.toLowerCase(), match)) {
                            results.add(getEntity(rendering, type));
                        }
                    }
                }
            }

        }

        return results;
    }


    @SuppressWarnings("unchecked")
    private <T extends OWLEntity> Set<T> getAllEntities(Class<T> type) {
        if (type.equals(OWLDatatype.class)) {
            TreeSet<OWLOntology> TreeSet = new TreeSet<OWLOntology>();
            TreeSet.add(ont);
            return (Set<T>) new MyOWLDataTypeUtils(ont.getOWLOntologyManager()).getKnownDatatypes(TreeSet);
        } else {
            Set<T> entities = new TreeSet<T>();

            if (type.equals(OWLClass.class)) {
                entities.addAll((Set<T>) ont.getClassesInSignature());
            } else if (type.equals(OWLObjectProperty.class)) {
                entities.addAll((Set<T>) ont.getObjectPropertiesInSignature());
            } else if (type.equals((OWLDataProperty.class))) {
                entities.addAll((Set<T>) ont.getDataPropertiesInSignature());
            } else if (type.equals(OWLIndividual.class)) {
                entities.addAll((Set<T>) ont.getIndividualsInSignature());
            } else if (type.equals(OWLAnnotationProperty.class)) {
                entities.addAll((Set<T>) ont.getAnnotationPropertiesInSignature());
            } else if (type.equals(OWLDatatype.class)) {
                entities.addAll((Set<T>) ont.getDatatypesInSignature());
            }

            return entities;
        }
    }


    private <T extends OWLEntity> T getEntity(String rendering, Class<T> type) {
        if (OWLClass.class.isAssignableFrom(type)) {
            return type.cast(renderingCache.getOWLClass(rendering));
        } else if (OWLObjectProperty.class.isAssignableFrom(type)) {
            return type.cast(renderingCache.getOWLObjectProperty(rendering));
        } else if (OWLDataProperty.class.isAssignableFrom(type)) {
            return type.cast(renderingCache.getOWLDataProperty(rendering));
        } else if (OWLNamedIndividual.class.isAssignableFrom(type)) {
            return type.cast(renderingCache.getOWLIndividual(rendering));
        } else if (OWLAnnotationProperty.class.isAssignableFrom(type)) {
            return type.cast(renderingCache.getOWLAnnotationProperty(rendering));
        } else if (OWLDatatype.class.isAssignableFrom(type)) {
            return type.cast(renderingCache.getOWLDatatype(rendering));
        } else {
            return type.cast(renderingCache.getOWLEntity(rendering));
        }
    }


    private <T extends OWLEntity> Set<String> getRenderings(Class<T> type) {
        if (OWLClass.class.isAssignableFrom(type)) {
            return renderingCache.getOWLClassRenderings();
        } else if (OWLObjectProperty.class.isAssignableFrom(type)) {
            return renderingCache.getOWLObjectPropertyRenderings();
        } else if (OWLDataProperty.class.isAssignableFrom(type)) {
            return renderingCache.getOWLDataPropertyRenderings();
        } else if (OWLNamedIndividual.class.isAssignableFrom(type)) {
            return renderingCache.getOWLIndividualRenderings();
        } else if (OWLAnnotationProperty.class.isAssignableFrom(type)) {
            return renderingCache.getOWLAnnotationPropertyRenderings();
        } else if (OWLDatatype.class.isAssignableFrom(type)) {
            return renderingCache.getOWLDatatypeRenderings();
        } else {
            return renderingCache.getOWLEntityRenderings();
        }
    }


    private interface SimpleWildCardMatcher {

        boolean matches(String rendering, String s);
    }
}
