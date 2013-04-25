package net.sinofool.dbpool.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class DBConfig {

    private HashMap<String, DBInstance> instances;

    public DBServer getDBServer(String instance, int access, String pattern) {
    	// TODO need lock
        DBInstance db = instances.get(instance);
        DBServer ds = null;
        if(db != null) {
        	ds = db.getDbServer(access, pattern);	
        }else {
        	// TODO log warn
        }
        return ds;
    }

    /**
     * 
     * @param newConfig
     * @return changed servers
     */
    public synchronized List<DBServer> reloadConfig(final Map<String, net.sinofool.dbpool.idl.DBServer[]> newConfig) {
        List<DBServer> changes = new ArrayList<DBServer>();
        HashMap<String, DBInstance> newInstances = new HashMap<String, DBInstance>();
        for (Entry<String, net.sinofool.dbpool.idl.DBServer[]> entry : newConfig.entrySet()) {
            DBInstance value = new DBInstance();
            List<DBServer> change = value.reloadConfig(entry.getValue());
            changes.addAll(change);
            newInstances.put(entry.getKey(), value);
        }
        instances = newInstances;
        return changes;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub

    }

}
