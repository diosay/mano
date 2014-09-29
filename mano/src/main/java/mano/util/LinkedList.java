/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.util;

/**
 * 表示双向链接列表。
 *
 * @author jun <jun@diosay.com>
 */
public class LinkedList<T> {

    /**
     * 实现的一个节点。
     */
    public final class Node implements LinkedListNode<T> {

        private LinkedListNode<T> prev;
        private LinkedListNode<T> next;
        @Override
        public LinkedList<T> getList() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public LinkedListNode<T> getPrev() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public LinkedListNode<T> getNext() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        public void addAfter(LinkedListNode<T> node) {
            if (prev != null) {
                
            }
        }

    }

    LinkedListNode<T> first;
    LinkedListNode<T> last;

    public void addAfter() {

    }

    /**
     * 在列表的开头处添加包含指定值的新节点。
     *
     * @param value
     */
    public void addFirst(T value) {
        Node node = new Node();
        this.addFirst(node);
    }

    /**
     * 在列表的开头处添加指定的新节点。
     *
     * @param node
     */
    public void addFirst(LinkedListNode<T> node) {
        if (this.first == null) {
            this.first = node;
        } else {
            
            //remove()
            //addbefor()
            //first.addafter(node)
        }
    }
}
