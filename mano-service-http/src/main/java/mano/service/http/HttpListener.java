package mano.service.http;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import mano.DateTime;
import mano.io.ChannelException;
import mano.net.NioSocketChannelListener;
import mano.net.http.HttpContext;
import mano.net.http.HttpRequest;
import mano.net.http.HttpResponse;
import mano.net.http.HttpServer;
import mano.web.HttpSession;
import mano.web.WebApplication;

/**
 *
 * @author jun
 */
public class HttpListener extends NioSocketChannelListener {

    class HttpContextImpl extends NioChannel implements HttpContext {

        public int step;
        public static final int STEP_REQUEST_LINE = 0;
        public static final int STEP_HEADERS = 1;
        public static final int STEP_PROC = 2;
        public java.util.Map<String, String> headers = new java.util.HashMap<>();
        public String orgUrl;
        public String method;
        public String version;
        public ByteBuffer keepBuffer;
        public int phonse = 0;

        public HttpRequestImpl request;
        public HttpResponseImpl response;
        public HttpServer server;
        public HttpContextImpl() {
            request = new HttpRequestImpl();
            request.context=this;
            response = new HttpResponseImpl();
            response.context = this;
        }
        
        @Override
        public void handleError(Throwable cause){
            super.handleError(cause);
            if(!response.headerSent()){
                
                response.status(400);
                response.keepAlive=false;
                response.write("Bad Request (Invalid Hostname)");
                response.end();
                
            }
            else{
                
                this.close();
            }
        }

        public void handleRequest() {

            HttpRequestHandlerAdapter adapter = (HttpRequestHandlerAdapter) getContext().items().getOrDefault(HttpService.REQUEST_HANDLER_PROVIDER_KEY, null);
            if (adapter == null) {

            }
            adapter.handleRequest(this, t -> {
                this.handleError(t);
            });
            //response.write("<h2>hello</h2>");
            //response.flush();
            //response.write("<h2>,world</h2>");
//            try {
//                response.transmit("E:\\demo\\jettyserver\\webapps\\ROOT\\index.html");
//            } catch (IOException ex) {
//                ex.printStackTrace();
//            }
//            response.end();
            /*byte[] hello = ("hello,world " + DateTime.now() + "<hr>" + "URL:" + request.rawUrl()).getBytes();
             StringBuilder sb = new StringBuilder();
             sb.append("HTTP/1.1 200 OK").append("\r\n");
             sb.append("Date:").append(DateTime.now().toGMTString()).append("\r\n");
             sb.append("Connection:").append("close").append("\r\n");//Keep-Alive
             sb.append("Content-length:").append(hello.length).append("\r\n");
             sb.append("Content-Type:").append("text/html;charset=utf-8").append("\r\n");
             sb.append("\r\n");

             try {
             channel().write(sb.toString().getBytes());
             ((NioChannel) channel()).submit(createWriteFuture(ByteBuffer.wrap(hello)).addTask((ctx2, fu) -> {
             ctx2.channel().close();
             }));
             } catch (ChannelException ex) {
             handleError(ex);
             }*/
        }

        @Override
        public WebApplication getApplication() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean isCompleted() {
            return this.response.done;
        }

        @Override
        public HttpRequest getRequest() {
            return request;
        }

        @Override
        public HttpResponse getResponse() {
            return response;
        }

        @Override
        public HttpServer getServer() {
            return server;
        }

        @Override
        public HttpSession getSession() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    }

    @Override
    protected NioChannel createChannel() {
        return new HttpContextImpl();
    }

}
