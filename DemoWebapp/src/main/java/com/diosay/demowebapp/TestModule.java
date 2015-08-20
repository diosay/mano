/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.diosay.demowebapp;

import mano.web.ActionResult;
import mano.web.Module;

/**
 *
 * @author jun
 */
public class TestModule extends Module {
    void index(){
        
        this.context().getResponse().write("hello word");
        
    }
    
    ActionResult testview(){
        return view();
    }
}
