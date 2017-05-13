/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nbpayara.spi;

/**
 *
 * @author boris.heithecker
 * @param <E>
 */
public interface JMSListener<E extends JMSEvent> {

    default public void addNotify() {
    }

    default public void removeNotify() {
    }

    public void onMessage(E event);
}
