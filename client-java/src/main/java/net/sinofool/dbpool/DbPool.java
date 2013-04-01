package net.sinofool.dbpool;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.sql.DataSource;

import net.sinofool.dbpool.config.DBConfig;
import net.sinofool.dbpool.config.DBServer;
import net.sinofool.dbpool.config.DBSource;
import net.sinofool.dbpool.idl.DBPoolClientPrx;
import net.sinofool.dbpool.idl.DBPoolClientPrxHelper;
import net.sinofool.dbpool.idl.DBPoolServerPrx;
import net.sinofool.dbpool.idl.DBPoolServerPrxHelper;
import net.sinofool.dbpool.idl._DBPoolClientDisp;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;

public class DBPool {

    public static int READ_ACCESS = (1 << 0);
    public static int WRITE_ACCESS = (1 << 1);

    @SuppressWarnings("serial")
    private class DBClient extends _DBPoolClientDisp {

        @Override
        public boolean pushDBInstanceDict(Map<String, net.sinofool.dbpool.idl.DBServer[]> dict, Ice.Current __current) {
            List<DBServer> changes = dbconfig.reloadConfig(dict);
            for (DBServer change : changes) {
                dbsource.closeDataSource(change);
            }
            return true;
        }
    }

    private DBSource dbsource = new DBSource();
    private DBConfig dbconfig = new DBConfig();
    private Ice.Communicator ic;
    private Ice.ObjectAdapter adapter;
    private DBPoolClientPrx clientPrx;
    private DBPoolServerPrx serverPrx;

    private Timer keepAliveTimer = new Timer();

    public void initialize() {
        ic = Ice.Util.initialize();
        adapter = ic.createObjectAdapterWithEndpoints("DBPoolClient", "default");
        clientPrx = DBPoolClientPrxHelper.uncheckedCast(adapter.add(new DBClient(), ic.stringToIdentity("C")));
        adapter.activate();
        serverPrx = DBPoolServerPrxHelper.uncheckedCast(ic.stringToProxy("M:default -h 127.0.0.1 -p 10000"));
        dbconfig.reloadConfig(serverPrx.getDBInstanceDict());
        serverPrx.registerClient(clientPrx);

        keepAliveTimer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                try {
                    serverPrx.registerClient(clientPrx);
                } catch (Throwable e) {
                    // TODO log it.
                }
            }
        }, 15 * 1000L, 15 * 1000L);
    }

    public void close() {
        ic.shutdown();
        ic.destroy();
    }

    public DataSource getDataSource(String instance, int access, String pattern) {
        DBServer ds = dbconfig.getDBServer(instance, access, pattern);
        return dbsource.getDataSource(ds);
    }

    public static void main(String[] args) throws SQLException, InterruptedException {
        DBPool pool = new DBPool();
        pool.initialize();

        for (int i = 0; i < Integer.MAX_VALUE; ++i) {
            DataSource ds = pool.getDataSource("user", READ_ACCESS, "");
            QueryRunner run = new QueryRunner(ds);
            String ret = run.query("SELECT user()", new ResultSetHandler<String>() {

                @Override
                public String handle(ResultSet rs) throws SQLException {
                    while (rs.next()) {
                        return (rs.getString(1));
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
