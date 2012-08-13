package at.ainf.logger;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 25.08.11
 * Time: 12:24
 * To change this template use File | Settings | File Templates.
 */

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

@Aspect
public class Test {

    private static Logger logger = LoggerFactory.getLogger(Test.class.getName());

    LogToolsManager logToolsManager;

    String aufgabe = "";

    String user = "";

    String hostname = "";

    String ontologyName = "";

    String o = "";

    void logMsg(String message) {
        logger.info(user + "; " + aufgabe + "; " + o + "; " + hostname + "; " + message );
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

        String identifier[] = {"Test ", "UQ1", "UW1", "GQ1", "GW1", "UQ2", "UW2", "GQ2", "GW2", "UQ3", "UW3", "GQ3", "GW3", "UQ4", "UW4", "GQ4", "GW4", "UQ5", "UW5", "GQ5", "GW5", "UQ6", "UW6", "GQ6", "GW6", "UQ7", "UW7", "GQ7", "GW7",  "UQ8", "UW8", "GQ8", "GW8"};
        JComboBox comboBoxD = new JComboBox(identifier);
        comboBoxD.setSelectedItem("Test ");

        String aufgaben[] = {"Tutorial", "Task_1", "Task_2"};
        JComboBox comboBox = new JComboBox(aufgaben);
        comboBox.setSelectedItem("Tutorial");

        String ontology[] = {"Koala", "Uni", "Gizk"};
        JComboBox comboBoxOntology = new JComboBox(ontology);
        comboBoxOntology.setSelectedItem("Koala");

        Object complexMsg[] = {"Please input your Identifier:", comboBoxD,
                "Which task are you doing:", comboBox,
                "Which ontology do you want to open:",comboBoxOntology};

        JOptionPane optionPane = new JOptionPane();
        optionPane.setMessage(complexMsg);
        optionPane.setMessageType(JOptionPane.QUESTION_MESSAGE);
        JDialog dialog = optionPane.createDialog(null, "User Data");
        dialog.setVisible(true);

        aufgabe = (String) comboBox.getSelectedItem();
        o = (String)comboBoxOntology.getSelectedItem();
        if (o.equals("Koala"))
            ontologyName = "ontologies/koala.owl";
        else if (o.equals("Uni"))
            ontologyName = "ontologies/uni.owl";
        else if (o.equals("Gizk"))
            ontologyName = "ontologies/gizk.owl";
        user = (String) "user" + comboBoxD.getSelectedItem();
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            hostname = "HOST";
        }

        logMsg("Program; Global; started program");

        logMsg("Program; Global; started mouse logging");

