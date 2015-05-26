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

    public ViewResult() {
    }

    @Override
    public void execute(ViewContext context) {
        if (context.getEngine() == null) {
            context.getContext().getResponse().write("this view engine not found.");
            return;
        }

        String path = context.getPath();

        if (path == null || "".equals(path)) {
            if (context.routePath().isEmpty()) {
                path = "~/" + context.getController() + "/" + context.getAction() + ".html";
            } else {
                path = "~/" + context.routePath().stream().reduce("/",
                        (result, element)
                        -> result = result + element) + ".html";
            }
        }
        
        context.setPath(path);
        context.getEngine().render(context);
    }

}
