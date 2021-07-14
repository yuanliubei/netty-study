package com.yuaniubei.nettystudy.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * Created on 2021/7/14.
 */
@Slf4j
public class NettyServer {

    public static void main(String[] args) throws Exception {

        /**
         * bossGroup只处理连接，真正的io、业务处理交给workGroup
         * NioEventLoopGroup其实就是线程组，默认是线程数默认是cpu个数*2
         */
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workGroup = new NioEventLoopGroup();

        try {
            //启动引导类
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)   //配制tcp参数，将backlog设置为128
                    .childOption(ChannelOption.SO_KEEPALIVE, true)  //设置保持活动连接状态
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            //给pipline设置处理器
                            socketChannel.pipeline().addLast(new NettyServerHandler());
                        }
                    });

            //启动并绑定端口
            ChannelFuture channelFuture = bootstrap.bind(9999).sync();
            log.info("netty server started");
            //对通道关闭进行监听
            channelFuture.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }
}

@Slf4j
class NettyServerHandler extends ChannelInboundHandlerAdapter {

    /**
     * 开始有消息读取
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel channel = ctx.channel();
        ByteBuf byteBuf = (ByteBuf) msg;
        log.info("受到客户端发送的消息, msg = {} ",byteBuf.toString(CharsetUtil.UTF_8));
        log.info("客户端的地址是 = {}",ctx.channel().remoteAddress().toString());
    }

    /**
     * 读取消息完成
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        ctx.writeAndFlush(Unpooled.copiedBuffer("消息读取完成", CharsetUtil.UTF_8));
    }

    /**
     * 出现异常，断开连接
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.channel().close();
    }
}