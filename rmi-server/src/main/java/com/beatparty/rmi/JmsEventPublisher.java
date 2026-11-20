package com.beatparty.rmi;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.HashMap;
import java.util.Map;

public class JmsEventPublisher {

    private final ObjectMapper mapper = new ObjectMapper();
    private Connection connection;
    private Session session;
    private MessageProducer queueProducer;
    private MessageProducer hypeProducer;
    private boolean connected = false;

    public void init() {
        try {
            Context ctx = new InitialContext(); // Reads jndi.properties from classpath
            ConnectionFactory factory = (ConnectionFactory) ctx.lookup("ConnectionFactory");
            Topic queueTopic = (Topic) ctx.lookup("queueEvents");
            Topic hypeTopic = (Topic) ctx.lookup("hypeEvents");

            connection = factory.createConnection();
            connection.start();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            queueProducer = session.createProducer(queueTopic);
            queueProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

            hypeProducer = session.createProducer(hypeTopic);
            hypeProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

            connected = true;
            System.out.println("[JMS/JNDI] Connected to ActiveMQ topics via InitialContext.");
        } catch (Exception e) {
            connected = false;
            System.err.println("[JMS/JNDI] Warning: Could not connect to ActiveMQ (" + e.getMessage() + "). RMI will still run without event broadcast.");
        }
    }

    public synchronized void publishQueueEvent(String eventType, String clubId, Object payload) {
        publish(queueProducer, eventType, clubId, payload);
    }

    public synchronized void publishHypeEvent(String eventType, String clubId, Object payload) {
        publish(hypeProducer, eventType, clubId, payload);
    }

    private void publish(MessageProducer producer, String eventType, String clubId, Object payload) {
        if (!connected || producer == null) {
            return;
        }
        try {
            Map<String, Object> envelope = new HashMap<>();
            envelope.put("eventType", eventType);
            envelope.put("clubId", clubId);
            envelope.put("payload", payload);
            envelope.put("timestamp", System.currentTimeMillis());

            String json = mapper.writeValueAsString(envelope);
            TextMessage msg = session.createTextMessage(json);
            msg.setStringProperty("clubId", clubId);
            msg.setStringProperty("eventType", eventType);
            producer.send(msg);
        } catch (Exception e) {
            System.err.println("[JMS] Failed to publish event " + eventType + ": " + e.getMessage());
        }
    }

    public void close() {
        try {
            if (queueProducer != null) queueProducer.close();
            if (hypeProducer != null) hypeProducer.close();
            if (session != null) session.close();
            if (connection != null) connection.close();
        } catch (JMSException e) {
            System.err.println("[JMS] Error closing resources: " + e.getMessage());
        }
    }
}