package com.yuaniubei.nettystudy.bio;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created on 2021/7/11.
 */
@Slf4j
public class BIOServer {

    public static void main(String[] args) throws IOException {

        /**
         * cmd telnet 127.0.0.1 9999
         * 在控制台按下ctrl + ]
         * 在控制台发送消息 send "msg content"
         */
        ExecutorService executor = Executors.newCachedThreadPool();
        ServerSocket serverSocket = new ServerSocket(9999);
        log.info("socket server启动完成");
        for(;;){
            //监听accpet事件，获取连接后的socket，每个socket使用一个线程
            Socket socket = serverSocket.accept();
            log.info("有新的客户端链接");
            executor.execute(() -> {
               new BIOThread(socket).run();
            });
        }
    }
}

@Slf4j
class BIOThread implements  Runnable{

    private Socket socket;

    public BIOThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        byte[] bytes = new byte[1024];
        try {
            //获取输入流
            InputStream inputStream = socket.getInputStream();
            while (true) {
                int read = inputStream.read(bytes);
                if (read != -1) {
                    String str = new String(bytes, 0, read);
                    log.info("接受到的msg = {}, threadName = {}", str, Thread.currentThread().getName());
                } else {
                    //-1 表示读取完消息
                    break;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                log.info("链接断开, threadName = {}", Thread.currentThread().getName());
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
