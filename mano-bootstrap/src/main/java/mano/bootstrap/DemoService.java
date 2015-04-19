/*
 * Copyright (C) 2014-2015 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.bootstrap;

import mano.service.Service;

/**
 *
 * @author johnwhang
 */
public class DemoService extends Service {

    private String serviceName;

    @Override
    public String getServiceName() {
        return serviceName;
    }

    @Override
    protected void onInit() throws Exception {
        serviceName = this.getContext().getProperty("service.name");
    }

    @Override
    protected void onStart() throws Exception {
        System.out.println("hello ");
        Thread.sleep(1000);
        //this.stop();
    }

    @Override
    protected void onStop() throws Exception {
    }
    
}
