package com.company;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class WorkThread {
    private final String webServerRoot = "/Users/cg/data/code/wheel/java/demo/html";
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

            FileOutputStream fileOutputStream = null;
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
            HashMap<String, HashMap> fileMetas = new HashMap<>();
            HashMap<String, String> fileMeta = new HashMap<>();


            while ((len2 = inputStream.read(buffer)) != -1) {

                fileOutputStream1.write(buffer);

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
                        startBinary = true;
                    }
                }

                if (startBinary) {

//                        获取下面的信息
//                        Content-Disposition: form-data; name="picture"; filename="tooopen_sy_10140314310699.jpg"
//                        Content-Type: image/jpeg

                    if (lineStr.contains("Content-Disposition")) {
                        fileMeta = parseFileMeta(lineStr, fileMeta);
                    }

                    if (lineStr.contains("Content-Type")) {
                        fileMeta = parseFileMeta(lineStr, fileMeta);
                    }

                    if (!fileMeta.isEmpty()) {
                        String filename = fileMeta.get("name");
                        if (filename != null) {
                            fileMetas.put(filename, fileMeta);
                        }
                    }

                    if ((firstCrlf == false) && (asciiCode == 10 && preAsciiCode == 13 && prePreAsciiCode == 10)) {

                        firstCrlf = true;

                        prePreAsciiCode = preAsciiCode;
                        preAsciiCode = asciiCode;

                        fileOutputStream = new FileOutputStream(webServerRoot + "/tmp" + System.currentTimeMillis());

                        continue;
                    }

                    if (prePreAsciiCode == 13 && preAsciiCode == 10 && asciiCode == 45) {
                        firstCrlf = false;
                        fileOutputStream.close();
                    }
                }

                if (firstCrlf) {
                    fileOutputStream.write(buffer);
                }

                if(asciiCode == 10 && preAsciiCode == 13 && prePreAsciiCode == 45){
                    OutputStream outputStream = socket.getOutputStream();
                    doPost(outputStream);
                    outputStream.flush();
                    outputStream.close();
                    break;
                }

                prePreAsciiCode = preAsciiCode;
                preAsciiCode = asciiCode;
            }


            System.out.println(fileMetas);




//            关闭后，浏览器异常，不能收到返回

            fileOutputStream.close();
            fileOutputStream1.close();
            inputStream.close();

            socket.close();
            System.out.println("结束");
            System.out.println(fileMetas);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doPost(OutputStream outputStream) throws IOException {
        System.out.println("POST 请求");
//        String decodedEntityBody = URLDecoder.decode(entityBody, "UTF-8");
//        System.out.println("decodedEntityBody start");
//        System.out.println(decodedEntityBody);
//        System.out.println("decodedEntityBody end");
//        String[] decodedEntityBodyArr = decodedEntityBody.split("=");
//        System.out.println(decodedEntityBodyArr[0] + "\t" + decodedEntityBodyArr[1]);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("HTTP/1.1 200 OK\n");
//        stringBuffer.append("Content-Type: text/html" + "; charset=UTF-8\n");

//        byte[] decodedEntityBodyBuff = decodedEntityBody.getBytes();
//        int valueLength = decodedEntityBodyBuff.length;
//
//        Map<String, String> entityBodyMap = parseEntityBody(decodedEntityBody);
//        Set<String> names = entityBodyMap.keySet();
//        String keyValueStr = "";
//        for (String name : names) keyValueStr += name + ":" + entityBodyMap.get(name) + "\n";
//        byte[] keyValueStrBuff = keyValueStr.getBytes();
//        int keyValueStrLength = keyValueStrBuff.length;

        String keyValueStr = new String("hello");
        String html = "<html><head><title>cg</title></head><body><p>I am cg!</p></body></html>" + (char) 10 + (char) 13;
        stringBuffer.append("Content-Length: " + html.getBytes().length + "\n");
        stringBuffer.append("Content-Type: text/html; charset=UTF-8" + (char) 10 + (char) 13);
        stringBuffer.append("Connection: closed" + (char)10 + (char)13);
        stringBuffer.append("" + (char) 10 + (char) 13);

        stringBuffer.append(html);

        outputStream.write(stringBuffer.toString().getBytes());

        System.out.println(stringBuffer.toString());

        System.out.println("post end");
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

    public HashMap<String, String> parseFileMeta(String line, HashMap<String, String> fileMeta) {
        String[] lineArr = line.split(";");
        for (String e : lineArr) {
            String[] eArr = e.split("=");
            if (eArr.length == 1) {
                eArr = e.split(":");
            }
            fileMeta.put(eArr[0].trim(), eArr[1].replaceAll("\"", "").trim());
        }

        return fileMeta;
    }
}
