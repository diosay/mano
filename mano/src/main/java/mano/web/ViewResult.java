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
public class ViewResult implements ActionResult {
    
    private ViewEngine engine;
    private String template;
    
    public ViewResult() {
    }
    
    public final ViewResult init(ViewEngine ve) {
        engine = ve;
        return this;
    }
    
    @Override
    public void execute(ViewContext context) {
        if (engine == null) {
            context.getContext().getResponse().write("this view engine not found.");
            return;
        }
        
        String path = context.getPath();
        if (path == null || "".equals(path)) {
            path = "~/"+context.getController() + "/" + context.getAction() + ".html";
        }
        context.setPath(path);
        engine.render(context);
    }
    
}
