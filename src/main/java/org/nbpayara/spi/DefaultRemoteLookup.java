/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nbpayara.spi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.apache.commons.lang3.StringUtils;
import org.nbpayara.core.Domain;
import org.nbpayara.spi.impl.RemoteReference;
import org.nbpayara.spi.util.CollectionUtil;
import org.nbpayara.spi.util.ContextCredentials;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author boris.heithecker
 */
public abstract class DefaultRemoteLookup implements RemoteLookup {

    private final String endpoints;
    private DynamicSCL dynamicSCL;
    private final String host;
    private final String port;
    private final RequestProcessor RP = new RequestProcessor(getClass().getCanonicalName(), 1);
    private final Set<String> modules = new HashSet<>();
    private ContextCredentials credentials;
    
    protected DefaultRemoteLookup(String initialHost, String initalPort) {
        this(initialHost, initalPort, null);
    }

    protected DefaultRemoteLookup(String initialHost, String initalPort, String endpoints) {
        this.host = initialHost;
        this.port = initalPort;
        this.endpoints = endpoints;
    }

    @Override
    public RequestProcessor getRequestProcessor() {
        return RP;
    }

    public Set<String> getModules() {
        return modules;
    }

    public ContextCredentials getContextCredentials() {
        return credentials;
    }

    protected void setContextCredentials(ContextCredentials creds) {
        credentials = creds;
    }

    @Override
    public Object lookup(String name) {
        final Properties props = new Properties();
        return doLookup(name, Object.class, props);
    }

    @Override
    public <T> T lookup(Class<T> clz) {
        return lookupAll(clz).stream()
                .collect(CollectionUtil.singleOrNull());
    }

    @Override
    public <T> Collection<T> lookupAll(final Class<T> clz) {
        final List<RemoteReference<T>> references = new ArrayList<>();
        Lookups.forPath("RemoteLookup").lookupAll(RemoteReference.class).stream()
                .map(RemoteReference.class::cast)
                .filter(rl -> rl.getType().equals(clz) && (StringUtils.isBlank(rl.getModule()) || modules.contains(rl.getModule())))
                .forEach(references::add);
        final Map<String, Class> beans = getBeans();
        if (beans != null) {
            beans.entrySet().stream()
                    .filter(e -> (clz.isAssignableFrom(e.getValue())))
                    .map(e -> new RemoteReference(e.getKey(), e.setValue(clz)))
                    .forEach(references::add);
        }
        final Properties props = new Properties();
        return references.stream()
                .map(ref -> doLookup(ref.getLookup(), ref.getType(), props))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    protected <T> T doLookup(final String name, final Class<T> clz, Properties props) {
        if (host != null) {
            props.setProperty("org.omg.CORBA.ORBInitialHost", host);
        }
        if (port != null) {
            props.setProperty("org.omg.CORBA.ORBInitialPort", port);
        }
        if (endpoints != null) {
            props.setProperty("com.sun.appserv.iiop.endpoints", endpoints);
        }
        props.setProperty("com.sun.corba.ee.transport.ORBTCPTimeouts", "2000:150000:100");
        ClassLoader lastCCL = Thread.currentThread().getContextClassLoader();
        T ret = null;
        try {
            checkRunning();
            InitialContext context = new InitialContext(props);
            Thread.currentThread().setContextClassLoader(getContextClassLoader());
            login();
            Object o = context.lookup(name);
            if (o != null) {
                ret = clz.cast(o);
            }
        } catch (NamingException | ClassCastException | SecurityException | IOException nex) {
            Logger.getLogger(DefaultRemoteLookup.class.getName()).log(Level.SEVERE, name, nex);
            throwRemoteLookupException(name, nex);
        } finally {
            if (lastCCL != null) {
                Thread.currentThread().setContextClassLoader(lastCCL);
            }
        }
        return ret;
    }

    protected void checkRunning() throws IOException {
        Domain.ensureRunning(getProviderInfo().getURL());
    }

    protected void throwRemoteLookupException(String name, final java.lang.Exception nex) throws RemoteLookupException {
        StringJoiner sj = new StringJoiner(",");
        if (endpoints != null) {
            sj.add(endpoints);
        }
        if (host != null) {
            String h = host;
            if (port != null) {
                h += ":" + port;
            }
            sj.add(h);
        }
        throw new RemoteLookupException(sj.toString(), name, nex);
    }

    protected ClassLoader getContextClassLoader() {
        if (dynamicSCL == null) {
            dynamicSCL = new DynamicSCL();
        }
        return dynamicSCL;
    }

    @Override
    public void login() throws SecurityException {
    }

    protected Map<String, Class> getBeans() {
        return null;
    }

    @Override
    public final boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DefaultRemoteLookup other = (DefaultRemoteLookup) obj;
        return Objects.equals(this.getProviderInfo(), other.getProviderInfo());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + Objects.hashCode(this.endpoints);
        hash = 23 * hash + Objects.hashCode(this.host);
        hash = 23 * hash + Objects.hashCode(this.port);
        return hash;
    }

}
