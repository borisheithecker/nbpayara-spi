/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nbpayara.spi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;
import org.nbpayara.core.DomainInfo;
import org.nbpayara.spi.util.CollectionUtil;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 *
 * @author boris.heithecker
 */
public interface RemoteLookup {

    public Object lookup(String name);

    public <T> T lookup(Class<T> clz);

    public <T> Collection<T> lookupAll(Class<T> clz);

    public DomainInfo getProviderInfo();

    public RequestProcessor getRequestProcessor();

    public void login() throws SecurityException;

    public static RemoteLookup get(String url) {

        return Lookup.getDefault().lookupAll(RemoteLookup.class).stream()
                .filter(l -> l.getProviderInfo().getURL().equals(url))
                .map(RemoteLookup.class::cast)
                .collect(CollectionUtil.singleOrNull());
    }

    public @interface Registrations {

        public Registration[] value();
    }

    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.TYPE})
    public static @interface Registration {

        public String name();

        public Class<?> beanInterface();

        public String module() default "";
    }
}