        Toolkit.getDefaultToolkit().addAWTEventListener(
                new AWTEventListener() {
                    public void eventDispatched(AWTEvent e)
                    {
                        if (e.getID() == MouseEvent.MOUSE_CLICKED) {
                            logMsg("User; Global; clicked button " + ((MouseEvent)e).getButton() + " click number   "  +
                                      ((MouseEvent)e).getClickCount());
                        }
                    }
                },
                AWTEvent.MOUSE_EVENT_MASK);
    }

     @After("protegeAppStart()")
    public void logProtegeApp() {
        logToolsManager.openDebugTab();
    }

    boolean started = false;

    @After("execution(void org.protege.editor.core.ProtegeApplication.showWelcomeFrame(..))")
    public void showWelcomeFrame(JoinPoint jp) {
        if (!started) {
            started = true;

        }

    }

    @AfterReturning(value="execution(* at.ainf.protegeview.Activator.createLogTool(..))",
            returning="logTools")
    public void logCreateLogTool(Object logTools) {
        logToolsManager = LogToolsManager.getInstance(logTools);
    }

    @Before("execution(* org.protege.editor.core.platform.PlatformArguments.getArguments(..))")
    public void modArguments() {
        System.setProperty("command.line.arg.0",ontologyName);
    }

    @Pointcut(
        "execution(void org.protege.editor.core.ProtegeApplication.stop(..))"
    )
    void protegeAppShutDown() {}

    @Before("protegeAppShutDown()")
    public void logProtegeAppShutDown() {
        logMsg("Program; Global; program shutdown  ");
    }


    @After("execution(void at.ainf.protegeview.menuactions.OpenDTabAction.actionPerformed(..))")
    public void logDebugTabOpened() {
        logMsg("User; Menu; debugTab opened");
    }


    @Before("execution(void org.protege.editor.owl.ui.inference.PrecomputeAction.actionPerformed(..))")
    public void logStartReasoner() {
        logMsg("User; Menu; reasoner started");
    }


     @Before("execution(void org.protege.editor.owl.ui.action.UndoAction.actionPerformed(..))")
    public void logUndo() {
        logMsg("User; Menu; undo done");
    }


    @Before("execution(void at.ainf.protegeview.WorkspaceTab.doCalculateHittingSet(..))")
    public void logCalculateHS() {
        logMsg("User; Toolbox; calculate HS clicked");
    }

    @Before("execution(void at.ainf.protegeview.WorkspaceTab.doResetAct(..))")
    public void logReset() {
        logMsg("User; Toolbox; toolbox reset clicked");
    }

    @Before("execution(void at.ainf.protegeview.WorkspaceTab.doConfigOptions(..))")
    public void logConfigOptions() {
        logMsg("User; Toolbox; config options clicked");
    }

    @Before("execution(void at.ainf.protegeview.WorkspaceTab.doConfigurationWizard(..))")
    public void logConfigWizard() {
        logMsg("User; Toolbox; config wizard clicked");
    }

    @Before("execution(void at.ainf.protegeview.queryaskingview.QueryShowPanel.init(..))")
    public void logGetQuery() {
        logMsg("User; Toolbox; user clicked get query");
    }

    @Before("execution(void at.ainf.protegeview.queryaskingview.QueryShowPanel.applyChanged(..))")
    public void logConfirm(JoinPoint jp) {
        logMsg("User; Toolbox; clicked confirm ;" + logToolsManager.getConfAxStr(jp.getThis()));
    }

    @AfterReturning(value="execution(* at.ainf.protegeview.controlpanel.OptionsDialog.isOkClicked(..)) && this(options)",returning="isConfirmed")
    public void logApplyOpt(boolean isConfirmed, Object options) {
        if (isConfirmed) {
            logMsg("User; Options; user applied options" + options);
        }
        else {
            logMsg("User; Options; user aborted options");
        }
    }


    @Before("execution(* at.ainf.protegeview.testcasesentailmentsview.TcaeFramelist.getSectionEditor(..)) && args(object,type)")
    public void logTestcaseAddClk(Object object, Object type) {
        logMsg("User; tcae; user opened editor to add test case;" + type + ";" + object);
    }

    @Before("execution(* at.ainf.protegeview.testcasesentailmentsview.TcaeFramelist.getSectionItemEditor(..)) && args(object,type)")
    public void logTestcaseEdClk(Object object, Object type) {
        logMsg("User; tcae; user opened editor to edit test case;" + type + ";" + object);
    }


    @AfterReturning(value="execution(* at.ainf.protegeview.testcasesentailmentsview.TcaeFramelist.isFromUserConfirmed(..))",returning="isConfirmed")
    public void logUserConfirmation(boolean isConfirmed) {
        //boolean ok = isConfirmed != null && isConfirmed.equals(JOptionPane.OK_OPTION);
        logMsg("User; AxiomEditor; user aborted editor");
    }

    @AfterReturning(value="execution(* at.ainf.protegeview.testcasesentailmentsview.axiomeditor.OWLAxiomEditor.getEditedObject(..))",returning="ret")
    public void logEditedTestcase(JoinPoint jp, Object ret) {

        String res = "";
        Set<Object> ax = (Set<Object>) ret;
        for (Object axiom : ax)
            res += logToolsManager.getRendering(axiom) + ";";

        logMsg("User; AxiomEditor; user confirmed edit edited testcase was;" + logToolsManager.getSe(jp.getThis()) + ";" + res );
    }

    @Before("execution(* at.ainf.protegeview.views.ResultsListSection.setUserMarkedThisTarget(..)) &&  args(mark)")
    public void logUserMarkedTargetDiag(JoinPoint jp, boolean mark) {
        if   (mark) {
            logMsg("User; hsview; CLICKED user marked diagnosis as target diag with confidence;"
                                          + logToolsManager.getUserTargetConfidence(jp.getThis())
                                          + ";axioms:;"
                                          + logToolsManager.getAxiomSet(jp.getThis()));
        }
        else {
            logMsg("User; hsview; user unmarked diagnosis as target diag axioms: " + logToolsManager.getAxiomSet(jp.getThis()) );
        }
    }

    @Before("execution(void at.ainf.protegeview.queryaskingview.QueryShowPanel.setEntailedMarkers(..))")
    public void logYesTestcase(JoinPoint jp) {
        logMsg ("User; queryview; user clicked yes button ;" + logToolsManager.getQueryAxioms(jp.getThis()));
    }

    @Before("execution(void at.ainf.protegeview.queryaskingview.QueryShowPanel.setNonEntailedMarkers(..))")
    public void logNoTestcase(JoinPoint jp) {

        logMsg ("User; queryview; user clicked no button ;" + logToolsManager.getQueryAxioms(jp.getThis()));
    }

    @Before("execution(void at.ainf.protegeview.configwizard.DebuggerWizard.applyPreferences(..))")
    public void logWizardApply() {
        logMsg ("User; DebuggerWizard; debugger wizard finished from user");

    }

    @Before("execution(void org.protege.editor.core.ui.wizard.WizardController.cancelButtonPressed(..))")
    public void logCancelButtonPressed() {
        logMsg ("User; DebuggerWizard; user pressed cancel button in wizard");
    }

    @Before("execution(void org.protege.editor.core.ui.wizard.WizardController.backButtonPressed(..))")
    public void logBackButtonPressed() {
        logMsg ("User; DebuggerWizard; user pressed back button in wizard");
    }

    @Before("execution(void org.protege.editor.core.ui.wizard.WizardController.nextButtonPressed(..))")
    public void logNextButtonPressed() {
        logMsg ("User; DebuggerWizard; user pressed next button in wizard");
    }

    private static int cnt = 0;

    @Before("execution(void at.ainf.protegeview.debugmanager.DebugManager.setConflictSets(..)) && args(object)")
    public void logConflictSet(Object object) {
        Set<Set<Object>> conflicts = (Set<Set<Object>>) object;
        String result = "";
        cnt++;
        if (conflicts == null)
            logMsg("Program; Global; conflict sets were set to null to reset " );

        if (conflicts != null) {
            result += "Num CS: ;" + conflicts.size() + "; ";
            for (Set<Object> conflict : conflicts) {
                String r = result + "size=;" + conflict.size() + ";";
                for (Object axiom : conflict) {
                    r += logToolsManager.getRendering(axiom) + ";";
                }
                logMsg("Program; Global; conflict sets: ; cnt=;" + cnt + ";" + r );

            }


        }
    }

    @Before("execution(void at.ainf.protegeview.debugmanager.DebugManager.setValidHittingSets(..)) && args(object)")
    public void logValHittingSet(Object object) {
        Set<Set<Object>> validHittingSets = (Set<Set<Object>>) object;
        String result = "";
        cnt++;

        if (validHittingSets == null)
            logMsg("Program; Global; hitting sets were set to null to reset " );

        if (validHittingSets != null) {
            result += "Num HS: ;" + validHittingSets.size() + "; ";
            for (Set<Object> hs : validHittingSets) {
                String r = result + " " + "size=  ;" + hs.size() + ";";
                r +=  logToolsManager.getHsRendering(hs);
                logMsg("Program; Global; hitting sets;cnt=;" + cnt + ";" + r );
            }
        }
    }

    @After("execution(void at.ainf.protegeview.queryaskingview.buttons.QueryQuestListItem.handleEntailed(..))")
    public void logAddEnt(JoinPoint jp) {
        if (logToolsManager.getMarkedStatus(jp.getThis(),"isEntailedMarked"))
            logMsg("User; queryview; CLICKED user marks axiom in query: ;entailed;" + logToolsManager.getL(jp.getThis()) + ";" + logToolsManager.getAxiom(jp.getThis()));
        else
            logMsg("User; queryview; CLICKED user unmarked axiom in query: ;entailed;" + logToolsManager.getAxiom(jp.getThis()));
    }

    @Before("execution(void at.ainf.protegeview.queryaskingview.QueryShowPanel.setQueryListModel(..)) && args(axioms)")
    public void logQueryLst(Object axioms) {
        String result = "";
        Set<Object> ax = (Set<Object>) axioms;
        for(Object axiom : ax) {
            result += logToolsManager.getRendering(axiom) + ";";
        }
        logMsg("Program; queryview; query axiom list is:; " + result);
    }

    @After("execution(void at.ainf.protegeview.queryaskingview.buttons.QueryQuestListItem.handleNotEntailed(..))")
    public void logAddNEnt(JoinPoint jp) {
        if (logToolsManager.getMarkedStatus(jp.getThis(),"isNonEntailedMarked"))
            logMsg("User; queryview; CLICKED user marks axiom in query: ;non entailed;" + logToolsManager.getL(jp.getThis()) + ";"  + logToolsManager.getAxiom(jp.getThis()));
        else
            logMsg("User; queryview; CLICKED user unmarked axiom in query: ;non entailed;" + logToolsManager.getAxiom(jp.getThis()));
    }

    @Before("execution(void at.ainf.protegeview.queryaskingview.buttons.QueryQuestListItem.handleUnknownEntailed(..))")
    public void logNoUserIdea(JoinPoint jp) {
        if (logToolsManager.getMarkedStatus(jp.getThis(),"isUnknowMarked"))
            logMsg("User; queryview; CLICKED user marks axiom in query with unknow: " + logToolsManager.getAxiom(jp.getThis()));
        else
            logMsg("User; queryview; CLICKED user unmarked axiom in query with unknow: " + logToolsManager.getAxiom(jp.getThis()));
    }

    @Before("execution(void at.ainf.protegeview.queryaskingview.QueryShowPanel.unmarkAxioms(..)) && args(entailed)")
    public void logUnmarkedAx(JoinPoint jp, boolean entailed) {
        String message = "Program; queryview; following " + (entailed ? "" : "not ") + "entailed axioms are unmarked: ";

        Set<String> axioms = logToolsManager.getQueryAx(jp.getThis(),entailed);
        for (String a : axioms)
            message += a + ", ";
        logMsg(message);

    }

    @Before("execution(* at.ainf.protegeview.testcasesentailmentsview.TcaeFramelist.removeItem(..)) && args(object)")
    public void logRemoveTestcase(Object object) {
        logMsg("User; tcae; CLICKED user remove testcase" + logToolsManager.getRemoved(object));
    }

    @After("execution(void at.ainf.protegeview.WorkspaceTab.displaySection(..))")
    public void logTcaeVupdate(JoinPoint jp) {
        Map<String,String> res = logToolsManager.getStrnTestcases(jp.getThis());
        cnt++;

        for (String k : res.keySet())
            logMsg("Program; tcae; testcase view now ;" + " cnt = ;" + cnt + ";" + k + ";" + res.get(k));

    }

    @AfterReturning(value="execution(* at.ainf.protegeview.queryaskingview.DiagProvider.getQuery(..))",returning="ret")
    public void logQuery(Object ret) {
        String r = logToolsManager.getQueryRendering(ret);
        cnt++;

        String[] l = r.split("%");
        for (String ln : l)
            logMsg("Program; queryview; query stats ;" + "cnt=;" + cnt + ";" + ln);
    }

    @Before("execution(* at.ainf.protegeview.debugmanager.DebugManager.setTreeNode(..)) && args(object)")
    public void logTreenode(JoinPoint jp, Object object) {
        logMsg("User; hsview; CLICKED show axiom in hs tree " + logToolsManager.getHsRendering(object));
    }

    @Before("execution(void at.ainf.protegeview.debugmanager.DebugManager.setAxiom(..)) && args(object)")
    public void logAxEpl(Object object) {
        String result = "";

        if (object == null)
            logMsg("Program; AxiomExplain; explain axiom is set to null to reset " );
        else
            logMsg("Program; AxiomExplain; explain axiom is now set to " + logToolsManager.getRendering(object));


    }

    @Before("execution(* at.ainf.protegeview.views.ResultsListSection.setShowEntailments(..)) && args(object)")
    public void logShowEnt(JoinPoint jp, boolean object) {
        if (object)
            logMsg("User; hsview; CLICKED marked show entailment of " + logToolsManager.getShowEntailemnts(jp.getThis()));
        else
            logMsg("User; hsview; CLICKED unmarked show entailment of " + logToolsManager.getShowEntailemnts(jp.getThis()));
    }

    @Before("execution(* org.protege.editor.owl.ui.view.AbstractOWLEntityHierarchyViewComponent.transmitSelection(..))")
    public void logUserLooks(JoinPoint jp) {
        logMsg("User; Global; CLICKED user clicks on entity " + logToolsManager.getShowEntity(jp.getThis()));
    }

    @Around("execution(* at.ainf.protegeview.backgroundsearch.BackgroundSearcher.doBackgroundSearch(..))")
    public Object logHS(ProceedingJoinPoint pj ) {
        long start = System.nanoTime();
        Object ret = null;
        try {
            ret = pj.proceed();
        } catch (Throwable throwable) {
            throwable.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        start = System.nanoTime() - start;
        logMsg("Program; Toolbox; time needed for calc HS ;" + start + " ns ");
        return ret;

    }

}
