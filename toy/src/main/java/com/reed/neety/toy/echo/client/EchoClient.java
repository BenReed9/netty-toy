package com.reed.neety.toy.echo.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Data;

import java.net.InetSocketAddress;

/**
 * 一个 Bootstrap 被创建来初始化客户端
 * 一个 NioEventLoopGroup 实例被分配给处理该事件的处理，这包括创建新的连接和处理入站和出站数据
 * 一个 InetSocketAddress 为连接到服务器而创建
 * 一个 EchoClientHandler 将被安装在 pipeline 当连接完成时
 * 之后 Bootstrap.connect（）被调用连接到远程的 - 本例就是 echo(回声)服务器。
 *
 * @Author: reed
 */
@Data
public class EchoClient {
    private final String host;
    private final int port;

    public void start() throws Exception {
        // 创建 Bootstrap
        Bootstrap bootstrap = new Bootstrap();

        // 指定 EventLoopGroup 来处理客户端事件。由于我们使用 NIO 传输，所以用到了 NioEventLoopGroup 的实现
        NioEventLoopGroup group = new NioEventLoopGroup();
        try {
            bootstrap.group(group)
                    // 使用的 channel 类型是一个用于 NIO 传输
                    .channel(NioSocketChannel.class)
                    // 设置服务器的 InetSocketAddress
                    .remoteAddress(new InetSocketAddress(host, port))
                    // 当建立一个连接和一个新的通道时，创建添加到 EchoClientHandler 实例 到 channel pipeline
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    .addLast(new EchoClientHandler());
                        }
                    });

            // 连接到远程;等待连接完成
            ChannelFuture sync = bootstrap.connect().sync();

            // 阻塞直到 Channel 关闭
            sync.channel().closeFuture().sync();
        } finally {
            // 调用 shutdownGracefully() 来关闭线程池和释放所有资源
            group.shutdownGracefully().sync();
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println(
                    "Usage: " + EchoClient.class.getSimpleName() +
                            " <host> <port>");
            return;
        }

        final String host = args[0];
        final int port = Integer.parseInt(args[1]);

        new EchoClient(host, port).start();
    }
}
