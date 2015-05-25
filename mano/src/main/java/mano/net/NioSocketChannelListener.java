package mano.net;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.charset.CharsetEncoder;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import mano.DateTime;
import mano.InvalidOperationException;
import mano.Queue;
import mano.io.Channel;
import mano.io.ChannelContext;
import mano.io.ChannelException;
import mano.io.ChannelHandlerChain;
import mano.io.ChannelFuture;
import mano.io.ChannelListenerAbstract;
import mano.logging.Log;
import mano.util.ScheduleTask;

/**
 *
 * @author junman
 */
public class NioSocketChannelListener extends ChannelListenerAbstract {

    private ServerSocketChannel serverChannel;
    private java.util.concurrent.Semaphore backlog = new java.util.concurrent.Semaphore(0);

    @Override
    public void bind(String address, int backlog) throws IOException {
        InetSocketAddress inet;
        try {
            if (address == null) {
                throw new NullPointerException("address is required.");
            }

            String protocol;
            String addr;
            int port;
            int index = address.indexOf("://");
            int last = address.lastIndexOf(":");
            if (index <= 0) {
                throw new IllegalArgumentException("protocol is required and must be a tcp.ipv4 or tcp.ipv6.");
            }
            if (last <= 0) {
                throw new IllegalArgumentException("port is required.");
            }
            protocol = address.substring(0, index);
            addr = address.substring(index + 3, last);
            port = Integer.parseInt(address.substring(last + 1).trim());
            if (port <= 0) {
                throw new IllegalArgumentException("port must be greater than 0.");
            }
            if ("tcp.ipv4".equalsIgnoreCase(protocol) || "tcp.ipv6".equalsIgnoreCase(protocol)) {
                inet = new InetSocketAddress(addr, port);
            } else {
                throw new UnsupportedOperationException("protocol must be a tcp.ipv4 or tcp.ipv6.");
            }
        } catch (Throwable t) {
            throw new IOException("Failed to resolving to address.", t);
        }

        this.serverChannel = ServerSocketChannel.open();
        this.serverChannel.bind(inet, backlog <= 0 ? 20 : (backlog / 5 <= 5 ? 5 : backlog / 5));
        this.serverChannel.socket().setReuseAddress(true);
        this.backlog = new java.util.concurrent.Semaphore(backlog <= 0 ? 100 : backlog);
    }
    
    @Override
    protected void onStart() throws Exception {
        if (serverChannel == null) {
            throw new Exception("unbind address");
        }
        
        mano.Queue<IOWorker> workers = new mano.util.ArrayBlockingQueue<>();
        IOWorker worker;

        for (int i = 0; i < Runtime.getRuntime().availableProcessors() * 2; i++) {
            worker = new IOWorker();
            worker.init();
            Thread tworker = new Thread(worker);
            tworker.setName("SelectableChannel IO Wroker");
            tworker.start();
            workers.offer(worker);
            Thread.sleep(10);
        }

        System.out.println("started listener: " + serverChannel.getLocalAddress());
        String oname = Thread.currentThread().getName();
        Thread.currentThread().setName("ChannelListener");
        try {
            while (this.isOpen()) {
                if (this.backlog.tryAcquire(1000 * 60 * 10, TimeUnit.MILLISECONDS)) {
                    if(serverChannel!=null){
                        
//                        NioChannel nc=new NioChannel();
//                        nc.real=serverChannel.accept();
//                        nc.real.configureBlocking(false);
//                        nc.test();
                        
                        worker = workers.poll();
                        worker.queue(serverChannel.accept());
                        if(worker.running){
                            workers.offer(worker);
                        }
                    }
                } else {
                    this.close();
                    break;
                }
            }
        } finally {
            Thread.currentThread().setName(oname);
        }

    }

    @Override
    protected void onStop() {
        if (serverChannel != null) {
            try {
                System.out.println("stopped listener: " + serverChannel.getLocalAddress());
                serverChannel.close();
            } catch (IOException ex) {

            }
            serverChannel = null;
        }
    }

    protected NioChannel createChannel() {
        return new NioChannel();
    }

    protected class ReadChannelFuture extends ChannelFuture {

        ByteBuffer buf;

        public ReadChannelFuture() {
            super(null, ChannelFuture.OP_READ);
        }

