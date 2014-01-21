package net.sinofool.dbpool.config;

public class DBServer {

    public String driver;
    public String host;
    public String port;
    public String db;
    public String user;
    public String pass;

    public int coreSize;
    public int maxSize;
    public int idleTimeSeconds;

    public String expression;
    public int weight;
    public int access;

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
}
