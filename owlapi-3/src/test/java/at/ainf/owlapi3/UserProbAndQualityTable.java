package at.ainf.owlapi3;

import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 20.05.11
 * Time: 14:14
 * To change this template use File | Settings | File Templates.
 */
public class UserProbAndQualityTable {

    private HashMap<UsersProbab, HashMap<DiagProbab, TableList>> map;

    public UserProbAndQualityTable() {
        map = new HashMap<UsersProbab, HashMap<DiagProbab, TableList>>();
        for (UsersProbab usersProbab : UsersProbab.values()) {
            map.put(usersProbab, new HashMap<DiagProbab, TableList>());
            for (DiagProbab diagProbab : DiagProbab.values()) {
                map.get(usersProbab).put(diagProbab, new TableList());
            }

        }

    }

    public TableList getEntry(UsersProbab usersProbab, DiagProbab diagProbab) {
        HashMap<DiagProbab, TableList> diagProbabTableListHashMap = map.get(usersProbab);
        if (diagProbabTableListHashMap != null)
            return diagProbabTableListHashMap.get(diagProbab);
        return null;
    }

}
