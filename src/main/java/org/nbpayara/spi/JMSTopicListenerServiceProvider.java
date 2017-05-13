/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nbpayara.spi;

import java.util.List;
import org.nbpayara.core.DomainInfo;
import org.nbpayara.spi.util.CollectionUtil;
import org.openide.util.Lookup;

/**
 *
 * @author boris.heithecker
 */
public interface JMSTopicListenerServiceProvider {

    public List<JMSTopicListenerService> getListenerServices();

    public JMSTopicListenerService getListenerService(String topic);

    public DomainInfo getInfo();

    public static JMSTopicListenerServiceProvider find(String id) {
        return Lookup.getDefault().lookupAll(JMSTopicListenerServiceProvider.class).stream()
                .filter(lsp -> lsp.getInfo().getURL().equals(id))
                .collect(CollectionUtil.singleOrNull());
    }

}
