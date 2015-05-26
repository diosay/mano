/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.web;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import mano.net.http.HttpException;
import mano.net.http.HttpStatus;

/**
 * 表示一个嵌套的 Web 应用模块。
 *
 * @author jun
 */
public abstract class NestedModule extends Module implements ActionResult {

    /**
     * 注意：非特定程序请勿调用。
     *
     * @param context
     */
    @Override
    public final void execute(ViewContext context) {
        init(context);
        //getRemainingRoutingPath
        //getRemainingRouteSegments
        List<String> segments = new ArrayList<>();
        try {
            if (segments.size() == 0) {
                resolveAndExecute(context, "index");
            } else {
                resolveAndExecute(context, segments.remove(0));
            }
        } catch (Exception ex) {
            if(ex instanceof InvocationTargetException){
                throw new RuntimeException(((InvocationTargetException)ex).getTargetException());
            }else{
                throw new RuntimeException(ex);
            }
        }
    }

    void resolveAndExecute(ViewContext context, String action) throws HttpException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        if (action == null || action == "") {
            throw new HttpException(HttpStatus.NotFound);
        }
        Method method = null;
        for (Method member : this.getClass().getDeclaredMethods()) {
            if (member.getName().equalsIgnoreCase(action) && member.getParameterCount() == 0) {
                method = member;
                break;
            }
        }
        if (method == null) {
            throw new HttpException(HttpStatus.NotFound);
        }
        context.addRoutePath(action.toLowerCase());
        Filter[] tmps = method.getAnnotationsByType(Filter.class);
        ArrayList<ActionFilter> filters = new ArrayList<>();
        for (Filter f : tmps) {
            filters.add(f.value().newInstance());
        }
        //执行过滤器
        for (ActionFilter filter : filters) {
            if (!context().getRequest().isConnected() || !filter.onActionExecuting(context)) {
                return;
            }
        }
        if (context().getRequest().isConnected()) {
            //执行方法
            method.setAccessible(true);
            Object result = method.invoke(this, new Object[0]);
            //渲染结果
            if (result == null) {

            }
            if (!context.getContext().isCompleted() && result instanceof ActionResult) {
                ((ActionResult) result).execute(context);
            } else if (!context.getContext().isCompleted()) {
                context.getContext().getResponse().write(result);
            }
        }

        //执行过滤器
        for (ActionFilter filter : filters) {
            if (!context().getRequest().isConnected() || !filter.onActionExecuted(context)) {
                return;
            }
        }
    }

}
