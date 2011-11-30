package at.ainf.logger;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 25.08.11
 * Time: 12:24
 * To change this template use File | Settings | File Templates.
 */

import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.StringTokenizer;

@Aspect
public class Test {

    private static Logger logger = Logger.getLogger(Test.class.getName());

    LogToolsManager logToolsManager;

    String aufgabe = "";

    String user = "";

    void logMsg(String message) {
        logger.info(user + "; " + aufgabe + "; " + message + " " + hmacSha1(user + "; " + aufgabe +"; "+message,"k"));
    }


    static final String HEXES = "0123456789ABCDEF";
  public static String getHex( byte [] raw ) {
    if ( raw == null ) {
      return null;
    }
    final StringBuilder hex = new StringBuilder( 2 * raw.length );
    for ( final byte b : raw ) {
      hex.append(HEXES.charAt((b & 0xF0) >> 4))
         .append(HEXES.charAt((b & 0x0F)));
    }
    return hex.toString();
  }

    public static String hmacSha1(String value, String key) {
            try {
                // Get an hmac_sha1 key from the raw key bytes
                byte[] keyBytes = key.getBytes();
                SecretKeySpec signingKey = new SecretKeySpec(keyBytes, "HmacSHA1");

                // Get an hmac_sha1 Mac instance and initialize with the signing key
                Mac mac = Mac.getInstance("HmacSHA1");
                mac.init(signingKey);

                // Compute the hmac on input data bytes
                byte[] rawHmac = mac.doFinal(value.getBytes());

                // Convert raw bytes to Hex
                //byte[] hexBytes = new Hex().encode(rawHmac);

                //  Covert array of Hex bytes to a String
                return getHex(rawHmac);  // return new String(hexBytes, "UTF-8");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }


    @Pointcut(
        "execution(void org.protege.editor.core.ProtegeApplication.start(..))"
    )
    void protegeAppStart() {}

    @Before("protegeAppStart()")
    public void logProtegeAppStart() {

        JTextField usernameField = new JTextField("matnr");

        String aufgaben[] = {"Task_1", "Task_2", "Task_3"};
        JComboBox comboBox = new JComboBox(aufgaben);
        comboBox.setSelectedItem("Task_1");

        Object complexMsg[] = {"Please input your Matriculation Number:", usernameField, "Which task are you doing:", comboBox};

        JOptionPane optionPane = new JOptionPane();
        optionPane.setMessage(complexMsg);
        optionPane.setMessageType(JOptionPane.QUESTION_MESSAGE);
        JDialog dialog = optionPane.createDialog(null, "User Data");
        dialog.setVisible(true);

        aufgabe = (String) comboBox.getSelectedItem();
        user = usernameField.getText();

        logMsg("GLOBAL program has started");

        logMsg("start logging mouse events ");

        Toolkit.getDefaultToolkit().addAWTEventListener(
                new AWTEventListener() {
                    public void eventDispatched(AWTEvent e)
                    {
                        if (e.getID() == MouseEvent.MOUSE_CLICKED) {
                            logMsg("User clicked button " + ((MouseEvent)e).getButton() + " click number   "  +
                                      ((MouseEvent)e).getClickCount());
                        }
                    }
                },
                AWTEvent.MOUSE_EVENT_MASK);
    }

    @AfterReturning(value="execution(* at.ainf.pluginprotege.BundleActivator.createLogTool(..))",
            returning="logTools")
    public void logCreateLogTool(Object logTools) {
        logToolsManager = LogToolsManager.getInstance(logTools);
    }


    @Pointcut(
        "execution(void org.protege.editor.core.ProtegeApplication.stop(..))"
    )
    void protegeAppShutDown() {}

    @Before("protegeAppShutDown()")
    public void logProtegeAppShutDown() {
        logMsg("GLOBAL program shut down");
    }


    @After("execution(void at.ainf.pluginprotege.menuactions.OpenDTabAction.actionPerformed(..))")
    public void logDebugTabOpened() {
        logMsg("MENU debug tab was opened");
    }


    @Before("execution(void org.protege.editor.owl.ui.inference.PrecomputeAction.actionPerformed(..))")
    public void logStartReasoner() {
        logMsg("MENU reasoner started");
    }


     @Before("execution(void org.protege.editor.owl.ui.action.UndoAction.actionPerformed(..))")
    public void logUndo() {
        logMsg("MENU undo done");
    }


    @Before("execution(void at.ainf.pluginprotege.WorkspaceTab.doCalculateHittingSet(..))")
    public void logCalculateHS() {
        logMsg("TOOLBOX CLICKED calculate HS clicked");
    }

    @Before("execution(void at.ainf.pluginprotege.WorkspaceTab.doResetAct(..))")
    public void logReset() {
        logMsg("TOOLBOX CLICKED reset clicked");
    }

    @Before("execution(void at.ainf.pluginprotege.WorkspaceTab.doConfigOptions(..))")
    public void logConfigOptions() {
        logMsg("TOOLBOX CLICKED config options clicked");
    }

    @Before("execution(void at.ainf.pluginprotege.WorkspaceTab.doConfigurationWizard(..))")
    public void logConfigWizard() {
        logMsg("TOOLBOX CLICKED config wizard clicked");
    }

    @Before("execution(void at.ainf.pluginprotege.queryaskingview.QueryShowPanel.init(..))")
    public void logGetQuery() {
        logMsg("TOOLBOX CLICKED user clicked get query");
    }

    @Before("execution(void at.ainf.pluginprotege.queryaskingview.QueryShowPanel.applyChanged(..))")
    public void logConfirm(JoinPoint jp) {
        logMsg("TOOLBOX CLICKED clicked confirm " + logToolsManager.getConfAxStr(jp.getThis()));
    }

    @AfterReturning(value="execution(* at.ainf.pluginprotege.controlpanel.OptionsDialog.isOkClicked(..)) && this(options)",returning="isConfirmed")
    public void logApplyOpt(boolean isConfirmed, Object options) {
        if (isConfirmed) {
            logMsg("TOOLBOX CLICKED user applied options" + options);
        }
        else {
            logMsg("TOOLBOX CLICKED user aborted options");
        }
    }


    @Before("execution(* at.ainf.pluginprotege.testcasesentailmentsview.TcaeFramelist.getSectionEditor(..)) && args(object,type)")
    public void logTestcaseAddClk(Object object, Object type) {
        logMsg("TCAE CLICKED user opened editor to add test case  " + object + type);
    }

    @Before("execution(* at.ainf.pluginprotege.testcasesentailmentsview.TcaeFramelist.getSectionItemEditor(..)) && args(object,type)")
    public void logTestcaseEdClk(Object object, Object type) {
        logMsg("TCAE CLICKED user opened editor to edit test case  " + object + " " + type);
    }


    @AfterReturning(value="execution(* at.ainf.pluginprotege.testcasesentailmentsview.TcaeFramelist.isFromUserConfirmed(..))",returning="isConfirmed")
    public void logUserConfirmation(boolean isConfirmed) {
        //boolean ok = isConfirmed != null && isConfirmed.equals(JOptionPane.OK_OPTION);
        logMsg("TCAE CLICKED user aborted editor");
    }

    @AfterReturning(value="execution(* at.ainf.pluginprotege.testcasesentailmentsview.axiomeditor.OWLAxiomEditor.getEditedObject(..))",returning="ret")
    public void logEditedTestcase(Object ret) {

        String res = "";
        Set<Object> ax = (Set<Object>) ret;
        for (Object axiom : ax)
            res += logToolsManager.getRendering(axiom) + ", ";

        logMsg("TCAE CLICKED user confirmed edit edited testcase was " + res );
    }

    @Before("execution(* at.ainf.pluginprotege.views.ResultsListSection.setUserMarkedThisTarget(..)) &&  args(mark)")
    public void logUserMarkedTargetDiag(JoinPoint jp, boolean mark) {
        if   (mark) {
            logMsg("HSVIEW CLICKED user marked diagnosis as target diag with confidence "
                                          + logToolsManager.getUserTargetConfidence(jp.getThis())
                                          + " axioms: "
                                          + logToolsManager.getAxiomSet(jp.getThis()));
        }
        else {
            logMsg("HSVIEW CLICKED user unmarked diagnosis as target diag axioms: " + logToolsManager.getAxiomSet(jp.getThis()) );
        }
    }

    @Before("execution(void at.ainf.pluginprotege.queryaskingview.QueryShowPanel.setEntailedMarkers(..))")
    public void logYesTestcase(JoinPoint jp) {
        logMsg ("QUERYVIEW CLICKED user clicked yes button " + logToolsManager.getQueryAxioms(jp.getThis()));
    }

    @Before("execution(void at.ainf.pluginprotege.queryaskingview.QueryShowPanel.setNonEntailedMarkers(..))")
    public void logNoTestcase(JoinPoint jp) {

        logMsg ("QUERYVIEW CLICKED user clicked no button " + logToolsManager.getQueryAxioms(jp.getThis()));
    }

    @Before("execution(void at.ainf.pluginprotege.configwizard.DebuggerWizard.applyPreferences(..))")
    public void logWizardApply() {
        logMsg ("TOOLBOX CLICKED debugger wizard finished from user");

    }

    @Before("execution(void org.protege.editor.core.ui.wizard.WizardController.cancelButtonPressed(..))")
    public void logCancelButtonPressed() {
        logMsg ("TOOLBOX CLICKED user pressed cancel button in wizard");
    }

    @Before("execution(void org.protege.editor.core.ui.wizard.WizardController.backButtonPressed(..))")
    public void logBackButtonPressed() {
        logMsg ("TOOLBOX CLICKED user pressed back button in wizard");
    }

    @Before("execution(void org.protege.editor.core.ui.wizard.WizardController.nextButtonPressed(..))")
    public void logNextButtonPressed() {
        logMsg ("TOOLBOX CLICKED user pressed next button in wizard");
    }

    @Before("execution(void at.ainf.pluginprotege.debugmanager.DebugManager.setConflictSets(..)) && args(object)")
    public void logConflictSet(Object object) {
        Set<Set<Object>> conflicts = (Set<Set<Object>>) object;
        String result = "";

        if (conflicts == null)
            logMsg("PROGRAM conflict sets were set to null to reset " );

        if (conflicts != null) {
            result += "Num CS: " + conflicts.size() + " ";
            for (Set<Object> conflict : conflicts) {
                result += "[" + "size=" + conflict.size() + " ";
                for (Object axiom : conflict) {
                    result += logToolsManager.getRendering(axiom) + ", ";
                }
                result +=  "] ";
            }
            logMsg("PROGRAM conflict sets: " + result);
        }
    }

    @Before("execution(void at.ainf.pluginprotege.debugmanager.DebugManager.setValidHittingSets(..)) && args(object)")
    public void logValHittingSet(Object object) {
        Set<Set<Object>> validHittingSets = (Set<Set<Object>>) object;
        String result = "";

        if (validHittingSets == null)
            logMsg("PROGRAM hitting sets were set to null to reset " );

        if (validHittingSets != null) {
            result += "Num HS: " + validHittingSets.size() + " ";
            for (Set<Object> hs : validHittingSets) {
                result += "[" + "size=" + validHittingSets.size() + " ";
                result +=  logToolsManager.getHsRendering(hs) + "]  ";
            }
            logMsg("PROGRAM hitting sets "  + result);
        }
    }

    @Before("execution(void at.ainf.pluginprotege.queryaskingview.buttons.QueryQuestListItem.handleEntailed(..))")
    public void logAddEnt(JoinPoint jp) {
        if (!logToolsManager.getMarkedStatus(jp.getThis(),"isEntailedMarked"))
            logMsg("QUERYVIEW CLICKED user marks axiom in query entailed: " + logToolsManager.getAxiom(jp.getThis()));
        else
            logMsg("QUERYVIEW CLICKED user unmarked axiom in query entailed: " + logToolsManager.getAxiom(jp.getThis()));
    }

    @Before("execution(void at.ainf.pluginprotege.queryaskingview.QueryShowPanel.setQueryListModel(..)) && args(axioms)")
    public void logQueryLst(Object axioms) {
        String result = "";
        Set<Object> ax = (Set<Object>) axioms;
        for(Object axiom : ax) {
            result += logToolsManager.getRendering(axiom) + ", ";
        }
        logMsg("PROGRAM query axiom list is: " + result);
    }

    @Before("execution(void at.ainf.pluginprotege.queryaskingview.buttons.QueryQuestListItem.handleNotEntailed(..))")
    public void logAddNEnt(JoinPoint jp) {
        if (!logToolsManager.getMarkedStatus(jp.getThis(),"isNonEntailedMarked"))
            logMsg("QUERYVIEW CLICKED user marks axiom in query non entailed: " + logToolsManager.getAxiom(jp.getThis()));
        else
            logMsg("QUERYVIEW CLICKED user unmarked axiom in query non entailed: " + logToolsManager.getAxiom(jp.getThis()));
    }

    @Before("execution(void at.ainf.pluginprotege.queryaskingview.buttons.QueryQuestListItem.handleUnknownEntailed(..))")
    public void logNoUserIdea(JoinPoint jp) {
        if (!logToolsManager.getMarkedStatus(jp.getThis(),"isUnknowMarked"))
            logMsg("QUERYVIEW CLICKED user marks axiom in query with unknow: " + logToolsManager.getAxiom(jp.getThis()));
        else
            logMsg("QUERYVIEW CLICKED user unmarked axiom in query with unknow: " + logToolsManager.getAxiom(jp.getThis()));
    }

    @Before("execution(void at.ainf.pluginprotege.queryaskingview.QueryShowPanel.unmarkAxioms(..)) && args(entailed)")
    public void logUnmarkedAx(JoinPoint jp, boolean entailed) {
        String message = "PROGRAM following " + (entailed ? "" : "not ") + "entailed axioms are unmarked: ";

        Set<String> axioms = logToolsManager.getQueryAx(jp.getThis(),entailed);
        for (String a : axioms)
            message += a + ", ";
        logMsg(message);

    }

    @Before("execution(* at.ainf.pluginprotege.testcasesentailmentsview.TcaeFramelist.removeItem(..)) && args(object)")
    public void logRemoveTestcase(Object object) {
        logMsg("TCAE CLICKED user remove testcase" + logToolsManager.getRemoved(object));
    }

    @After("execution(void at.ainf.pluginprotege.WorkspaceTab.displaySection(..))")
    public void logTcaeVupdate(JoinPoint jp) {
        logMsg("PROGRAM testcase view now " +logToolsManager.getStrnTestcases(jp.getThis()));
    }

    @AfterReturning(value="execution(* at.ainf.pluginprotege.queryaskingview.DiagProvider.getQuery(..))",returning="ret")
    public void logQuery(Object ret) {
        logMsg("QUERYVIEW PROGRAM " + logToolsManager.getQueryRendering(ret) );
    }

    @Before("execution(* at.ainf.pluginprotege.debugmanager.DebugManager.setTreeNode(..)) && args(object)")
    public void logTreenode(JoinPoint jp, Object object) {
        logMsg("HSVIEW CLICKED show axiom in hs tree " + logToolsManager.getHsRendering(object));
    }

    @Before("execution(void at.ainf.pluginprotege.debugmanager.DebugManager.setAxiom(..)) && args(object)")
    public void logAxEpl(Object object) {
        String result = "";

        if (object == null)
            logMsg("PROGRAM explain axiom is set to null to reset " );
        else
            logMsg("PROGRAM explain axiom is now set to " + logToolsManager.getRendering(object));


    }

    @Before("execution(* at.ainf.pluginprotege.views.ResultsListSection.setShowEntailments(..)) && args(object)")
    public void logShowEnt(JoinPoint jp, boolean object) {
        if (object)
            logMsg("HSVIEW CLICKED marked show entailment of " + logToolsManager.getShowEntailemnts(jp.getThis()));
        else
            logMsg("HSVIEW CLICKED unmarked show entailment of " + logToolsManager.getShowEntailemnts(jp.getThis()));
    }

    @Before("execution(* org.protege.editor.owl.ui.view.AbstractOWLEntityHierarchyViewComponent.transmitSelection(..))")
    public void logUserLooks(JoinPoint jp) {
        logMsg("GLOBAL CLICKED user clicks on entity " + logToolsManager.getShowEntity(jp.getThis()));
    }

    @Around("execution(* at.ainf.pluginprotege.backgroundsearch.BackgroundSearcher.doBackgroundSearch(..))")
    public Object logHS(ProceedingJoinPoint pj ) {
        long start = System.nanoTime();
        Object ret = null;
        try {
            ret = pj.proceed();
        } catch (Throwable throwable) {
            throwable.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        start = System.nanoTime() - start;
        logMsg("PROGRAM time needed for calc HS " + start + "ns ");
        return ret;

    }

}
