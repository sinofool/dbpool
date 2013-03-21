package net.sinofool.dbpool;

import javax.sql.DataSource;

import net.sinofool.dbpool.config.DBConfig;
import net.sinofool.dbpool.config.DBServer;
import net.sinofool.dbpool.config.DBSource;

public class DbPool {

    public static int READ_ACCESS = (1 << 0);
    public static int WRITE_ACCESS = (1 << 1);

    private DBSource dbsource = new DBSource();
    private DBConfig dbconfig = new DBConfig();

    public DataSource getDataSource(String instance, int access, String pattern) {
        DBServer ds = dbconfig.getDBServer(instance, access, pattern);
        return dbsource.getDataSource(ds);
    }

    public static void main(String[] args) {
        DbPool pool = new DbPool();
        pool.dbconfig.reloadConfig(); // TOOD test only

        System.out.println(pool.getDataSource("user", READ_ACCESS, ""));
        System.exit(1);
    }
}
