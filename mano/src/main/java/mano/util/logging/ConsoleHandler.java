/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.util.logging;

import java.io.IOException;

//http://www.2cto.com/kf/201302/191833.html

/**
 * 控制台日志处理程序。
 * @author jun <jun@diosay.com>
 */
public class ConsoleHandler extends StreamHandler{
    public ConsoleHandler(){
        this.setStream(System.out);
    }
    
    @Override
    public void close() throws IOException {
        //nothing
    }
}
