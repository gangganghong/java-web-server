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
            int bufferLen;

            FileOutputStream fileOutputStream = null;
            FileOutputStream fileOutputStream1 = new FileOutputStream(webServerRoot + "/log" + System.currentTimeMillis());


            boolean in = false;
            StringBuffer stringBuffer = new StringBuffer();
            String boundary = new String();
            boolean startBinary = false;
            int preAsciiCode = 0;
            int prePreAsciiCode = 0;
            boolean startFile = false;
            HashMap<String, HashMap> fileMetas = new HashMap<>();
            HashMap<String, String> fileMeta = new HashMap<>();

            while ((bufferLen = inputStream.read(buffer)) != -1) {

                fileOutputStream1.write(buffer);

                String lineStr = new String();

//                字符是A-Z之间（包含头尾）或-，是一行字符串的开头，开始暂存字符串
                String line = new String(buffer, 0, bufferLen);
                int asciiCode = Integer.parseInt(stringToAscii(line));
                if (in == false && ((65 <= asciiCode && asciiCode <= 90) || asciiCode == 45)) {
                    in = true;
                }
//                字符是换行符，是一行字符串的结尾，获取一行字符串，初始化暂存一行字符串的StringBuffer
                if (in == true && asciiCode == 10) {
                    in = false;
                    lineStr = stringBuffer.toString();
                    stringBuffer = new StringBuffer();
                }
//                暂存字符串
                if (in) {
                    stringBuffer.append(line);
                }

//                获取multipart表单的boundary
                if (lineStr.startsWith("Content-Type")) {
                    String[] lienArr = lineStr.split(";");
                    if (lienArr[0].endsWith("multipart/form-data")) {
                        boundary = lienArr[1].substring(lienArr[1].indexOf("=") + 1);
                    }
                }
//                进入二进制数据部分
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

//                    字符串为 【换行 + 回车 + 换行】 时，接下来的数据是 文件数据
                    if ((startFile == false) && (asciiCode == 10 && preAsciiCode == 13 && prePreAsciiCode == 10)) {

                        startFile = true;

                        prePreAsciiCode = preAsciiCode;
                        preAsciiCode = asciiCode;

                        fileOutputStream = new FileOutputStream(webServerRoot + "/tmp" + System.currentTimeMillis());

                        continue;
                    }
//                    字符串为 【回车 + 换行 + boundary】 时，即再次遇到了 boundary，一个文件的数据全部接收完毕
                    if (prePreAsciiCode == 13 && preAsciiCode == 10 && asciiCode == 45) {
                        startFile = false;
                        fileOutputStream.close();
                    }
                }
//                接收的是文件数据，将文件存储
                if (startFile) {
                    fileOutputStream.write(buffer);
                }

//                字符串为 【- + 回车 + 换行】 时，即最后一个 boundary，文件数据全部接收完毕。
//                一定要在此时 给出响应信息 + 关闭对应的读写字节流，否则，浏览器会一直处于 pending 状态。
//                耗时很久，才灵光乍现，猜想浏览器一直 pending 的原因是在接收完全部数据后，未立刻进行上述操作。
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

//            fileOutputStream1.close();
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

        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("HTTP/1.1 200 OK\n");
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
