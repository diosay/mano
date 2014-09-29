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
public class AuthorizationFilter implements ActionFilter {

    @Filter(AuthorizationFilter.class)
    @Filter(AuthorizationFilter.class)
    void test() {

    }

    @Override
    public boolean onActionExecuting(ViewContext context) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean onActionExecuted(ViewContext context) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
