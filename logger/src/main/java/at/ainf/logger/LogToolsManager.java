package at.ainf.logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 14.10.11
 * Time: 13:53
 * To change this template use File | Settings | File Templates.
 */
public class LogToolsManager {

    private Object logTools;

    private Method rendererMethod;

    private Method hsrendMethod;

    private Method queryMethod;

    private Method openDebugTabMeth;

    public LogToolsManager(Object logTools) {
        this.logTools = logTools;
        try {
            rendererMethod = logTools.getClass().getMethod("getRendering", new Class[]{Object.class});
            queryMethod = logTools.getClass().getMethod("renderQuery", new Class[]{Object.class});
            hsrendMethod = logTools.getClass().getMethod("getHsRendering", new Class[]{Object.class});
            openDebugTabMeth = logTools.getClass().getMethod("openDebugTab", new Class[]{});
        } catch (NoSuchMethodException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private String executeMethod(Method method, Object axiom) {
        try {
            return (String) method.invoke(logTools, new Object[]{axiom});
        } catch (IllegalAccessException e1) {
            e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InvocationTargetException e1) {
            e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }

    public String getRendering(Object axiom) {
        return executeMethod(rendererMethod, axiom);
    }

    public String getHsRendering(Object set) {
        return executeMethod(hsrendMethod, set);
    }

    public void openDebugTab() {
        try {
            openDebugTabMeth.invoke(logTools, new Object[]{});
        } catch (IllegalAccessException e1) {
            e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InvocationTargetException e1) {
            e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public String getQueryRendering(Object set) {
        return executeMethod(queryMethod, set);
    }

    public int getUserTargetConfidence(Object object) {
        Method method;
        try {
            method = object.getClass().getMethod("getUserTargetConfidence", new Class[]{});
            return (Integer) method.invoke(object, new Object[]{});

        } catch (NoSuchMethodException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InvocationTargetException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return -1;

    }

    public String getShowEntailemnts(Object object) {
        Method method;
        try {
            method = object.getClass().getMethod("getAxiomSet", new Class[]{});
            return getHsRendering (method.invoke(object, new Object[]{}));

        } catch (NoSuchMethodException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InvocationTargetException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return  " ";

    }

    public String getAxiom(Object object) {
        Method method;
        try {
            method = object.getClass().getMethod("getAxiom", new Class[]{});
            return getRendering(method.invoke(object, new Object[]{}));

        } catch (NoSuchMethodException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InvocationTargetException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return "";

    }

    public String getStrnTestcases(Object object) {
        Method method;
        try {
            method = object.getClass().getMethod("getStrTestcases", new Class[]{});
            return ((String) method.invoke(object, new Object[]{}));

        } catch (NoSuchMethodException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InvocationTargetException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return "";

    }

    public Set<String> getQueryAx(Object object, boolean ent) {

        try {
            Method method;
            Method entailMethod = object.getClass().getMethod("getQueryAxiomsEntailed", new Class[]{});
            Method nonEntailMethod = object.getClass().getMethod("getQueryAxiomsNonEntailed", new Class[]{});

            if (ent)
                method = entailMethod;
            else
                method = nonEntailMethod;

            Set<String> result = new HashSet<String>();
            Set<Object> r = (Set<Object>) method.invoke(object, new Object[]{});
            for (Object axiom : r)
                result.add(getRendering(axiom));

            return result;


        } catch (NoSuchMethodException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InvocationTargetException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return new HashSet<String>();
    }

    public String getConfAxStr(Object object) {

        try {

            Method entailMethod = object.getClass().getMethod("getQueryAxiomsEntailed", new Class[]{});
            Method nonEntailMethod = object.getClass().getMethod("getQueryAxiomsNonEntailed", new Class[]{});
            boolean entailed = true;

            String result = "";
            Set<Object> r = (Set<Object>) entailMethod.invoke(object, new Object[]{});
            if (r.size() == 0) {
                r = (Set<Object>) nonEntailMethod.invoke(object, new Object[]{});
                entailed = false;
            }
            for (Object axiom : r)
                result += getRendering(axiom) + ", ";
            if (entailed)
                result += "entailed";
            else
                result += "not entailed";

            return result;


        } catch (NoSuchMethodException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InvocationTargetException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return "  ";

    }

    public String getQueryAxioms(Object object) {
        Method method;
        try {
            String result ="";
            method = object.getClass().getMethod("getQueryAxioms", new Class[]{});
            Set<Object> r = (Set<Object>) method.invoke(object, new Object[]{});
            for (Object axiom : r)
                result += getRendering(axiom) + ", ";

            return result;


        } catch (NoSuchMethodException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InvocationTargetException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return "";

    }

    public boolean getMarkedStatus(Object object, String methodName) {
        Method method;
        try {
            method = object.getClass().getMethod(methodName, new Class[]{});
            return (Boolean) method.invoke(object, new Object[]{});
        } catch (NoSuchMethodException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InvocationTargetException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return false;
    }

    public String getAxiomSet(Object object) {
        Method method;
        try {
            String result = "";

            Method rendererMethod = object.getClass().getMethod("getAxiomSet", new Class[]{});
            Set<Object> set = (Set<Object>) rendererMethod.invoke(object, new Object[]{});

            for (Object axiom : set)
                result = result + logToolsManager.getRendering(axiom) + ", ";

            return result;

        } catch (NoSuchMethodException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InvocationTargetException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return "";

    }

    public String getRemoved(Object object) {
        Method method;
        try {

            String result = " ";

            Method typeMethod = object.getClass().getMethod("getSectionType", new Class[]{});
            Object type = typeMethod.invoke(object, new Object[]{});
            result += type.toString() + ": ";
            Method rendererMethod = object.getClass().getMethod("getAxioms", new Class[]{});
            Set<Object> set = (Set<Object>) rendererMethod.invoke(object, new Object[]{});

            for (Object axiom : set)
                result = result + logToolsManager.getRendering(axiom) + ", ";

            return result;

        } catch (NoSuchMethodException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InvocationTargetException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return "";

    }

    public String getShowEntity(Object object) {

        try {
            String result = " ";

            Method enMethod = object.getClass().getMethod("getSelectedEntity", new Class[]{});
            Object entity = enMethod.invoke(object, new Object[]{});

            Method typeMethod = entity.getClass().getMethod("getEntityType", new Class[]{});
            Object type =  typeMethod.invoke(entity, new Object[]{});

             result += " " + type.toString() + " ";
            result += getRendering(entity);

            return result;

        } catch (NoSuchMethodException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InvocationTargetException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return "";
    }

    private static LogToolsManager logToolsManager;

    public static LogToolsManager getInstance(Object logTools) {
        if (logToolsManager == null) logToolsManager = new LogToolsManager(logTools);

        return logToolsManager;
    }

}
