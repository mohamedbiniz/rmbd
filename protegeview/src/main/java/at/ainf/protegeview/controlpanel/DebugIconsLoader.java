package at.ainf.protegeview.controlpanel;

import javax.swing.*;
import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 03.08.11
 * Time: 11:21
 * To change this template use File | Settings | File Templates.
 */
public class DebugIconsLoader {


	public static final String CLASSADD = "class.add.png";

    public static final String DATARANGEADD = "datarange.add.png";

    public static final String INDIVIDUALADD = "individual.add.png";

    public static final String PROPERTYDATA = "property.data.add.png";

    public static final String PROPERTYOBJECT = "property.object.add.png";

    public static final String LOAD = "project.open.gif";

    public static final String SAVE = "project.save.gif";

    public static final String YESFORALL = "yes.gif";

    public static final String NOFORALL = "no.gif";

    public static final String YESFORALLACTIVATED = "ayes.gif";

    public static final String NOFORALLACTIVATED = "actno.gif";

    public static final String OPTIONS = "Option.png";

    public static final String SEARCH = "Search.png";

    public static final String WIZARD = "Wizard.png";

    public static final String CLEAR = "clear.png";

    public static final String BLANK = "blank.gif";

    public static final String INIT = "init.png";

    public static final String CONFIRM = "green.png";

    public static final String QUERY = "query.png";

    public static final String DIAGNOSES = "diagnoses.png";

    public static final String OPTION = "options.png";

    public static final String TESTCASES = "testcases.png";

    public static final String PREVIEW = "preview.png";

    public static final String REVISION = "revision.png";

    public static final String METAMODELING = "metamodeling.png";

    public static final String NEXT = "next.png";



	public static final String RELATIVE_PATH = "/lib/icons/";

	public static ImageIcon getIcon(String iconName) {
		ImageIcon imageIcon = null;
		URL iconURL = DebugIconsLoader.class.getResource(RELATIVE_PATH + iconName);
		if(iconURL != null) {
			imageIcon = new ImageIcon(iconURL);
		}

		return imageIcon;
	}



}
