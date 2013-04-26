package net.sinofool.dbpool.config;

import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;

public class DBSource {

	// use ConcurrentHashMap to make the methods are threadsafe
    private ConcurrentHashMap<Integer, BasicDataSource> datasources = new ConcurrentHashMap<Integer, BasicDataSource>();

    public DataSource getDataSource(DBServer server) {
        BasicDataSource ds = datasources.get(server.checksum());
        if (ds != null) {
            return ds;
        }
        ds = new BasicDataSource();
        ds.setDriverClassName(server.driver);
        ds.setUrl("jdbc:mysql://" + server.host + ":" + server.port + "/" + server.db + "?autoReconnect=true");
        ds.setUsername(server.user);
        ds.setPassword(server.pass);
        ds.setInitialSize(server.coreSize);
        ds.setMinIdle(server.coreSize);
        ds.setMaxActive(server.maxSize);
        ds.setMinEvictableIdleTimeMillis(server.idleTimeSeconds * 1000L / 2);

        datasources.put(server.checksum(), ds);
        return ds;
    }

    public void closeDataSource(DBServer server) {
        BasicDataSource ds = datasources.remove(server.checksum());
        if (ds == null) {
            // TODO this should never happened.
            return;
        }
        try {
            ds.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub

    }

}
