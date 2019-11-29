package com.company;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Server {

    public static void main(String[] args) {
        String configFile = "";
        String defaultConfigFile = "/Users/cg/data/code/wheel/java/demo/html/config.json";
        ConfigParser configParser = new ConfigParser();
        File file = new File(configFile);
        if (!file.exists()) {
            configFile = defaultConfigFile;
        }

        HashMap<String, HashMap> config = configParser.parse(configFile);

        Server server = new Server();
        server.masterThread(config);
    }

    private void masterThread(HashMap<String, HashMap> config) {
        for (HashMap.Entry<String, HashMap> entry : config.entrySet()) {
            new VirtualMachineThread(entry.getValue()).start();
        }
    }

    private class VirtualMachineThread extends Thread {

        private HashMap<String, String> serverConfig;

        public VirtualMachineThread(HashMap<String, String> serverConfig) {
            this.serverConfig = serverConfig;
        }

        @Override
        public void run() {
            try {
                int port = Integer.parseInt(serverConfig.get("port"));
                ServerSocket serverSocket = new ServerSocket(port);

                while (true) {
                    Socket socket = serverSocket.accept();
                    if (socket != null) {
                        new WorkThread(socket, serverConfig).run();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
