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
//                String msg = new String(buffer);
//                System.out.println(msg);
//                System.out.println("=========================");
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buffer);
                InputStreamReader requestInputStreamReader = new InputStreamReader(byteArrayInputStream);
                BufferedReader bufferedReader = new BufferedReader(requestInputStreamReader);
                String requestLine = bufferedReader.readLine();
                if(requestLine == null){
//                    return;
                }
                System.out.println("=========================requestLine start");
                System.out.println(requestLine + "\t" + Thread.currentThread());
                System.out.println("=========================requestLine end");

//                GET /2015110214261032002.jpg HTTP/1.1

//                获取文件路径
                String[] requestLineArr = requestLine.split(" ");
                if(requestLineArr.length < 2){
//                    return;
                }
                String filename = requestLineArr[1];
                System.out.println(filename);

//                GET /2015110214261032002.jpg HTTP/1.1
//                Host: localhost:2000
//                Connection: keep-alive
//                Pragma: no-cache
//                Cache-Control: no-cache
//                Upgrade-Insecure-Requests: 1
//                User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.108 Safari/537.36
//                Sec-Fetch-User: ?1
//                Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3
//                Sec-Fetch-Site: none
//                Sec-Fetch-Mode: navigate
//                Accept-Encoding: gzip, deflate, br
//                Accept-Language: zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7





                OutputStream outputStream = socket.getOutputStream();
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("HTTP/1.1 200 OK\n");
                String fileType = getFileType(filename);
                stringBuffer.append("Content-Type: " + fileType + "; charset=UTF-8\n");
                stringBuffer.append("Connection: closed\n");

                // 读取磁盘文件
                File file = new File("/Users/cg/data/code/wheel/java/demo/html" + filename);
                stringBuffer.append("Content-Length: " + file.length() + "\n");
//                java.net.SocketException: Broken pipe (Write failed)
//                stringBuffer.append("Transfer-Encoding: chunked\n");

                FileInputStream fileInputStream = new FileInputStream(file);

                if(fileType == "image/jpeg"){
                    stringBuffer.append("\n");
                    outputStream.write(stringBuffer.toString().getBytes());
                    System.out.println(111111);
                    int len = 0;
                    byte[] buf = new byte[1024];
                    while ((len = fileInputStream.read(buf)) != -1){
                        outputStream.write(buf, 0, len);
                    }
                }else{
                    System.out.println(222222);
                    stringBuffer.append("\n");
                    InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
                    while (inputStreamReader.ready()) {
                        stringBuffer.append((char) inputStreamReader.read());
//                        outputStream.write(inputStreamReader.read());
                    }
                    outputStream.write(stringBuffer.toString().getBytes());
                }

                outputStream.flush();
                System.out.println("文件输出结束");
                System.out.println("thread status:\t" + Thread.currentThread().isAlive());
                sleep(5000);
//                socket.shutdownOutput();
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
