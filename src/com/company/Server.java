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
            System.out.println("工作线程\t" + Thread.currentThread() + "\t" + socket + "\t处理请求\t" + System.currentTimeMillis());
            try {
                InputStream inputStream = socket.getInputStream();
                byte[] buffer = new byte[1024];
                inputStream.read(buffer);
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buffer);
                InputStreamReader requestInputStreamReader = new InputStreamReader(byteArrayInputStream);
                BufferedReader bufferedReader = new BufferedReader(requestInputStreamReader);
                String requestLine = bufferedReader.readLine();
                System.out.println("=========================requestLine start");
                System.out.println(requestLine + "\t" + Thread.currentThread());
                System.out.println("=========================requestLine end");

//                获取文件路径
                String[] requestLineArr = requestLine.split(" ");
                String filename = requestLineArr[1];
                System.out.println(filename);

                String fileType = getFileType(filename);
                OutputStream outputStream = socket.getOutputStream();
                StringBuffer stringBuffer = new StringBuffer();

                // 读取磁盘文件
                File file = new File("/Users/cg/data/code/wheel/java/demo/html" + filename);
                if(file.exists()){
                    stringBuffer.append("HTTP/1.1 200 OK\n");

                    stringBuffer.append("Content-Type: " + fileType + "; charset=UTF-8\n");
                    stringBuffer.append("Connection: closed\n");

                    stringBuffer.append("Content-Length: " + file.length() + "\n");

                    stringBuffer.append("\n");
                    outputStream.write(stringBuffer.toString().getBytes());

                    FileInputStream fileInputStream = new FileInputStream(file);

                    DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

                    int len;
                    byte[] buf = new byte[1024];
                    while ((len = fileInputStream.read(buf)) != -1){
                        dataOutputStream.write(buf, 0, len);
                    }
                    fileInputStream.close();
                    bufferedReader.close();
                    dataOutputStream.close();

                }else{
                    stringBuffer.append("HTTP/1.1 404 Not Found\n");
                    stringBuffer.append("\n");
                    outputStream.write(stringBuffer.toString().getBytes());

                    bufferedReader.close();
                }

                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private String getFileType(String filePath){
            String fileType = "text/plain";

            if(filePath.equals("/test.html")){
                fileType = "text/html";
            }else if(filePath.equals("/2015110214261032002.jpg")){
                fileType = "image/jpeg";
            }

            return fileType;
        }
    }
}
