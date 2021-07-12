package com.yuaniubei.nettystudy.nio;

import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * Created on 2021/7/11.
 */
@Slf4j
public class NIOServer {

    public static void main(String[] args)throws Exception {

        /**
         * 22:34:27.764 [main] INFO com.yuaniubei.nettystudy.nio.NIOServer - selectionKeys size = 1
         * 22:34:27.776 [main] INFO com.yuaniubei.nettystudy.nio.NIOServer - 客户端链接成功, channel = 682376643
         * 22:34:27.776 [main] INFO com.yuaniubei.nettystudy.nio.NIOServer - accpt byteBuffer = -1174962175
         * 22:34:33.781 [main] INFO com.yuaniubei.nettystudy.nio.NIOServer - selectionKeys size = 1
         * 22:34:33.781 [main] INFO com.yuaniubei.nettystudy.nio.NIOServer - read byteBuffer = -1174962175
         * 22:34:33.781 [main] INFO com.yuaniubei.nettystudy.nio.NIOServer - 获取到channel = 682376643 发送的消息 msg = 1
         *
         * 可以看出read中的byteBuffer就是accept中关联的byteBuffer
         */

        //开启网络通道
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        Selector selector = Selector.open();
        //serverSocketChannel绑定端口
        serverSocketChannel.socket().bind(new InetSocketAddress(9998));
        //将serverSocketChannel设置为非阻塞
        serverSocketChannel.configureBlocking(false);
        //把serverSocketChannle注册到selector上，事件为accept
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        for(;;){
            //阻塞1000毫秒，并在1000毫秒后返回结果
            int selectResult = selector.select(1000);
            if(0 == selectResult){
                //表示没有事件发生
                continue;
            }
            //获取关注事件集合
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            log.info("selectionKeys size = {}",selectionKeys.size());

            Iterator<SelectionKey> keyIterator = selectionKeys.iterator();
            while(keyIterator.hasNext()){
                SelectionKey key = keyIterator.next();
                if(key.isAcceptable()){
                    //如果是accept链接事件，生成一个socketchannel
                    SocketChannel channel = serverSocketChannel.accept();
                    log.info("客户端链接成功, channel = {}",channel.hashCode());
                    //设置为非阻塞
                    channel.configureBlocking(false);
                    //将socketChannel注册到selector上，并设置为read事件，同时关联一个byteBuffer
                    ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                    log.info("accpt byteBuffer = {}",byteBuffer.hashCode());
                    channel.register(selector,SelectionKey.OP_READ, byteBuffer);
                }else if(key.isReadable()){
                    //通过selectkey获取channnel
                    SocketChannel channel = (SocketChannel) key.channel();
                    //通过selectkey获取bytebuffer
                    ByteBuffer buffer = (ByteBuffer) key.attachment();
                    log.info("read byteBuffer = {}",buffer.hashCode());
                    channel.read(buffer);
                    String msg = new String(buffer.array());
                    log.info("获取到channel = {} 发送的消息 msg = {}",channel.hashCode(),msg);
                }
                //移除selectkey，防止重复操作
                keyIterator.remove();
            }
        }
    }
}
