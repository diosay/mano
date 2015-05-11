/*
 * Copyright (C) 2014-2015 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.io;

import java.nio.ByteBuffer;
import mano.io.ChannelContextOld;
import mano.io.ChannelException;
import mano.io.ChannelTask;

/**
 * @deprecated 移除
 * 用于I/O的任务。
 * @author sixmoon
 */
final class ChannelTaskInternal implements ChannelTask {
    /**
     * 表示一个读操作。
     */
    public static final Integer OP_READ = 1;
    /**
     * 表示一个写操作。
     */
    public static final Integer OP_WRITE = 2;
    private static final Integer TP_BUFFER = 1;
    private static final Integer TP_FILE = 1;
    private static final Integer OP_DEFAULT = 0;
    private Integer op;
    private Integer type;
    private ByteBuffer buffer;
    
    /**
     * 内部实例化。
     */
    ChannelTaskInternal(){
        
    }
    
    /**
     * 设置缓冲区，并指定将要执行的操作类型。
     * @param op
     * @param buffer 
     */
    public void set(Integer op, ByteBuffer buffer) {
        this.buffer = buffer;
        this.op = op;
        this.type = TP_BUFFER;
    }

    @Override
    public void execute(ChannelContextOld context) {
//        if (OP_WRITE.equals(op)) {
//            try {
//                context.channel().queueWriteBuffer(buffer);
//            } catch (ChannelException ex) {
//                ex.printStackTrace();
//            }
//        } else if (OP_READ.equals(op)) {
//            try {
//                context.channel().queueReadBuffer(buffer);
//            } catch (ChannelException ex) {
//                ex.printStackTrace();
//            }
//        }
    }

}
