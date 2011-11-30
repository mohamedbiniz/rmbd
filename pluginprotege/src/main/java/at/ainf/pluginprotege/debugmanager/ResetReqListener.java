package at.ainf.pluginprotege.debugmanager;

import java.util.EventListener;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 14.09.11
 * Time: 14:34
 * To change this template use File | Settings | File Templates.
 */
public interface ResetReqListener extends EventListener {

    void processResetReq (ResetReqEvent e);

}
