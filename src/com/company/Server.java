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
            String uri = "/post.php";
            Socket phpSocket = new Socket("127.0.0.1", 9000);
            OutputStream outputStream = phpSocket.getOutputStream();
            outputStream.write(uri.getBytes());
            outputStream.flush();
            while (true) {
                Socket socket = serverSocket.accept();
                if (socket != null) {
                    new WorkThread(socket).run();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
