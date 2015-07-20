package net.sinofool.dbpool;

import net.sinofool.dbpool.idl.DBPoolServerPrx;
import net.sinofool.dbpool.idl.DBPoolServerPrxHelper;

public class Reload {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Example: java Reload M:default -h 127.0.0.1 -p 10000");
            System.exit(1);
        }
        
        StringBuffer str = new StringBuffer();
        for (String arg : args) {
            str.append(arg).append(" ");
        }

        Ice.Communicator ic = Ice.Util.initialize();
        DBPoolServerPrx serverPrx = DBPoolServerPrxHelper.uncheckedCast(ic.stringToProxy(str.toString()));
        System.out.println("reload start");
        serverPrx.reload();
        System.out.println("reload done");
        ic.shutdown();
        ic.destroy();
    }
}
