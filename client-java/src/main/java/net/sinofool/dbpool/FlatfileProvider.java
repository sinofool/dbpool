package net.sinofool.dbpool;

import java.security.InvalidParameterException;

import javax.sql.DataSource;

import net.sinofool.dbpool.config.DBServer;
import net.sinofool.dbpool.config.DBSource;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlatfileProvider implements ConfigProvider {

    private static final Logger logger = LoggerFactory.getLogger(FlatfileProvider.class);

    private DBServer read;
    private DBServer write;

    private DBSource source;

    @Override
    public void initialize(final Configuration props) throws Exception {
        read = loadConfig(props, "dbppool.provider.flatfile.read", DBPool.READ_ACCESS, "", 100);
        write = loadConfig(props, "dbppool.provider.flatfile.write", DBPool.WRITE_ACCESS, "", 100);
        source = new DBSource();
    }

    private DBServer loadConfig(final Configuration props, String prefix, int access, String expression, int weight) {
        DBServer server = new DBServer();
        server.driver = props.getString(prefix + ".driver", "mysql");
        server.host = props.getString(prefix + ".host");
        server.port = props.getString(prefix + ".port", "3306");
        server.user = props.getString(prefix + ".user");
        server.pass = props.getString(prefix + ".pass");
        server.db = props.getString(prefix + ".db");
        server.coreSize = props.getInt(prefix + ".coresize", 1);
        server.maxSize = props.getInt(prefix + ".maxsize", 10);
        server.idleTimeSeconds = props.getInt(prefix + ".idletimeseconds", 60);
        server.access = access;
        server.expression = expression;
        server.weight = weight;
        return server;
    }

    @Override
    public void close() {
        source.closeDataSource(read);
        source.closeDataSource(write);
    }

    @Override
    public DataSource getDataSource(String instance, int access, String pattern) {
        if (!instance.isEmpty() && !pattern.isEmpty()) {
            throw new InvalidParameterException("FlatfileProvider support only empty instance name and pattern name.");
        }
        if (access == DBPool.READ_ACCESS) {
            return source.getDataSource(read);
        } else if (access == DBPool.WRITE_ACCESS) {
            return source.getDataSource(write);
        } else {
            logger.error("FlatfileProvider is outdated with DBPool");
            throw new InvalidParameterException("FlatfileProvider is outdated. This should never happen.");
        }
    }

}
