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

    private HashMap<String, DBInstance> instances = new HashMap<String, DBInstance>();
    private ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    public DBServer getDBServer(String instance, int access, String pattern) {
        DBInstance db;
        rwLock.readLock().lock();
        try {
            db = instances.get(instance);
        } finally {
            rwLock.readLock().unlock();
        }
        if (db == null) {
            logger.warn("DBServer " + instance + " is null!");
            return null;
        }
        return db.getDBServer(access, pattern);
    }

    /**
     * 
     * @param newConfig
     * @return changed servers
     */
    public List<DBServer> reloadConfig(final Map<String, net.sinofool.dbpool.idl.DBServer[]> newConfig) {
        List<DBServer> changes = new ArrayList<DBServer>();
        rwLock.writeLock().lock();
        try {
            HashMap<String, DBInstance> newInstances = new HashMap<String, DBInstance>();
            for (Entry<String, net.sinofool.dbpool.idl.DBServer[]> entry : newConfig.entrySet()) {
                DBInstance value = instances.containsKey(entry.getKey()) ? instances.get(entry.getKey())
                        : new DBInstance();
                List<DBServer> change = value.reloadConfig(entry.getValue());
                changes.addAll(change);
                newInstances.put(entry.getKey(), value);
            }
            instances = newInstances;
            return changes;
        } finally {
            rwLock.writeLock().unlock();
        }
    }
}
