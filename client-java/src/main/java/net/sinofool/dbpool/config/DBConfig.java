package net.sinofool.dbpool.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DBConfig {

	private static final Logger logger = LoggerFactory.getLogger(DBConfig.class);
	
    private HashMap<String, DBInstance> instances;
    
    //use rwLock to make the class threadsafe, and have a better performance at the same time
    private ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    public DBServer getDBServer(String instance, int access, String pattern) {
    
    	rwLock.readLock().lock();
        DBInstance db = instances.get(instance);
        rwLock.readLock().unlock();
        
        DBServer ds = null;
        if(db != null) {
        	ds = db.getDbServer(access, pattern);	
        }else {
        	logger.warn("DBServer " + instance + " is null!");
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
        
        if(!changes.isEmpty()) {
        	logger.info("New config updated");
        	
        	rwLock.writeLock().lock();
            instances = newInstances;
            rwLock.writeLock().unlock();	
        }else {
        	logger.debug("No new config updated");
        }
        
        return changes;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub

    }

}
