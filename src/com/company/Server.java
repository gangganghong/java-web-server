package com.company;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    public static void main(String[] args) {
        Server server = new Server();
        server.masterThread();
    }

    private void masterThread() {
        try {
            ServerSocket serverSocket = new ServerSocket(2000);
            while (true) {
                Socket socket = serverSocket.accept();
                if (socket != null) {
                    new WorkThread(socket).start();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class WorkThread extends Thread {
        private Socket socket;

        public WorkThread(Socket socket) {
            this.socket = socket;
        }

//        错误
//        public void WorkThread(Socket socket){
//            this.socket = socket;
//        }

        @Override
        public void run() {
            System.out.println("工作线程\t" + Thread.currentThread() + "\t处理请求\t" + System.currentTimeMillis());
            try {
                OutputStream outputStream = socket.getOutputStream();
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("HTTP/1.1 200 OK\n");
                stringBuffer.append("Content-Type: text/html; charset=UTF-8\n");
                stringBuffer.append("Connection: keep-alive\n");

                // 读取磁盘文件
                File file = new File("/Users/cg/data/code/wheel/java/demo/html/test.html");
                stringBuffer.append("Content-Length: " + file.length() + "\n");

                stringBuffer.append("\n");

                FileInputStream fileInputStream = new FileInputStream(file);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");

                while (inputStreamReader.ready()) {
                    stringBuffer.append((char) inputStreamReader.read());
                }

                outputStream.write(stringBuffer.toString().getBytes());
                outputStream.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
