package mano.net;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.charset.CharsetEncoder;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import mano.Queue;
import mano.io.Channel;
import mano.io.ChannelContext;
import mano.io.ChannelException;
import mano.io.ChannelHandlerChain;
import mano.io.ChannelFuture;
import mano.io.ChannelListenerAbstract;
import mano.io.ChannelTask;

/**
 *
 * @author junman
 */
public class NioSocketChannelListener extends ChannelListenerAbstract {

    private ServerSocketChannel serverChannel;
    private java.util.concurrent.Semaphore backlog = new java.util.concurrent.Semaphore(0);

    @Override
    public void bind(String address, int backlog) throws IOException {
        this.serverChannel = ServerSocketChannel.open();
        this.serverChannel.bind(new InetSocketAddress(8082));
        this.backlog = new java.util.concurrent.Semaphore(backlog <= 0 ? 100 : backlog);
    }

    @Override
    protected void onStart() throws Exception {
        if (serverChannel == null) {
            throw new Exception("unbind address");
        }

        //ChannelListenerPipe pipe = new ChannelListenerPipe();
        //pipe.addFilter(new DemoChannelFilter());
        //this.addHandler(new DemoChannelFilter());
        mano.Queue<IOWorker> workers = new mano.util.ArrayBlockingQueue<>();

        IOWorker worker;

        for (int i = 0; i < Runtime.getRuntime().availableProcessors() * 2; i++) {
            worker = new IOWorker();
            //worker.pipe = pipe;
            worker.init();
            mano.util.ThreadPool.execute(worker);
            workers.offer(worker);
        }

        System.out.println("started listener: " + serverChannel.getLocalAddress());
        while (this.isOpen()) {
            if (this.backlog.tryAcquire(1000 * 60 * 10, TimeUnit.MILLISECONDS)) {
                worker = workers.poll();
                worker.queue(serverChannel.accept());
                workers.offer(worker);
            } else {
                this.close();
                break;
            }
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
                //System.out.println("read:" + len);
                if (len <= 0) {
                    context.handleError(new IOException("接收到0字節"));
                    context.freeBuffer(buf);
                    return;
                }
                buf.flip();
                context.filterInbound(buf);
            } catch (IOException ex) {
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

        public WriteFileChannelFuture() {
            super(null, ChannelFuture.OP_WRITE);
        }

        @Override
        protected void doExecute(ChannelContext context) {
            try {

                try (FileChannel chan = new FileInputStream(filename).getChannel()) {
                    chan.position(position);
                    long len=length;
                    int count;
                    ByteBuffer buf;
                    while(len>0){
                        buf=context.allocBuffer();
                        count=Math.min(buf.limit(), (int)len);
                        buf.limit(count);
                        len-=chan.read(buf);
                        
                        buf.flip();
                        context.filterOutbound(buf);
                    }
                }
            } catch (IOException ex) {
                context.handleError(ex);
            }
        }

    }

    protected class IOWorker implements Runnable {

        Selector selector;
        mano.util.ArrayBlockingQueue<java.nio.channels.SocketChannel> queued = new mano.util.ArrayBlockingQueue<>();

        public void queue(java.nio.channels.SocketChannel sc) {
            queued.offer(sc);
        }

        public void init() throws IOException {
            selector = Selector.open();
        }

        @Override
        public void run() {
            System.out.println("worker started:" + this.hashCode());
            try {
                exec();
            } catch (IOException ex) {
                ex.printStackTrace();//TODO:异常处理
            }
            System.out.println("worker stopped:" + this.hashCode());
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

                if (selector.select(200) <= 0) {
                    try {
                        Thread.sleep(1); //让出当前线程给其它线程处理
                    } catch (InterruptedException ex) {
                        //nothing
                    }
                    continue;
                }

                iter = selector.selectedKeys().iterator();
                while (iter.hasNext()) {
                    task = (Runnable) (iter.next().attachment());
                    if (task != null) {
                        task.run();//TODO:内部处理不能出现异常
                    }
                    iter.remove();
                }
            }
        }

        private void register(Selector selector, java.nio.channels.SocketChannel sc) throws IOException {
            sc.configureBlocking(false);
            NioChannel chan = createChannel();
            chan.key = sc.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, chan);
            chan.real = sc;
            chan.worker = this;
            selector.wakeup();
            //System.out.println("conn:" + sc.getRemoteAddress());
            try (ChannelHandlerChain chain = getHandlerChain()) {
                chain.handleOpened(chan);
            }
        }

    }

    protected class NioChannel implements ChannelContext, Channel, Runnable {

        private SelectionKey key;
        private java.nio.channels.SocketChannel real;
        private Queue<ChannelFuture> readPaddings = new mano.util.ArrayBlockingQueue<>();
        private Queue<ChannelFuture> writePaddings = new mano.util.ArrayBlockingQueue<>();
        private IOWorker worker;

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
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void handleError(Throwable cause) {
            cause.printStackTrace();
        }

        //@Override
        public void submit(ChannelTask task) throws ChannelException {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean isSecure() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        //@Override
        public boolean hasPaddingWrite() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        //@Override
        public boolean hasPaddingRead() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void read() throws ChannelException {
            ChannelFuture f = new ReadChannelFuture();
            readPaddings.offer(f.fresh());
        }

        @Override
        public void read(ByteBuffer buffer) throws ChannelException {
            readPaddings.offer(new ReadChannelFuture().setBuffer(buffer).fresh());
        }

        //@Override
        public void queueReadBuffer(ByteBuffer buffer) throws ChannelException {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
            writePaddings.offer(new WriteByteArrayChannelFuture().setBuffer(ByteBuffer.wrap(buffer, offset, count)).fresh());
        }

        @Override
        public void submit(ChannelFuture future) {
            if (future.operation() == ChannelFuture.OP_READ) {
                readPaddings.offer(future);
            } else if (future.operation() == ChannelFuture.OP_WRITE) {
                writePaddings.offer(future);
            }
        }

        //@Override
        public void queueWriteBuffer(ByteBuffer buffer) throws ChannelException {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void close(boolean force) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void close() {
            if (real != null) {
                try {
                    real.close();
                } catch (IOException ex) {

                } finally {
                    backlog.release();
                    real = null;
                }
            }
        }

        @Override
        public void await() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean isOpen() {
            return real != null && real.isOpen();
        }

        @Override
        public void run() {
            ChannelFuture future;
            while (key.isValid() && key.isReadable() && (future = this.readPaddings.poll()) != null) {
                future.execute(this);
                future.release();
            }
            while (key.isValid() && key.isWritable() && (future = this.writePaddings.poll()) != null) {
                future.execute(this);
                future.release();
            }

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
                ByteBuffer buf = allocBuffer();
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
            WriteFileChannelFuture wfc=new WriteFileChannelFuture();
            wfc.filename=filename;
            wfc.position=position;
            wfc.length=length;
            writePaddings.offer(wfc.fresh());
        }
    }

}
