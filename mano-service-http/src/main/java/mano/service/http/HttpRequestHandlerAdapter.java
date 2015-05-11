
package mano.service.http;

import mano.service.http.HttpListener.HttpContextImpl;

/**
 *
 * @author jun
 */


interface HttpRequestHandlerAdapter {
    public interface ErrorHandler{
        void handleError(Throwable cause);
    }
    void handleRequest(HttpContextImpl context,ErrorHandler errorHandler);
}
