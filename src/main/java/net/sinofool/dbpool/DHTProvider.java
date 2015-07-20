package net.sinofool.dbpool;

import org.apache.commons.configuration.Configuration;

import javax.sql.DataSource;

/**
 * Created by Bochun on 2015-07-17.
 */
public class DHTProvider implements ConfigProvider {

    public void initialize(Configuration props) throws Exception {

    }

    public void close() {

    }

    public DataSource getDataSource(String instance, int access, String pattern) {
        return null;
    }
}
