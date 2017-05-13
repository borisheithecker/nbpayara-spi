/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nbpayara.spi.impl;

import java.util.Map;
import org.openide.util.Lookup;

/**
 *
 * @author boris.heithecker
 */
public class RemoteReference<T> {

    private final String lookup;
    private final Class<T> type;
    private final String module;

    private RemoteReference(String lookup, Class<T> type, String module) {
        this.lookup = lookup;
        this.type = type;
        this.module = module;
    }

    public RemoteReference(String lookup, Class<T> type) {
        this(lookup, type, null);
    }

    public String getLookup() {
        return lookup;
    }

    public Class<T> getType() {
        return type;
    }

    public String getModule() {
        return module;
    }
    
    public static <T> RemoteReference<T> create(Map<String, ?> params) {
        String name = (String) params.get("ejbName");
        String module = (String) params.get("module");
        String type = (String) params.get("beanInterface");
        try {
            final ClassLoader sysCl = Lookup.getDefault().lookup(ClassLoader.class);      
            Class<?> clz =  Class.forName(type, true, sysCl);
            return new RemoteReference(name, clz, module);
        } catch (ClassNotFoundException ex) {
        }
        return null;
    }
}
