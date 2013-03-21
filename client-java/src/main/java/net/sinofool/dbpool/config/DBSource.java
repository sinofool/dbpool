package net.sinofool.dbpool.config;

import java.util.HashMap;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;

public class DBSource {

    private HashMap<DBServer, BasicDataSource> datasources = new HashMap<DBServer, BasicDataSource>();

    public DataSource getDataSource(DBServer server) {
        BasicDataSource ds = datasources.get(server);
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

        datasources.put(server, ds);
        return ds;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub

    }

}
