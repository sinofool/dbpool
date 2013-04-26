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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBPool {

	private static final Logger logger = LoggerFactory.getLogger(DBConfig.class);
	
    public static int READ_ACCESS = (1 << 0);
    public static int WRITE_ACCESS = (1 << 1);

    @SuppressWarnings("serial")
    private class DBClient extends _DBPoolClientDisp {

        @Override
        public boolean pushDBInstanceDict(Map<String, net.sinofool.dbpool.idl.DBServer[]> dict, Ice.Current __current) {
        	logger.info("pushDBInstanceDict start");
            List<DBServer> changes = dbconfig.reloadConfig(dict);
            for (DBServer change : changes) {
                dbsource.closeDataSource(change);
            }
            logger.info("pushDBInstanceDict finish");
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
    
    /*
     * @param proxy: format like "M:default -h 127.0.0.1 -p 10000"
     */
    public void initialize(String proxy) {
        ic = Ice.Util.initialize();
        adapter = ic.createObjectAdapterWithEndpoints("DBPoolClient", "default");
        clientPrx = DBPoolClientPrxHelper.uncheckedCast(adapter.add(new DBClient(), ic.stringToIdentity("C")));
        adapter.activate();
 
        // set timeout 1000, so if the trans data is big we can still get it
        serverPrx = DBPoolServerPrxHelper.uncheckedCast(ic.stringToProxy(proxy).ice_timeout(1000));
        dbconfig.reloadConfig(serverPrx.getDBInstanceDict());
        serverPrx.registerClient(clientPrx);

        keepAliveTimer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                try {
                	//also get config after register to make sure client can get 
                	//the change in the period of client lost connection with server
                	logger.debug("Run communicate with server start");
                    serverPrx.registerClient(clientPrx);
                    dbconfig.reloadConfig(serverPrx.getDBInstanceDict());
                    logger.debug("Run communicate with server finish");
                } catch (Throwable e) {
                    logger.warn("Run communicate with server get exception " + e);
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
        if( ds != null) {
        	return dbsource.getDataSource(ds);	
        }
        return null;        
    }

    public static void main(String[] args) throws SQLException, InterruptedException {
    	String proxyStr = null;
    	if(args.length == 0) {
    		System.out.println("Use default proxy \"M:default -h 127.0.0.1 -p 10000\"," +
    				" or you can give one in cmdline !");
    		proxyStr = "M:default -h 127.0.0.1 -p 10000";
    	}else {
    		proxyStr = args[0];
    	}
        DBPool pool = new DBPool();
        pool.initialize(proxyStr);

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
