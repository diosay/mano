/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.util;

/**
 * 表示一个链表节点。
 * @author jun <jun@diosay.com>
 */
public interface LinkedListNode<T> {
    /**
     * 获取所属链表。
     * @return 
     */
    LinkedList<T> getList();
    /**
     * 获取当前节点的前一个节点。
     * @return 
     */
    LinkedListNode<T> getPrev();
    /**
     * 获取当前节点的后一个节点。
     * @return 
     */
    LinkedListNode<T> getNext();
    
    
    
}
