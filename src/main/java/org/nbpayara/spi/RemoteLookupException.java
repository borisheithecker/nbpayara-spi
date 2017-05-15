/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nbpayara.spi;

import org.openide.util.NbBundle;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages({"RemoteLookupException.message=An error occurred looking up {0} at {1}. Cause is {3}: {4}"})
public class RemoteLookupException extends RuntimeException {

    private final String ejb;
    private final String endpoints;

    public RemoteLookupException(String endpoints, String ejbName, Throwable cause) {
        super(cause);
        this.ejb = ejbName;
        this.endpoints = endpoints;
    }

    @Override
    public String getMessage() {
        return NbBundle.getMessage(RemoteLookupException.class, "RemoteLookupException.error.message", ejb, endpoints, getCause().getClass().getName(), getCause().getMessage());
    }

}
