/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nbpayara.spi;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.openide.util.RequestProcessor;

/**
 *
 * @author boris.heithecker
 */
public class JMSTopicListenerService implements Runnable {

    public final RequestProcessor RP = new RequestProcessor(JMSTopicListenerService.class.getName(), 4);
    private final String topic;
    private final String connectionFactory;
    private final String host;
    private final String port;
    private MessageListenerImpl listener;
    private final Map<Class, Set<JMSListener>> listeners = new HashMap<>();

    private JMSTopicListenerService(String host, String port, String connectionFactory, String topic) {
        this.host = host;
        this.port = port;
        this.connectionFactory = connectionFactory;
        this.topic = topic;
//        this.iiopEndpoint = iiopEndpoints;
//        RP.post(this::initialize);
    }

    public JMSTopicListenerService(String connectionFactory, String topic) {
        this(null, null, connectionFactory, topic);
    }

    public String getTopics() {
        return topic;
    }

    public <E extends JMSEvent> void registerListener(final Class<E> clz, final JMSListener<E> l) {
        synchronized (listeners) {
            listeners.computeIfAbsent(clz, k -> new HashSet<>())
                    .add(l);
        }
    }

    public <E extends JMSEvent> void unregisterListener(final JMSListener<E> l) {
        synchronized (listeners) {
            listeners.values().stream()
                    .forEach(s -> s.remove(l));
        }
    }

    public void run(final JMSEvent evt) {
        if (listener != null && evt != null) {
            listeners.entrySet().stream()
                    .filter(e -> e.getKey().isAssignableFrom(evt.getClass()))
                    .flatMap(e -> e.getValue().stream())
                    .forEach(l -> l.onMessage(evt));
        }
    }

    public void initialize() {
        RP.post(this);
    }

    @Override
    public void run() {
        if (listener != null || !RP.isRequestProcessorThread()) {
            return;
        }
        try {
            Properties prop = new Properties();
            if (host != null && port != null) {
                prop.put("imqAddressList", "mq://" + host + ":" + port);
            }
            InitialContext ctx = new InitialContext(prop);
            TopicConnectionFactory qFactory = (TopicConnectionFactory) ctx.lookup(connectionFactory);
            Connection connection = qFactory.createConnection();
            TopicSession session = (TopicSession) connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Topic queue = (Topic) ctx.lookup(topic);
            MessageConsumer mc = session.createSubscriber(queue);
            connection.start();
            final MessageListenerImpl l = new MessageListenerImpl(this);
            mc.setMessageListener(l);
            listener = l;
        } catch (NamingException | JMSException ex) {
            listener = null;
            Logger.getLogger(JMSTopicListenerService.class.getName()).log(Level.WARNING, ex.getMessage());
        }

    }
}
