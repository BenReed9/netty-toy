package com.reed.neety.toy.echo.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.Data;

import java.net.InetSocketAddress;

@Data
public class EchoServer {

    private final int port;

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println(
                    "Usage: " + EchoServer.class.getSimpleName() +
                            " <port>");
            return;
        }

        // 设置端口值 , 创建呼叫服务器的 start 方法
        new EchoServer(Integer.parseInt(args[0])).start();
    }

    private void start() {

        // 创建 EventLoopGroup
        NioEventLoopGroup group = new NioEventLoopGroup();

        // 创建 ServerBootstrap
        ServerBootstrap b = new ServerBootstrap();
        b.group(group)
                // 指定 NIO 传输 Channel
                .channel(NioServerSocketChannel.class)
                // 设置 socket 地址所选端口
                .localAddress(new InetSocketAddress(port))
                // 添加 EchoServerHandler 到 Channel 的 ChannelPipeline
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                .addLast(new EchoServerHandler());
                    }
                });

        try {
            // 绑定的服务器;sync 等待服务器关闭
            ChannelFuture f = b.bind().sync();
            System.out.println(EchoServer.class.getName() + "started and listen on " + f.channel().localAddress());
            // 关闭 channel 和 块，直到它被关闭
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            try {
                // 关机的 EventLoopGroup，释放所有资源
                group.shutdownGracefully().sync();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }

    }

}
