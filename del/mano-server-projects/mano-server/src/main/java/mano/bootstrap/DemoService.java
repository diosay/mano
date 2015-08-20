/*
 * Copyright (C) 2014-2015 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.bootstrap;

import java.nio.ByteBuffer;
import mano.DateTime;
import mano.io.ByteBufferPool;
import mano.io.ChannelContext;
import mano.io.ChannelException;
import mano.io.ChannelHandler;
import mano.io.ChannelHandlerChain;
import mano.io.ChannelListener;
import mano.io.ChannelListenerContext;
import mano.net.AIOSocketChannelListener;
import mano.net.SSLHandler;
import mano.service.Service;
import mano.util.CachedObjectRecyler;
import mano.util.Pool;
import mano.util.ThreadPool;

/**
 *
 * @author johnwhang
 */
public class DemoService extends Service {

    private String serviceName;

    @Override
    public String getServiceName() {
        return serviceName;
    }

    @Override
    protected void onInit() throws Exception {
        serviceName = this.getContext().getProperty("service.name");
    }

//    class HttpHandler implements ChannelHandler {
//
//        ExecutorService executor;
//        java.nio.charset.CharsetEncoder encoder = java.nio.charset.Charset.forName("utf-8").newEncoder();
//
//        @Override
//        public void handleConnected(Channel channel) {
//            try {
//                Request req=new Request();
//                req.channel=channel;
//                requests.put(channel, req);
//                channel.read();
//                //channel.read();
//            } catch (ChannelException ex) {
//                channel.close();
//            }
//        }
//
//        @Override
//        public void handleDisconnected(Channel channel) {
//            System.out.println("channel closed");
//        }
//
//        @Override
//        public ExecutorService getEexecutor() {
//            return executor;
//        }
//
//        @Override
//        public BufferAllocator getAllocator() {
//            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//        }
//        
//        class Request{
//            Channel channel;
//            ByteBuffer buffer;
//            java.util.LinkedList<String> headers=new java.util.LinkedList<>();
//            boolean done;
//        }
//        java.util.HashMap<Channel,Request> requests=new java.util.HashMap<>();
//
//        int i=0;
//        @Override
//        public void handleReceived(Channel channel, ByteBuffer buffer) {
//
//            Request req=requests.get(channel);
//            if(!req.done){
//                
//                if(req.buffer!=null){
//                    
//                    if(req.buffer.remaining()>=buffer.remaining()){
//                        req.buffer.put(buffer);
//                        req.buffer.flip();
//                    }else{
//                        while(req.buffer.hasRemaining()&&buffer.hasRemaining()){
//                            req.buffer.put(buffer.get());
//                        }
//                        req.buffer.flip();
//                        
//                    }
//                    
//                }
//                
//                
//                
//            }
//            
//            
//            
//            System.out.println("RECV:" + new String(buffer.array(), buffer.arrayOffset() + buffer.position(), buffer.remaining()));
//
//            byte[] hello = ("hello,world 你好世界 " + DateTime.now()).getBytes();
//            StringBuilder sb = new StringBuilder();
//            sb.append("HTTP/1.1 200 OK").append("\r\n");
//            sb.append("Date:").append(DateTime.now().toGMTString()).append("\r\n");
//            sb.append("Connection:").append("close").append("\r\n");
//            sb.append("Content-length:").append(hello.length).append("\r\n");
//            sb.append("\r\n");
//
//            try {
//                channel.write(sb, encoder);
//
//                channel.write(hello, 0, hello.length);
//
//                channel.submit((ChanneFilterChainFactory factory, Channel channel1) -> {
//                    channel1.close(false);
//                    System.out.println("closing here");
//                });
//            } catch (ChannelException ex) {
//                channel.close();
//            }
//
//        }
//
//        @Override
//        public void handleError(Channel channel, Throwable cause) {
//            cause.printStackTrace();
//            channel.close();
//        }
//    }
//    
//    
    class HttpHandler implements ChannelHandler {

        java.nio.charset.CharsetEncoder encoder = java.nio.charset.Charset.forName("utf-8").newEncoder();

        @Override
        public void handleConnected(ChannelContext context, ChannelHandlerChain chain) {
            System.out.println("conn...");
            try {
                context.channel().read();
            } catch (ChannelException ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public void handleDisconnected(ChannelContext context, ChannelHandlerChain chain) {
            System.out.println("channel closed");
        }

        @Override
        public void handleInbound(ChannelContext context, ChannelHandlerChain chain, ByteBuffer buffer) {

            System.out.println("RECV:" + new String(buffer.array(), buffer.arrayOffset() + buffer.position(), buffer.remaining()));

            context.free(buffer);

            byte[] hello = ("hello,world 你好世界 " + DateTime.now()).getBytes();
            StringBuilder sb = new StringBuilder();
            sb.append("HTTP/1.1 200 OK").append("\r\n");
            sb.append("Date:").append(DateTime.now().toGMTString()).append("\r\n");
            sb.append("Connection:").append("close").append("\r\n");
            sb.append("Content-length:").append(hello.length).append("\r\n");
            sb.append("\r\n");

            try {
                context.channel().write(sb, encoder);

                context.channel().write(hello, 0, hello.length);

                context.channel().submit((ChannelContext channel1) -> {
                    System.out.println("response done.closing...");
                    channel1.channel().close(false);
                });
            } catch (ChannelException ex) {
                context.channel().close();
            }
        }

        @Override
        public void handleOutbound(ChannelContext context, ChannelHandlerChain chain, ByteBuffer buffer) {
            if (buffer.hasRemaining()) {
                context.putOutbound(buffer);
            } else {
                context.free(buffer);
            }
        }

        @Override
        public void handleError(ChannelContext context, ChannelHandlerChain chain, Throwable cause) {
            System.out.println("ERR HANDING:");
            cause.printStackTrace();
        }

        @Override
        public void setProperty(String property, Object value) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void init() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void destroy() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    }

    Pool<ByteBuffer> sslBufferPool = new CachedObjectRecyler<ByteBuffer>() {

        @Override
        protected ByteBuffer createNew() {
            return ByteBuffer.allocate(1024 * 17);//SSLFilter.DEFAULT_BUFFER_SIZE
        }
    };

    @Override
    protected void onStart() throws Exception {

        //handler.executor = Executors.newFixedThreadPool(4);// Executors.newCachedThreadPool();//Executors.newFixedThreadPool(4);
        System.out.println("CPUs:" + Runtime.getRuntime().availableProcessors());

        ChannelListenerContext context = new ChannelListenerContext(ThreadPool.getService());

        ChannelListener listener = new AIOSocketChannelListener();
        listener.addHandler(new SSLHandler());
        listener.addHandler(new HttpHandler());

        listener.closedEvent().add((sender, e) -> {
            
        });
        listener.bind("localhost:443", 0);
        listener.setContext(context);
        listener.run();
    }

    @Override
    protected void onStop() throws Exception {

    }

}
