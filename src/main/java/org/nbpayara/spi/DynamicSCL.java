/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nbpayara.spi;

import java.lang.reflect.Field;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 *
 * @author boris.heithecker
 */
final class DynamicSCL extends ClassLoader implements LookupListener {

    private final Lookup.Result<ClassLoader> sclResult;

    @SuppressWarnings("LeakingThisInConstructor")
    DynamicSCL() {
        super(null);
        sclResult = Lookup.getDefault().lookupResult(ClassLoader.class);
        sclResult.addLookupListener(this);
        resultChanged(null);
    }

    @Override
    public final void resultChanged(LookupEvent ev) {
        final ClassLoader lkpResCL = sclResult.allInstances().iterator().next();
        synchronized (this) {
            if (getParent() != lkpResCL) {
                try {
                    Field f = ClassLoader.class.getDeclaredField("parent");
                    f.setAccessible(true);
                    f.set(this, lkpResCL);
                } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }
}
