package net.sinofool.dbpool;

import java.util.Properties;

import javax.sql.DataSource;

public interface ConfigProvider {
	public void initialize(final Properties props) throws Exception;

	public void close();
	
	public DataSource getDataSource(String instance, int access, String pattern);
}
