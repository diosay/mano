/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.configuration;

import java.util.ArrayList;
import java.util.List;
import mano.util.xml.XmlHelper;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 表示一个列表元素。
 *
 * @author jun <jun@diosay.com>
 */
public class ListElement {

    public List<String> parse(XmlHelper helper, Node node) throws Exception {

        if (helper == null) {
            throw new IllegalArgumentException("helper");
        }
        if (node == null) {
            throw new IllegalArgumentException("node");
        }
        List<String> list=new ArrayList<>();
        NodeList nodes=helper.selectNodes(node, "add|clear|remove");
        if(nodes!=null){
            for(int i=0;i<nodes.getLength();i++){
                node=nodes.item(i);
                switch (node.getNodeName()) {
                    case "clear":
                        list.clear();
                        break;
                    case "remove":
                        list.remove(node.getTextContent());
                        break;
                    case "add":
                        list.add(node.getTextContent());
                        break;
                }
            }
        }
        return list;
    }
}