        public ReadChannelFuture setBuffer(ByteBuffer buffer) {
            this.buf = buffer;
            return this;
        }

        @Override
        protected void doExecute(ChannelContext context) {
            if (buf == null) {
                buf = context.allocBuffer();//ByteBuffer.allocate(160);
            }
            try {
                int len = context.recv(buf);
                //System.out.println("channel reading count:" + len);
                if (len < 0) {
                    context.handleError(new IOException("远程客户端连接中断。"));
                    context.freeBuffer(buf);
                    return;
                }
                buf.flip();
                //System.out.println("be in");
                context.filterInbound(buf);
//                if(buf.hasRemaining()){
//                    context.filterInbound(buf);
//                }else{
//                    context.freeBuffer(buf);
//                }
            } catch (IOException ex) {
                //ex.printStackTrace();
                context.handleError(ex);
            }

        }

    }

    protected class WriteByteArrayChannelFuture extends ChannelFuture {

        private ByteBuffer buffer;

        public WriteByteArrayChannelFuture() {
            super(null, ChannelFuture.OP_WRITE);
        }

        public WriteByteArrayChannelFuture setBuffer(ByteBuffer buffer) {
            this.buffer = buffer;
            return this;
        }

        @Override
        protected void doExecute(ChannelContext context) {
            try {
                context.send(buffer);
            } catch (IOException ex) {
                context.handleError(ex);
            }
        }

    }

    protected class WriteFileChannelFuture extends ChannelFuture {

        private long position;
        private long length;
        private String filename;
        private boolean keep;

        public WriteFileChannelFuture() {
            super(null, ChannelFuture.OP_WRITE);
        }

        protected void doExecute2(ChannelContext context) {
            try {

                try (FileChannel chan = new FileInputStream(filename).getChannel()) {
                    chan.position(position);
                    long len = length;
                    int count;
                    ByteBuffer buf;
                    while (len > 0) {
                        buf = context.allocBuffer();
                        count = Math.min(buf.limit(), (int) len);
                        buf.limit(count);
                        len -= chan.read(buf);

                        buf.flip();
                        context.filterOutbound(buf);
                    }
                }
            } catch (IOException ex) {
                context.handleError(ex);
            }
        }

        @Override
        public boolean keepAndNextCall() {
            return keep;
        }
        FileChannel chan;

        private void init() throws FileNotFoundException, IOException {
            if (chan == null) {
                chan = new FileInputStream(filename).getChannel();
                chan.position(position);
            }
        }

        @Override
        protected void doExecute(ChannelContext context) {
            try {
                if (length > 0) {
                    init();
                    ByteBuffer buf = context.allocBuffer();
                    int count = chan.read(buf);
                    if (count > 0) {
                        buf.flip();
                        context.filterOutbound(buf);
                        length -= count;
                    }
                    if (length > 0) {
                        keep = true;
                    } else {
                        keep = false;
                        chan.close();
                        chan = null;
                    }
                }
            } catch (IOException ex) {
                context.handleError(ex);
            }
        }

        @Override
        protected void finalize() throws Throwable {
            try {
                this.chan.close();
            } catch (IOException ex) {
            }
            chan = null;
            super.finalize();
        }

    }

    protected class IOWorker implements Runnable {

        Selector selector;
        mano.util.ArrayBlockingQueue<java.nio.channels.SocketChannel> queued = new mano.util.ArrayBlockingQueue<>();

        public void queue(java.nio.channels.SocketChannel sc) {
            if(!running){
                try {
                    sc.close();
                } catch (IOException ex) {
                    
                }
            }
            queued.offer(sc);
        }

        public void init() throws IOException {
            selector = Selector.open();
        }
        boolean running;
        @Override
        public void run() {
            running=true;
            System.out.println("worker started:" + this.hashCode());
            try {
                exec();
            } catch (IOException ex) {
                ex.printStackTrace();//TODO:异常处理
            }
            System.out.println("worker stopped:" + this.hashCode());
            running=false;
        }

        public void exec() throws IOException {
            java.nio.channels.SocketChannel sc;
            SelectionKey key;
            Iterator<SelectionKey> iter;
            Runnable task;
            while (isOpen()) {

                while ((sc = queued.poll()) != null) {
                    register(selector, sc);
                }
                if (selector.select(10) <= 0) {//TODO:TTFB等待过长这里的因素居多，重点关注
                    Thread.yield();
                    //continue;
                }

                iter = selector.selectedKeys().iterator();
                while (iter.hasNext()) {
                    key = iter.next();
                    task = (Runnable) key.attachment();
                    if (task != null) {
                        task.run();//TODO:内部处理不能出现异常
                    }
                    iter.remove();
                }
            }
        }

