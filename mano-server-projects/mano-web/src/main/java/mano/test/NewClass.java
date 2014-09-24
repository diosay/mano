/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */

package mano.test;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import io.netty.channel.ChannelHandlerAdapter;
/**
 *
 * @author jun <jun@diosay.com>
 */
public class NewClass {
    public static void main(String[] args) {
        ServerBootstrap bootstrap = new ServerBootstrap();
        EventLoopGroup bossGroup = new NioEventLoopGroup(); 
        EventLoopGroup workerGroup = new NioEventLoopGroup();
//        bootstrap.group(bossGroup,workerGroup)
//                .channel(NioServerSocketChannel.class)
//                .childHandler(new ChannelInitializer<SocketChannel>() { // (4)
//                 @Override
//                 public void initChannel(SocketChannel ch) throws Exception {
//                     //ch.pipeline().addLast(new DiscardServerHandler());
//                 }
//             })
//             .option(ChannelOption.SO_BACKLOG, 128)          // (5)
//             .childOption(ChannelOption.SO_KEEPALIVE, true); 
        io.netty.buffer.ByteBuf b;
        bootstrap.bind(new InetSocketAddress(8080));
        System.out.println("服务器已经启动，请访问http://127.0.0.1:8080/index.html进行测试！\n\n");
    }
}
