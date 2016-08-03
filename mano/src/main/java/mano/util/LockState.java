/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.util;

/**
 *
 * @author jun <jun@diosay.com>
 */
public interface LockState {
    void notifyDone();
     boolean isDone();
     void waitDone() throws InterruptedException;
}
