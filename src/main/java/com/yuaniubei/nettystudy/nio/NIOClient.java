package com.yuaniubei.nettystudy.nio;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

@Slf4j
public class NIOClient {

    public static void main(String[] args) throws IOException {

        //开启网络通道
        SocketChannel socketChannel = SocketChannel.open();
        //设置为非阻塞
        socketChannel.configureBlocking(false);
        InetSocketAddress inetSocketAddress = new InetSocketAddress("127.0.0.1", 9998);

        boolean connect = socketChannel.connect(inetSocketAddress);
        if(!connect){
            while(socketChannel.finishConnect()){
                log.info("链接成功");
                break;
            }
        }
        String str = "hello, nio";
        //声明byteBuffer
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        byteBuffer.put(str.getBytes(StandardCharsets.UTF_8));
        //反转缓冲区
        byteBuffer.flip();
        socketChannel.write(byteBuffer);
        log.info("发送消息, msg = {}",new String(byteBuffer.array()));
        while(true){

        }
    }
}
