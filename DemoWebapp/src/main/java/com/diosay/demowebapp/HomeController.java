/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.diosay.demowebapp;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;
import mano.web.Controller;
import mano.web.UrlMapping;
import mano.net.http.HttpMethod;
import mano.net.http.HttpPostFile;

/**
 *
 * @author jun
 */
@UrlMapping("/home")
public class HomeController extends Controller {

    @UrlMapping(value = "/index")
    void index() throws IOException {

        if (this.getContext().getRequest().getMethod().equals(HttpMethod.POST)) {
//            this.getContext().getRequest().loadEntityBody();
//            //ByteArrayInputStream is=((ByteArrayInputStream)this.getContext().getRequest().entityBody());
//            java.io.BufferedReader reader=new java.io.BufferedReader(new java.io.InputStreamReader(this.getContext().getRequest().getEntityBodyStream()));
//            //byte[] arr=new byte[is.available()];
//            //int c=is.read(arr);
//            this.text(reader.readLine());
//            //

            for (Map.Entry<String, String> e : this.getContext().getRequest().form().entrySet()) {
                text("key:" + e.getKey() + " = " + e.getValue() + "<br>");
            }
            HttpPostFile f=this.file("file");
            if(f!=null){
                f.savaAs("D:\\tmp\\"+f.getName()+""+f.getExtension());
                text("file:"+f.getOriginal());
            }
            

        } else {
            this.view();
        }
    }
}
