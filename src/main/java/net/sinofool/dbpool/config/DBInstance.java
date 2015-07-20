package net.sinofool.dbpool.config;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Pattern;

import net.sinofool.dbpool.DBPool;

public class DBInstance {

    private Random random = new Random(System.currentTimeMillis());

    private ArrayList<DBServer> servers = new ArrayList<DBServer>();
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

    public List<DBServer> reloadConfig(net.sinofool.dbpool.idl.DBServer[] sers) {
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
        rwLock.writeLock().lock();
        try {
            if (servers.size() != newServers.size()) {
                changes = servers;
                return changes;
            }
            changes = new ArrayList<DBServer>();
            for (int i = 0; i < servers.size(); ++i) {
                String oldChecksum = servers.get(i).checksum();
                String newChecksum = newServers.get(i).checksum();
                if (!oldChecksum.equals(newChecksum)) {
                    changes.add(servers.get(i));
                }
            }
        } finally {
            servers = newServers;
            rwLock.writeLock().unlock();
        }
        return changes;
    }

    private int getDBServerList(int access, List<DBServer> candidate) {
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

        return totalWeight;
    }

    private int getDBServerList(int access, String pattern, List<DBServer> candidate) {
        int totalWeight = 0;
        Pattern objPattern = Pattern.compile(pattern);
        rwLock.readLock().lock();
        for (DBServer entry : servers) {
            if ((entry.access & access) == 0) {
                continue;
            }
            if (!objPattern.matcher(entry.expression).find()) {
                continue;
            }
            candidate.add(entry);
            totalWeight += entry.weight;
        }
        rwLock.readLock().unlock();

        return totalWeight;
    }

    public DBServer getDBServer(int access, String pattern) {
        LinkedList<DBServer> candidate = new LinkedList<DBServer>();
        int totalWeight = 0;

        if (pattern != null && !pattern.isEmpty()) {
            totalWeight = getDBServerList(access, pattern, candidate);
        } else {
            totalWeight = getDBServerList(access, candidate);
        }

        if (totalWeight == 0) {
            return null;
        }

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
}
