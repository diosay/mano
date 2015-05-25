/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.diosay.demowebapp;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;
import mano.DateTime;
import mano.web.Controller;
import mano.web.UrlMapping;
import mano.net.http.HttpMethod;
import mano.net.http.HttpPostFile;
import mano.security.fliters.XSSHtmlFliter;
import mano.web.UrlCached;

/**
 *
 * @author jun
 */
@UrlMapping("/home")
public class HomeController extends Controller {

    @UrlCached
    @UrlMapping(value = "/index")
    void index() throws IOException, InterruptedException {

        if (this.getContext().getRequest().getMethod().equals(HttpMethod.POST)) {
//            this.getContext().getRequest().loadEntityBody();
//            //ByteArrayInputStream is=((ByteArrayInputStream)this.getContext().getRequest().entityBody());
//            java.io.BufferedReader reader=new java.io.BufferedReader(new java.io.InputStreamReader(this.getContext().getRequest().getEntityBodyStream()));
//            //byte[] arr=new byte[is.available()];
//            //int c=is.read(arr);
//            this.text(reader.readLine());
//            //

            for (Map.Entry<String, String> e : this.getContext().getRequest().form().entrySet()) {
                this.getContext().getResponse().write("key:" + e.getKey() + " = " + e.getValue() + "<br>");
            }
            HttpPostFile f = this.file("file");
            if (f != null) {
                f.savaAs("D:\\tmp\\" + f.getName() + "" + f.getExtension());
                this.getContext().getResponse().write("file:" + f.getOriginal());
            }
            this.getContext().getResponse().write(".all done");
        } else {
            //Thread.sleep(1000);
            set("time", DateTime.now().toString());
            this.view();
        }
    }

    @UrlMapping(value = "/test")
    void test() {
        text(this.getContext().getRequest().url().toString() + "<br>");
        text(this.session("sky") + "");
    }

    @UrlMapping(value = "/session")
    void session() {
        this.session("sky", DateTime.now().toString());
        text("set session:" + this.session("sky") + "");
    }

    @UrlMapping(value = "/xxs")
    void xss() {
        StringBuilder sb = new StringBuilder("<form method=\"post\"><textarea name=\"text\" style=\"width: 500px;height: 300px;\">");

        if (this.isPost()) {
            sb.append(this.form("text"));
        }

        sb.append("</textarea><button type=\"submit\">submit</button></form><hr>输出：<br>");
        XSSHtmlFliter filter = new XSSHtmlFliter();
        if (this.isPost()) {
            sb.append(filter.encode(this.form("text")));
            sb.append("<br>");
            sb.append("<textarea>" + filter.encode(this.form("text")) + "</textarea>");
            sb.append("<br>");
            //sb.append(this.form("text"));
        }

        this.text(sb.toString(), "text/html;charset=utf-8");

    }

}
