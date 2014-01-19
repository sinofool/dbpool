package net.sinofool.dbpool;

import java.util.Properties;

import javax.sql.DataSource;

import net.sinofool.dbpool.config.DBServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlatfileProvider implements ConfigProvider {

    private static final Logger logger = LoggerFactory.getLogger(FlatfileProvider.class);

    private DBServer read;
    private DBServer write;

    @Override
    public void initialize(Properties props) throws Exception {
        String readDriver = props.getProperty("dbpool.provider.flatfile.read.driver", "mysql");
        String readHost = props.getProperty("dbppool.provider.flatfile.read.host");
        String readPort = props.getProperty("dbppool.provider.flatfile.read.port", "3306");
        String readUser = props.getProperty("dbppool.provider.flatfile.read.user");
        String readPass = props.getProperty("dbppool.provider.flatfile.read.pass");

        read = new DBServer();

        String writeDriver = props.getProperty("dbpool.provider.flatfile.write.driver", "mysql");
        String writeHost = props.getProperty("dbppool.provider.flatfile.write.host");
        String writePort = props.getProperty("dbppool.provider.flatfile.write.port", "3306");
        String writeUser = props.getProperty("dbppool.provider.flatfile.write.user");
        String writePass = props.getProperty("dbppool.provider.flatfile.write.pass");

    }

    @Override
    public void close() {
        // TODO Auto-generated method stub

    }

    @Override
    public DataSource getDataSource(String instance, int access, String pattern) {
        // TODO Auto-generated method stub
        return null;
    }

}
