package net.sinofool.dbpool.config;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.sinofool.dbpool.DBPool;

public class DBInstance {

    private Random random = new Random(System.currentTimeMillis());

    private ArrayList<DBServer> servers = new ArrayList<DBServer>();
    
    //use rwLock to make the class threadsafe, and have a better performance at the same time
    private ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    private int access(String acc) {
        // TODO needs more
        if (acc.equalsIgnoreCase("rw")) {
            return DBPool.WRITE_ACCESS | DBPool.READ_ACCESS;
        }
        if (acc.equalsIgnoreCase("r")) {
            return DBPool.READ_ACCESS;
        }
        if (acc.equalsIgnoreCase("w")) {
            return DBPool.WRITE_ACCESS;
        }
        return 0;
    }

    private String driver(String type) {
        if (type.equals("mysql")) {
            return "com.mysql.jdbc.Driver";
        }
        return type;
    }

    public synchronized List<DBServer> reloadConfig(net.sinofool.dbpool.idl.DBServer[] sers) {
        ArrayList<DBServer> newServers = new ArrayList<DBServer>();
        for (net.sinofool.dbpool.idl.DBServer ser : sers) {
            DBServer dbs = new DBServer();
            dbs.driver = driver(ser.type);
            dbs.host = ser.host;
            dbs.port = ser.port;
            dbs.db = ser.db;
            dbs.user = ser.user;
            dbs.pass = ser.pass;

            dbs.coreSize = ser.coreSize;
            dbs.maxSize = ser.maxSize;
            dbs.idleTimeSeconds = ser.idleTimeSeconds;

            dbs.expression = ser.expression;
            dbs.weight = ser.weight;
            dbs.access = access(ser.access);

            newServers.add(dbs);
        }

        // TODO need better alg to get changes
        ArrayList<DBServer> changes;
        do {
            if (servers.size() != newServers.size()) {
                changes = servers;
                break;
            }
            changes = new ArrayList<DBServer>();
            for (int i = 0; i < servers.size(); ++i) {
                if (servers.get(i).checksum() != newServers.get(i).checksum()) {
                    changes.add(servers.get(i));
                }
            }
        } while (false);

        rwLock.writeLock().lock();
        servers = newServers;
        rwLock.writeLock().unlock();
        
        return changes;
    }

    public DBServer getDbServer(int access, String pattern) {
        LinkedList<DBServer> candidate = new LinkedList<DBServer>();
        int totalWeight = 0;
        
        rwLock.readLock().lock();
        for (DBServer entry : servers) {
            if ((entry.access & access) == 0) {
                continue;
            }
            candidate.add(entry);
            totalWeight += entry.weight;
        }
        rwLock.readLock().unlock();
        
        // TODO: need better weight alg
        int randWeight = random.nextInt(totalWeight);
        DBServer choosen = null;
        for (DBServer dbServer : candidate) {
            randWeight -= dbServer.weight;
            if (randWeight <= 0) {
                choosen = dbServer;
                break;
            }
        }
        return choosen;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub

    }

}
