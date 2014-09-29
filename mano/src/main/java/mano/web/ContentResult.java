/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.web;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class ContentResult implements  ActionResult{
    private String content;
    public ContentResult(String s){
        this.content=s;
    }
    @Override
    public void execute(ViewContext service) {
        if(this.content!=null && !service.getContext().isCompleted()){
            service.getContext().getResponse().charset("utf-8");
            service.getContext().getResponse().write(this.content);
        }
    }
    
}
