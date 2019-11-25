package com.company;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Server {
    private final String webServerRoot = "/Users/cg/data/code/wheel/java/demo/html";

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
                    new WorkThread(socket).run();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class WorkThread {
        private Socket socket;

        public WorkThread(Socket socket) {
            this.socket = socket;
        }

//        错误
//        public void WorkThread(Socket socket){
//            this.socket = socket;
//        }

        public void run() {
            System.out.println("工作线程\t" + Thread.currentThread() + "\t" + socket + "\t处理请求\t" + System.currentTimeMillis());
            try {
                InputStream inputStream = socket.getInputStream();
                byte[] buffer = new byte[1];

                FileOutputStream fileOutputStream = new FileOutputStream(webServerRoot + "/tmp" + System.currentTimeMillis());
                FileOutputStream fileOutputStream1 = new FileOutputStream(webServerRoot + "/log" + System.currentTimeMillis());

                int len2;
                int i = 0;
                boolean in = false;
                StringBuffer stringBuffer = new StringBuffer();
                String boundary = new String();
                boolean startEntity = false;
                boolean startBinary = false;
                boolean startFile = false;
                int preAsciiCode = 0;
                int prePreAsciiCode = 0;
                boolean firstCrlf = false;
                StringBuffer tmpBuffer = new StringBuffer();
                Map<String, Map> fileMetas = new HashMap<>();
                while ((len2 = inputStream.read(buffer)) != -1) {

                    fileOutputStream1.write(buffer);

//                    if(i > 25){
//
//                        break;
//                    }
//                    i++;

                    String lineStr = new String();
                    String preLineStr = new String();
                    String line = new String(buffer, 0, len2);
                    int asciiCode = Integer.parseInt(stringToAscii(line));



                    if (in == false && ((65 <= asciiCode && asciiCode <= 90) || asciiCode == 45)) {
                        in = true;
                    }
                    if (in == true && asciiCode == 10) {
                        in = false;
                        lineStr = stringBuffer.toString();
//                        System.out.println("lineStr\t" + lineStr);
                        stringBuffer = new StringBuffer();
                    }


                    if (in) {
                        stringBuffer.append(line);
                    }



                    if (lineStr.startsWith("Content-Type")) {
                        String[] lienArr = lineStr.split(";");
                        if (lienArr[0].endsWith("multipart/form-data")) {
                            boundary = lienArr[1].substring(lienArr[1].indexOf("=") + 1);
                        }
                    }

                    if (!startBinary && !boundary.isEmpty()) {

                        if (!lineStr.isEmpty() && lineStr.startsWith("-") && lineStr.contains(boundary)) {
//                            System.out.println("startBinary start====================");
//                            System.out.println(lineStr);
//                            System.out.println(stringToAscii(lineStr));
//                            System.out.println(boundary);
//                            System.out.println("startBinary end====================");
                            startBinary = true;
                        }
                    }

                    Map<String, String> fileMeta = new HashMap<>();
                    if(startBinary){

//                        获取下面的信息
//                        Content-Disposition: form-data; name="picture"; filename="tooopen_sy_10140314310699.jpg"
//                        Content-Type: image/jpeg

                        if(lineStr.contains("Content-Disposition")){
                            System.out.println("Content-Disposition start====================");
                            System.out.println(lineStr);
                            System.out.println(preAsciiCode);
                            System.out.println(asciiCode);
                            System.out.println("Content-Disposition end====================");
                            fileMeta = parseFileMeta(lineStr, fileMeta);
                        }

                        if(lineStr.contains("Content-Type")){
                            System.out.println("Content-Type start====================");
                            System.out.println(lineStr);
                            System.out.println(preAsciiCode);
                            System.out.println(asciiCode);
                            System.out.println("Content-Type end====================");
                            fileMeta = parseFileMeta(lineStr, fileMeta);
                        }

                        if(!fileMeta.isEmpty()){
                            fileMetas.put(fileMeta.get("name"), fileMeta);
                        }

                        if ((firstCrlf == false) && (asciiCode == 10 && preAsciiCode == 13 && prePreAsciiCode == 10)) {

                            firstCrlf = true;

                            prePreAsciiCode = preAsciiCode;
                            preAsciiCode = asciiCode;

                            continue;
                        }
//
//                        System.out.println("检查换行符 start====================");
//                        System.out.println(lineStr);
//                        System.out.println(preAsciiCode);
//                        System.out.println(asciiCode);
//                        System.out.println("检查换行符 end====================");
                    }

                    /**
                     * 遇到可疑字符，暂存起来，检查无误后，再集中写入文件
                     */
//                    if(asciiCode == 45 && preAsciiCode == 45){
//
//                        tmpBuffer.append(buffer);
//                    }


                    if(prePreAsciiCode == 13 && preAsciiCode == 10 && asciiCode == 45){
                        firstCrlf = false;
                    }

                    if (firstCrlf) {
                        fileOutputStream.write(buffer);
                    }

                    prePreAsciiCode = preAsciiCode;
                    preAsciiCode = asciiCode;

                }

                System.out.println("结束");

                System.out.println(fileMetas);
//
//                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buffer);
//                InputStreamReader requestInputStreamReader = new InputStreamReader(byteArrayInputStream);
//                BufferedReader bufferedReader = new BufferedReader(requestInputStreamReader);
//                String requestLine = bufferedReader.readLine();
//                System.out.println("=========================requestLine start");
//                System.out.println(requestLine + "\t" + Thread.currentThread());
//                System.out.println("=========================requestLine end");
//
//                // 获取实体体
//                String msg = new String("hello");
//                String[] msgArr = msg.split("\n");
//                String entityBody = msgArr[msgArr.length - 1];
//
////                获取文件路径
//                String[] requestLineArr = requestLine.split(" ");
//                String method = requestLineArr[0];
//                switch (method) {
//                    case "POST":
//                        doPost(entityBody);
//                        break;
//                    case "GET":
//                    default:
//                        doGet(bufferedReader, requestLineArr[1]);
//                }

//                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void doPost(String entityBody) throws IOException {
            System.out.println("POST 请求");
            String decodedEntityBody = URLDecoder.decode(entityBody, "UTF-8");
            System.out.println("decodedEntityBody start");
            System.out.println(decodedEntityBody);
            System.out.println("decodedEntityBody end");
            String[] decodedEntityBodyArr = decodedEntityBody.split("=");
            System.out.println(decodedEntityBodyArr[0] + "\t" + decodedEntityBodyArr[1]);
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("HTTP/1.1 200 OK\n");
            stringBuffer.append("Content-Type: text/html" + "; charset=UTF-8\n");

            byte[] decodedEntityBodyBuff = decodedEntityBody.getBytes();
            int valueLength = decodedEntityBodyBuff.length;

            Map<String, String> entityBodyMap = parseEntityBody(decodedEntityBody);
            Set<String> names = entityBodyMap.keySet();
            String keyValueStr = "";
            for (String name : names) keyValueStr += name + ":" + entityBodyMap.get(name) + "\n";
            byte[] keyValueStrBuff = keyValueStr.getBytes();
            int keyValueStrLength = keyValueStrBuff.length;

            stringBuffer.append("Content-Length: " + keyValueStrLength + "\n");
            stringBuffer.append("\n");

            stringBuffer.append(keyValueStr);


            String st2 = new String("管理员");
            System.out.println(st2.length());
            System.out.println(valueLength);

            OutputStream outputStream = socket.getOutputStream();

            outputStream.write(stringBuffer.toString().getBytes());

            outputStream.close();
        }

        private void doGet(BufferedReader bufferedReader, String filename) throws IOException {
            System.out.println("GET 请求");

            String fileType = getFileType(filename);
            OutputStream outputStream = socket.getOutputStream();
            StringBuffer stringBuffer = new StringBuffer();

            // 读取磁盘文件
            File file = new File(webServerRoot + filename);
            if (file.exists()) {
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
                while ((len = fileInputStream.read(buf)) != -1) {
                    dataOutputStream.write(buf, 0, len);
                }
                fileInputStream.close();
                bufferedReader.close();
                dataOutputStream.close();

            } else {
                stringBuffer.append("HTTP/1.1 404 Not Found\n");
                stringBuffer.append("\n");
                outputStream.write(stringBuffer.toString().getBytes());

                bufferedReader.close();
            }
        }

        private String getFileType(String filePath) {
            String fileType = "text/plain";

            if (filePath.endsWith("html")) {
                fileType = "text/html";
            } else {
                fileType = "image/jpeg";
            }

            return fileType;
        }

        private Map<String, String> parseEntityBody(String entityBody) {
            Map<String, String> entityBodyMap = new HashMap<>();
            String[] entityBodyArr = entityBody.split("&");
            for (String keyAndValue : entityBodyArr) {
                String[] keyAndValueArr = keyAndValue.split("=");
                entityBodyMap.put(keyAndValueArr[0], keyAndValueArr[1]);
            }

            return entityBodyMap;
        }

        public String stringToAscii(String value) {
            StringBuffer sbu = new StringBuffer();
            char[] chars = value.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                if (i != chars.length - 1) {
                    sbu.append((int) chars[i]).append(",");
                } else {
                    sbu.append((int) chars[i]);
                }
            }
            return sbu.toString();
        }

        public Map<String, String> parseFileMeta(String line, Map<String, String> fileMeta){
            String[] lineArr = line.split(";");
            for(String e : lineArr){
                String[] eArr = e.split("=");
                if(eArr.length == 1){
                    eArr = e.split(":");
                }
                fileMeta.put(eArr[0], eArr[1].replaceAll("\"", ""));
            }

            return fileMeta;
        }
    }
}
