package net.sinofool.dbpool;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sinofool.dbpool.config.DBConfig;
import net.sinofool.dbpool.config.DBServer;
import net.sinofool.dbpool.config.DBSource;
import net.sinofool.dbpool.idl.DBPoolClientPrx;
import net.sinofool.dbpool.idl.DBPoolClientPrxHelper;
import net.sinofool.dbpool.idl.DBPoolServerPrx;
import net.sinofool.dbpool.idl.DBPoolServerPrxHelper;
import net.sinofool.dbpool.idl._DBPoolClientDisp;

public class ZeroCIceProvider implements ConfigProvider {

    private static final Logger logger = LoggerFactory.getLogger(ZeroCIceProvider.class);

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

    private DBConfig dbconfig = new DBConfig();
    private DBSource dbsource = new DBSource();

    @Override
    public DataSource getDataSource(String instance, int access, String pattern) {
        DBServer ds = dbconfig.getDBServer(instance, access, pattern);
        if (ds != null) {
            return dbsource.getDataSource(ds);
        }
        return null;
    }

    private Ice.Communicator ic;
    private Ice.ObjectAdapter adapter;
    private DBPoolClientPrx clientPrx;
    private DBPoolServerPrx serverPrx;

    private Timer keepAliveTimer = new Timer();

    /*
     * @param proxy: format like "M:default -h 127.0.0.1 -p 10000"
     */
    @Override
    public void initialize(final Properties props) {
        Ice.InitializationData initData = new Ice.InitializationData();
        initData.properties = Ice.Util.createProperties();
        initData.properties.setProperty("Ice.IPv6", "0");

        ic = Ice.Util.initialize(initData);
        adapter = ic.createObjectAdapterWithEndpoints("DBPoolClient", "default");
        clientPrx = DBPoolClientPrxHelper.uncheckedCast(adapter.add(new DBClient(), ic.stringToIdentity("C")));
        adapter.activate();

        String proxy = props.getProperty("dbpool.provider.zerocice.proxy", "M:default -h 127.0.0.1 -p 10000");
        logger.info("Connecting to " + proxy);

        // set timeout 1000, so if the trans data is big we can still get it
        serverPrx = DBPoolServerPrxHelper.uncheckedCast(ic.stringToProxy(proxy).ice_timeout(1000));
        dbconfig.reloadConfig(serverPrx.getDBInstanceDict());
        serverPrx.registerClient(clientPrx);

        keepAliveTimer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                try {
                    // also get config after register to make sure client can
                    // get
                    // the change in the period of client lost connection with
                    // server
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

    @Override
    public void close() {
        ic.shutdown();
        ic.destroy();
    }

}
