package net.sinofool.dbpool.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.sinofool.dbpool.idl.DBPoolServerPrx;
import net.sinofool.dbpool.idl.DBPoolServerPrxHelper;

public class DBConfig {
    
    private HashMap<String, DBInstance> instances;

    public DBServer getDBServer(String instance, int access, String pattern) {
        DBInstance db = instances.get(instance);
        DBServer ds = db.getDbServer(access, pattern);
        return ds;
    }

    public synchronized boolean reloadConfig() {
        Ice.Communicator ic = Ice.Util.initialize();
        Ice.ObjectPrx prx = ic.stringToProxy("M:default -h 127.0.0.1 -p 10000");
        DBPoolServerPrx dbserver = DBPoolServerPrxHelper.uncheckedCast(prx);
        HashMap<String, DBInstance> newInstances = new HashMap<String, DBInstance>();
        Map<String, net.sinofool.dbpool.idl.DBServer[]> newone = dbserver.getDBInstanceDict();
        for(Entry<String, net.sinofool.dbpool.idl.DBServer[]> entry : newone.entrySet()) {
            DBInstance value = new DBInstance();
            value.reloadConfig(entry.getValue());
            newInstances.put(entry.getKey(), value);
        }
        instances = newInstances;
        return true;
    }
    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub

    }

}
