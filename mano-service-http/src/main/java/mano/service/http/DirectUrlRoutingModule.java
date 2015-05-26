package mano.service.http;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import mano.net.http.HttpContext;
import mano.net.http.HttpModule;
import mano.util.Utility;
import mano.web.ActionFilter;
import mano.web.ActionResult;
import mano.web.Filter;
import mano.web.Module;
import mano.web.ViewContext;
import mano.web.ViewEngine;
import mano.web.ViewResult;
import mano.web.WebApplication;

/**
 *
 * @author jun
 */
public class DirectUrlRoutingModule implements HttpModule {

    private WebApplication app;
    private ViewEngine viewEngine;
    
    @Override
    public void init(WebApplication app, Properties params) {
        this.app = app;
        try {
            viewEngine = (ViewEngine) app.getLoader().newInstance(params.getProperty("view.engine"));
            viewEngine.setTempdir(Utility.toPath(app.getApplicationPath(), "WEB-INF/tmp").toString());
            viewEngine.setViewdir(Utility.toPath(app.getApplicationPath(), "views").toString());
        } catch (Throwable ex) {
            if (app.getLogger().isDebugEnabled()) {
                app.getLogger().debug("failed to initialization module:", ex);
            }
        }
    }

    @Override
    public boolean handle(HttpContext context) throws Exception {
        return this.handle(context, context.getRequest().url().getPath());
    }

    @Override
    public boolean handle(HttpContext context, String tryPath) throws Exception {
        
        if (viewEngine == null) {
            return false;
        } else if (!context.getRequest().isConnected()) {
            return false;
        }
        
        String[] segments=Utility.split(tryPath.toLowerCase(), "/", true);
        if(segments.length>=2){
            return resolveAndExecute(context,segments[0],segments[1],Arrays.copyOfRange(segments, 2, segments.length-2));//TODO:从配置加载
        }else if(segments.length==1){
            return resolveAndExecute(context,segments[0],"index",Arrays.copyOfRange(segments, 1, segments.length-1));//TODO:从配置加载
        }else{
            return resolveAndExecute(context,"home","index",segments);//TODO:从配置加载
        }
    }

    @Override
    public void dispose() {
        app = null;
    }
    
    //首字母转大写
    public static String toUpperCaseFirstOne(String s)
    {
        if(Character.isUpperCase(s.charAt(0)))
            return s;
        else
            return (new StringBuilder()).append(Character.toUpperCase(s.charAt(0))).append(s.substring(1)).toString();
    }
    
    Class<?> findClass(String name){
        String[] nsarr=app.getActionNamespaces();
        if(nsarr==null){
            return null;
        }
        Class<?> result;
        for(String ns:nsarr){
            
            try {
                result=app.getLoader().loadClass(ns+"."+name);
                
                if(Module.class.isAssignableFrom(result)){
                    return result;
                }
                
            } catch (ClassNotFoundException ex) {
                //ex.printStackTrace();
            }
            
        }
        
        
        return null;
    }
    
    private boolean resolveAndExecute(HttpContext context,String module,String action,String[] segments) throws ClassNotFoundException, InstantiationException, IllegalAccessException, Exception{
        
        //查找类
        module=module.toLowerCase();
        Class<?> clazz=findClass(toUpperCaseFirstOne(module)+"Module");
        if(clazz==null){
            clazz=findClass(toUpperCaseFirstOne(module));
        }
        if(clazz==null){
            return false;
        }
        
        //查找方法
        Method actionMethod=null;
        for (Method method : clazz.getDeclaredMethods()) {
            if(method.getName().equalsIgnoreCase(action) && method.getParameterCount()==0){
                actionMethod=method;
                break;
            }
        }
        
        if(actionMethod==null){
            return false;
        }
        
        //查找与实例化过滤器
        Filter[] tmps = clazz.getAnnotationsByType(Filter.class);
        ArrayList<ActionFilter> filters=new ArrayList<>();
        for(Filter f:tmps){
            filters.add(f.value().newInstance());
        }
        
        actionMethod.setAccessible(true);
        
        ViewContext vc=viewEngine.createContext(context);
        vc.setEngine(viewEngine);
        vc.setSegments(segments);
        //vc.setController(module);
        //vc.setAction(action.toLowerCase());
        vc.addRoutePath(module);
        vc.addRoutePath(action.toLowerCase());
        Module obj=(Module)clazz.newInstance();
        
        //执行过滤器
        for(ActionFilter filter:filters){
            if(!context.getRequest().isConnected() || !filter.onActionExecuting(vc)){
                return true;
            }
        }
        
        //初始化
        //TODO:init
        
        Method initMethod=Module.class.getDeclaredMethod("init", ViewContext.class);
        initMethod.setAccessible(true);
        initMethod.invoke(obj, vc);
        
        //执行方法
        Object result=null;
        if(!context.isCompleted()){
        try {
            result=actionMethod.invoke(obj, new Object[0]);
        }catch (InvocationTargetException ex) {
            throw ex.getTargetException()==null?ex:new Exception(ex.getTargetException());
        }}
        
        
        
        //渲染结果
        if(result==null){
            
        }else if(!context.isCompleted() && result instanceof ActionResult){
            ((ActionResult)result).execute(vc);
        }else if(!context.isCompleted()){
            context.getResponse().write(result);
        }
        
        //执行过滤器
        for(ActionFilter filter:filters){
            if(context.isCompleted() || !filter.onActionExecuted(vc)){
                return true;
            }
        }
        
        if(!context.isCompleted()){
            context.getResponse().end();
        }
        return true;
    }

}
