 
package mano.io;

import java.nio.ByteBuffer;

/**
 * @deprecated 移除
 * @author jun
 */
public abstract class IOBuffer {
    private volatile long lifetime;
    
    /**
     * 获取缓冲区。
     * @return 
     */
    public abstract ByteBuffer buffer();
    
    /**
     * 获取构建当前实例的提供程序。
     * @return 
     */
    public abstract IOBufferProvider provider();
    
    /**
     * 释放当前实例(或返回池中以供下次使用)。
     */
    public abstract void release();
    
    /**
     * 获取上次刷新的时间。
     * @return 
     */
    public final long getLifetime(){
        return this.lifetime;
    }
    
    /**
     * 刷新时间用于计时操作。
     */
    public final IOBuffer fresh(){
        this.lifetime=System.currentTimeMillis();
        return this;
    }
    
    
}