        private void register(Selector selector, java.nio.channels.SocketChannel sc) throws IOException {
            sc.configureBlocking(false);
            configureSocket(sc.socket());
            NioChannel chan = createChannel();
            chan.key = sc.register(selector, 0, chan);//SelectionKey.OP_READ | SelectionKey.OP_WRITE
            chan.real = sc;
            
            //chan.worker = this;
            selector.wakeup();
            
            //System.out.println("conn:" + sc.getRemoteAddress());
            try (ChannelHandlerChain chain = getHandlerChain()) {
                chain.handleOpened(chan);
            }
            //chan.connTime = System.currentTimeMillis();
        }

        private int lingerTime = -1;

        protected void configureSocket(Socket socket) {
            try {
                socket.setTcpNoDelay(true);
                if (lingerTime >= 0) {
                    socket.setSoLinger(true, lingerTime / 1000);
                } else {
                    socket.setSoLinger(false, 0);
                }
            } catch (SocketException e) {
                Log.TRACE.debug(e);
            }
        }

    }

    protected class NioChannel implements ChannelContext, Channel, Runnable {

        private SelectionKey key;
        private java.nio.channels.SocketChannel real;
        private Queue<ChannelFuture> readPaddings = new mano.util.ArrayBlockingQueue<>();
        private Queue<ChannelFuture> writePaddings = new mano.util.ArrayBlockingQueue<>();
        //private IOWorker worker;
        long runtime;
        long connTime;
        long requestId;
        public long getRequestId(){
            return requestId;
        }
        public NioChannel() {
            connTime = System.currentTimeMillis();
            requestId=this.hashCode();
            ScheduleTask.register(time -> {
                //System.out.println("check...");
                if (time - runtime > 5000) {
                    runtime = time;

                    if (!isOpen()) {
                        //TODO:空闲检测？
                        //System.out.println("channel closed");
                        close();
                    }
//                    readPaddings.forEachRemove(f -> {
//                        if (time - f.getLifetime() > 5000) {
//                            handleError(new IllegalMonitorStateException("read timeout:"+(time - f.getLifetime())));
//                            f.release();
//                            return true;
//                        }
//                        return false;
//                    });
//                    writePaddings.forEachRemove(f -> {
//                        if (time - f.getLifetime() > 5000) {
//                            handleError(new IllegalMonitorStateException("write timeout:"+(time - f.getLifetime())));
//                            f.release();
//                            return true;
//                        }
//                        return false;
//                    });
                }

                return !isOpen();
            });
        }

        @Override
        public Channel channel() {
            return this;
        }

        @Override
        public int send(ByteBuffer buffer) throws IOException {
            return real.write(buffer);
        }

        @Override
        public int recv(ByteBuffer buffer) throws IOException {
            return real.read(buffer);
        }

        @Override
        public Object get(String key) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void set(String key, Object value) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        //@Override
        public Object remove(String key) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public ByteBuffer allocBuffer() {
            return getContext().getBufferPool().get();
        }

        @Override
        public void freeBuffer(ByteBuffer buffer) {
            getContext().getBufferPool().put(buffer);
        }

        @Override
        public void release() {
        }

        @Override
        public void handleError(Throwable cause) {
            cause.printStackTrace();
        }

        @Override
        public boolean isSecure() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void read() throws ChannelException {
            ChannelFuture f = new ReadChannelFuture();
            submit(f.fresh());
        }

        @Override
        public void read(ByteBuffer buffer) throws ChannelException {
            submit(new ReadChannelFuture().setBuffer(buffer).fresh());
        }

