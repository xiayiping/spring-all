package org.xyp.demo.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.val;

import java.net.InetSocketAddress;

public class NettyEchoClient {
    private final String host;
    private final int port;

    public NettyEchoClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public static void main(String[] args) throws Exception {
        new NettyEchoClient("localhost", 8085).start();
    }

    private void start() throws Exception {
        val group = new NioEventLoopGroup();
        try {
            val bootstrap = new Bootstrap();
            bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .remoteAddress(new InetSocketAddress(host, port))
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new EchoClientHandler());
                    }
                });
            val future = bootstrap.connect().sync();
            future.channel().closeFuture().sync();

        } finally {
            group.shutdownGracefully().sync();
        }

    }
}
