package net.sinofool.dbpool.config;

public class DBServer {

    String driver;
    String host;
    String port;
    String db;
    String user;
    String pass;

    int coreSize;
    int maxSize;
    int idleTimeSeconds;

    String expression;
    int weight;
    int access;

    public String checksum() {
        StringBuffer buff = new StringBuffer();
        buff.append(driver);
        buff.append(host);
        buff.append(port);
        buff.append(db);
        buff.append(user);
        buff.append(pass);
        buff.append(coreSize);
        buff.append(maxSize);
        buff.append(idleTimeSeconds);
        buff.append(expression);
        buff.append(weight);
        buff.append(access);

        return buff.toString();
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub

    }

}
