package net.sinofool.dbpool;

import javax.sql.DataSource;

import org.apache.commons.configuration.Configuration;

public interface ConfigProvider {
    void initialize(final Configuration props) throws Exception;

    void close();

    DataSource getDataSource(String instance, int access, String pattern);
}
