package net.sinofool.dbpool;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import net.sinofool.dbpool.config.DBConfig;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBPool implements ConfigProvider {

    private static final Logger logger = LoggerFactory.getLogger(DBConfig.class);

    public static final int READ_ACCESS = (1 << 0);
    public static final int WRITE_ACCESS = (1 << 1);

    private ConfigProvider provider;

    @Override
    public void initialize(final Configuration dummy) throws Exception {
        PropertiesConfiguration props = new PropertiesConfiguration("dbpool.properties");
        String providerClazz = props.getString("dbpool.config.provider", "net.sinofool.dbpool.ZeroCIceProvider");
        logger.info("Initializing DBPool with provider: " + providerClazz);
        provider = (ConfigProvider) Class.forName(providerClazz).newInstance();
        provider.initialize(props);
    }

    @Override
    public void close() {
        provider.close();
    }

    @Override
    public DataSource getDataSource(final String instance, final int access, final String pattern) {
        return provider.getDataSource(instance, access, pattern);
    }

    public static void main(String[] args) throws Exception {
        DBPool pool = new DBPool();
        pool.initialize(null);

        for (int i = 0; i < Integer.MAX_VALUE; ++i) {
            DataSource ds = pool.getDataSource("user", READ_ACCESS, "user123*");
            QueryRunner run = new QueryRunner(ds);
            String ret = run.query("SELECT user()", new ResultSetHandler<String>() {

                @Override
                public String handle(ResultSet rs) throws SQLException {
                    if (rs.next()) {
                        return rs.getString(1);
                    }
                    return "Empty";
                }
            });
            System.out.println(ret);
            Thread.sleep(200L);
        }
        pool.close();
    }
}
