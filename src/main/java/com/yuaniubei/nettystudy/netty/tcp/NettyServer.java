package com.yuaniubei.nettystudy.netty.tcp;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.net.URI;


/**
 * Created on 2021/7/25.
 */
@Slf4j
public class NettyServer {

    public static void main(String[] args) {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workGroup = new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap();
        try {

            bootstrap.group(workGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new NettyServerHandler());

            ChannelFuture channelFuture = bootstrap.bind(9999).sync();
            channelFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if(channelFuture.isSuccess()){
                        log.info("监听端口成功");
                    }else{
                        log.info("监听端口失败");
                    }
                }
            });

            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }
}


class NettyServerHandler extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        //得到管道
        ChannelPipeline pipeline = socketChannel.pipeline();
        //向管道中添加HttpServerCoder 编解码器
        pipeline.addLast("MyHttpServerCodec", new HttpServerCodec());
        //添加自定义handler
        pipeline.addLast("MyTestHttpServerHandler", new TestHttpServerHandler());
    }
}

@Slf4j
class TestHttpServerHandler extends SimpleChannelInboundHandler<HttpObject> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, HttpObject httpObject) throws Exception {
        //判断是否是httpRequest请求
        if (httpObject instanceof HttpRequest) {
            int handlerContextHashCode = channelHandlerContext.pipeline().hashCode();
            Class<? extends HttpObject> clz = httpObject.getClass();
            SocketAddress clientAddr = channelHandlerContext.channel().remoteAddress();

            log.info("handlerContextHashCode = {}", handlerContextHashCode);
            log.info("clz = {}", clz);
            log.info("clentAddr = {}", clientAddr);
            //获取到httpRequest
            HttpRequest httpRequest = (HttpRequest) httpObject;
            //获取到uri对象
            URI uri = new URI(httpRequest.uri());
            log.info("uri = {}", uri);
            ByteBuf content = Unpooled.copiedBuffer("HELLO，我是服务器", CharsetUtil.UTF_8);
            //构造response
            DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, content);
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());

            channelHandlerContext.writeAndFlush(response);
        }
    }
}
