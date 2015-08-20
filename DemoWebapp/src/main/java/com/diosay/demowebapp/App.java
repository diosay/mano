/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.diosay.demowebapp;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import mano.web.WebApplication;

/**
 *
 * @author jun
 */
public class App extends WebApplication {
    @Override
    protected void onInit() {
        this.regisiterHandlers(HomeController.class);
    }
    
    @Override
    public String[] getActionNamespaces(){
        return new String[]{"com.diosay.demowebapp"};
    }
    
}
