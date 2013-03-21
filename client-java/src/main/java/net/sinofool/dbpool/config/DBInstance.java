package net.sinofool.dbpool.config;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

import net.sinofool.dbpool.DbPool;


public class DBInstance {

    private Random random = new Random(System.currentTimeMillis());

    private ArrayList<DBServer> servers = new ArrayList<DBServer>();

    private int access(String acc) {
        // TODO: needs more
        if (acc.equalsIgnoreCase("rw")) {
            return DbPool.WRITE_ACCESS | DbPool.READ_ACCESS;
        }
        if (acc.equalsIgnoreCase("r")) {
            return DbPool.READ_ACCESS;
        }
        if (acc.equalsIgnoreCase("w")) {
            return DbPool.WRITE_ACCESS;
        }
        return 0;
    }

    public synchronized boolean reloadConfig(net.sinofool.dbpool.idl.DBServer[] sers) {
        ArrayList<DBServer> newServers = new ArrayList<DBServer>();
        for (net.sinofool.dbpool.idl.DBServer ser : sers) {
            DBServer dbs = new DBServer();
            dbs.driver = ser.type;
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
        servers = newServers;
        return true;
    }

    public DBServer getDbServer(int access, String pattern) {
        LinkedList<DBServer> candidate = new LinkedList<DBServer>();
        int totalWeight = 0;
        for (DBServer entry : servers) {
            if ((entry.access | access) == 0) {
                continue;
            }
            candidate.add(entry);
            totalWeight += entry.weight;
        }
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
