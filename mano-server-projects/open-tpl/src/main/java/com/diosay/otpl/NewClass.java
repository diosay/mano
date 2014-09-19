/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.diosay.otpl;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class NewClass {

    public void setFilename() {

    }

    public void setOpenToken() {

    }

    public void setCloseToken() {
    }

    Charset charset = Charset.forName("utf-8");
    int lineNo;

    public void parse(InputStream in) throws IOException {

        lineNo = 0;
        mano.net.ByteArrayBuffer buf = new mano.net.ByteArrayBuffer(100);

        byte[] open = "{{".getBytes(charset);
        mano.net.ByteArrayBuffer.bytesIndexOf(null, lineNo, lineNo, open);

        BufferedReader reader = new BufferedReader(new InputStreamReader(in, charset));
        String line = reader.readLine();
        lineNo++;
        //findMarkup(line,0,0)

    }

    public static void mainxxx(String[] args) throws Exception {
        System.out.println("end");
    }
}
