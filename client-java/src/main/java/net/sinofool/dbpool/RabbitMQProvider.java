package net.sinofool.dbpool;

import javax.sql.DataSource;
import org.apache.commons.configuration.Configuration;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

public class RabbitMQProvider implements ConfigProvider
{
    private class RabbitMQListener implements Runnable
    {
        @Override
        public void run()
        {
            Connection connection = null;
            Channel channel = null;
            try
            {
                ConnectionFactory factory = new ConnectionFactory();
                factory.setHost(host);
                factory.setVirtualHost(vhost);
                factory.setUsername(user);
                factory.setPassword(password);

                connection = factory.newConnection();
                channel = connection.createChannel();

                channel.queueDeclare(queue, false, false, false, null);

                channel.basicQos(1);

                QueueingConsumer consumer = new QueueingConsumer(channel);
                channel.basicConsume(queue, false, consumer);

                System.out.println(" [x] Awaiting RPC requests");

                while (true)
                {
                    String response = null;

                    QueueingConsumer.Delivery delivery = consumer.nextDelivery();

                    BasicProperties props = delivery.getProperties();
                    BasicProperties replyProps = new BasicProperties.Builder()
                                    .correlationId(props.getCorrelationId()).build();

                    try
                    {
                        String message = new String(delivery.getBody(), "UTF-8");
                        response = update(message);
                    }
                    catch (Exception e)
                    {
                        System.out.println(" [.] " + e.toString());
                        response = "";
                    }
                    finally
                    {
                        channel.basicPublish("", props.getReplyTo(), replyProps,
                            response.getBytes("UTF-8"));

                        channel.basicAck(delivery.getEnvelope().getDeliveryTag(),
                            false);
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                if (connection != null)
                {
                    try
                    {
                        connection.close();
                    }
                    catch (Exception ignore)
                    {
                    }
                }
            }
        }

    }

    private String host;
    private String vhost;
    private String user;
    private String password;
    private String queue;
    private ConfigProvider persistency;
    private Thread watchdog;

    private String update(String message)
    {
        return message;
    }

    @Override
    public void initialize(Configuration props) throws Exception
    {
        this.host = props.getString("dbpool.provider.rabbitmq.host");
        this.vhost = props.getString("dbpool.provider.rabbitmq.vhost");
        this.user = props.getString("dbpool.provider.rabbitmq.user");
        this.password = props.getString("dbpool.provider.rabbitmq.password");
        this.queue = props.getString("dbpool.provider.rabbitmq.queue");
        if (props.containsKey("dbpool.provider.rabbitmq.persistency"))
        {
            this.persistency =
                            (ConfigProvider) Class.forName(props.getString("dbpool.provider.rabbitmq.persistency")).newInstance();
        }
        this.watchdog = new Thread(new RabbitMQListener());
        this.watchdog.setDaemon(true);
        this.watchdog.start();
    }

    @Override
    public void close()
    {
        this.watchdog.interrupt();
    }

    @Override
    public DataSource getDataSource(String instance, int access, String pattern)
    {
        // TODO Auto-generated method stub
        return null;
    }

}