        @Override
        public void write(CharSequence content, CharsetEncoder encoder) throws ChannelException {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void write(byte[] buffer) throws ChannelException {
            write(buffer, 0, buffer.length);
        }

        @Override
        public void write(byte[] buffer, int offset, int count) throws ChannelException {
            submit(new WriteByteArrayChannelFuture().setBuffer(ByteBuffer.wrap(buffer, offset, count)).fresh());
        }

        @Override
        public void submit(ChannelFuture future) {
            if (!this.isOpen()) {
                throw new InvalidOperationException(new IOException("远程客户端连接中断。"));
            }
            if (future.operation() == ChannelFuture.OP_READ) {
                readPaddings.offer(future);
                if ((key.interestOps() & SelectionKey.OP_READ) == 0) {
                    //System.out.println("reg interset read");
                    key.interestOps(SelectionKey.OP_READ);
                    key.selector().wakeup();
                }
            } else if (future.operation() == ChannelFuture.OP_WRITE) {
                writePaddings.offer(future);
                if ((key.interestOps() & SelectionKey.OP_WRITE) == 0) {
                    //System.out.println("reg interset write");
                    key.interestOps(SelectionKey.OP_WRITE);
                    key.selector().wakeup();
                }
            }
        }

        @Override
        public void close() {

            if (real != null) {
//                try {
//                    real.shutdownOutput();
//                    real.shutdownInput();
//                }catch(Throwable t){}
                
                try {
                    real.close();
                } catch (IOException ex) {

                } finally {
                    backlog.release();
                    real = null;
                }
//                if (Log.TRACE.isTraceEnabled()) {
//                    Log.TRACE.trace("Request("+getRequestId()+") done,total times:" + (System.currentTimeMillis() - connTime) + "ms");
//                }
            }
        }

        @Override
        public boolean isOpen() {
            return real != null && real.isOpen();
        }

        @Override
        public void run() {
            try {
                if (!isOpen() || !key.isValid()) {
                    close();
                    return;
                }
                //test();
                ChannelFuture future;
                if (key.isReadable() && (future = this.readPaddings.peek()) != null) {
                    future.fresh().execute(this);//刷新并执行
                    if (!future.keepAndNextCall()) {
                        this.readPaddings.poll().release();
                    }
                }
                if (key.isWritable() && (future = this.writePaddings.peek()) != null) {
                    future.fresh().execute(this);//刷新并执行
                    if (!future.keepAndNextCall()) {
                        this.writePaddings.poll().release();
                    }
                }
            } catch (Throwable t) {
                handleError(t);
            }
        }
        
        void test() throws IOException{
            byte[] hello = ("hello,world " + DateTime.now()).getBytes();
             StringBuilder sb = new StringBuilder();
             sb.append("HTTP/1.1 200 OK").append("\r\n");
             sb.append("Date:").append(DateTime.now().toGMTString()).append("\r\n");
             sb.append("Connection:").append("close").append("\r\n");//Keep-Alive
             sb.append("Content-length:").append(hello.length).append("\r\n");
             sb.append("Content-Type:").append("text/html;charset=utf-8").append("\r\n");
             sb.append("\r\n");
             
             this.send(ByteBuffer.wrap(sb.toString().getBytes()));
             this.send(ByteBuffer.wrap(hello));
             this.close();
             
        }

        public void filterInbound(ByteBuffer buffer) {
            try (ChannelHandlerChain chain = getHandlerChain()) {
                chain.handleInbound(this, buffer);
            }
        }

        public void filterOutbound(ByteBuffer buffer) {
            try (ChannelHandlerChain chain = getHandlerChain()) {
                chain.handleOutbound(this, buffer);
            }
        }

        public ChannelFuture createWriteFuture(ByteBuffer buffer) {
            return new WriteByteArrayChannelFuture().setBuffer(buffer);
        }

        @Override
        public void write(ByteBuffer buffer, boolean copy) throws ChannelException {
            if (copy) {
                ByteBuffer buf;
                while (buffer.hasRemaining()) {
                    buf = allocBuffer();
                    while (buf.hasRemaining() && buffer.hasRemaining()) {
                        buf.put(buffer.get());
                    }
                    buf.flip();
                    writePaddings.offer(new WriteByteArrayChannelFuture().setBuffer(buf).fresh());
                }
            } else {
                writePaddings.offer(new WriteByteArrayChannelFuture().setBuffer(buffer).fresh());
            }
        }

        @Override
        public void write(String filename, long position, long length) throws ChannelException {
            WriteFileChannelFuture wfc = new WriteFileChannelFuture();
            wfc.filename = filename;
            wfc.position = position;
            wfc.length = length;
            writePaddings.offer(wfc.fresh());
        }
    }

}
