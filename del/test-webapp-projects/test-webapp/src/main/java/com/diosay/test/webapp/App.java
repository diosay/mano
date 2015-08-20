/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.diosay.test.webapp;

import mano.web.WebApplication;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class App extends WebApplication {

    @Override
    protected void onDestory() {
        super.onDestory();
    }

    /**
     * 用于DEBUG的启动方法。
     * @param vargs 
     */
    public static void main(String[] vargs) {
        ServerStartupArgs args = createStartupArgs();
        args.libDirectory="E:\\repositories\\java\\mano\\test-webapp-projects\\test-webapp\\target\\build\\lib";
        args.serverDirectory="E:\\repositories\\java\\mano\\mano-server-projects\\mano-server\\target\\build";
        args.webappDirectory="E:\\repositories\\java\\mano\\test-webapp-projects\\test-webapp\\src\\main\\webapp";
        startDebugServer(args);
    }

}
