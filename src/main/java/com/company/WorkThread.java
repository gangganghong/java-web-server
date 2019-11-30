package com.company;


import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.StringTokenizer;

public class WorkThread extends Thread{
    private String webServerRoot;
    private Socket socket;
    private HashMap<String, String> serverConfig;

    public WorkThread() {

    }

    public WorkThread(Socket socket, HashMap<String, String> serverConfig) {
        this.socket = socket;
        this.serverConfig = serverConfig;
        webServerRoot = serverConfig.get("root");
    }

//        错误
//        public void WorkThread(Socket socket){
//            this.socket = socket;
//        }
    @Override
    public void run() {
        try {
            System.out.println("socket info start");
            System.out.println(socket.getPort());
            System.out.println("socket info end");

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
            boolean startHead = false;
            boolean endHead = false;
            boolean hasBoundary = false;
            HashMap<String, HashMap> fileMetas = new HashMap<>();
            HashMap<String, String> fileMeta = new HashMap<>();

            StringBuffer headerBuffer = new StringBuffer();

            HashMap<String, HashMap> header = null;

            ByteBuffer allDataTmp = ByteBuffer.allocate(1024 * 1024);

            while ((bufferLen = inputStream.read(buffer)) != -1) {
                allDataTmp.put(buffer);

                fileOutputStream1.write(buffer);

                String lineStr = new String();
                String line = new String(buffer, 0, bufferLen);
                int asciiCode = Integer.parseInt(stringToAscii(line));

                if (!startHead && (65 <= asciiCode && asciiCode <= 90)) {
                    startHead = true;
                }

                if (!endHead && (prePreAsciiCode == 13 && preAsciiCode == 10 && asciiCode == 13)) {
                    endHead = true;
                }

                if (startHead && !endHead) {
                    headerBuffer.append(line);
//                    错误写法
//                    headerBuffer.append(buffer);
                }

                if (endHead) {
                    header = getRequestHeaders(headerBuffer.toString());
                }

//                String method = null;


//                字符是A-Z之间（包含头尾）或-，是一行字符串的开头，开始暂存字符串
                if (in == false && ((65 <= asciiCode && asciiCode <= 90) || asciiCode == 45)) {
                    in = true;
                }
//                字符是换行符或回车符，是一行字符串的结尾，获取一行字符串，初始化暂存一行字符串的StringBuffer
                if (in == true && asciiCode == 13) {
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
                    hasBoundary = true;
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
                if (isEnd(asciiCode, preAsciiCode, prePreAsciiCode, hasBoundary)) {
                    allDataTmp.flip();
                    byte[] allData = new byte[allDataTmp.remaining()];
                    allDataTmp.get(allData);
                    String proxyPass = serverConfig.get("proxy_pass");
                    System.out.println("proxy start");
                    System.out.println(Thread.currentThread().getName() + "\t" + proxyPass);
                    System.out.println("proxy end");
                    if (proxyPass != null) {
                        proxy(allData, socket, proxyPass, header);
                    } else {
                        doRequest(header, allData);
                    }
                    break;
                }

                prePreAsciiCode = preAsciiCode;
                preAsciiCode = asciiCode;
            }

            inputStream.close();
            socket.close();

            System.out.println("结束");
            System.out.println(fileMetas);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doRequest(HashMap<String, HashMap> header, byte[] data) throws IOException {

        if (header == null || header.isEmpty()) return;

        HashMap<String, String> requestLine = header.get("requestLine");
        HashMap<String, String> headerLine = header.get("headerLine");
        StringBuffer stringBuffer = new StringBuffer();
        OutputStream outputStream = socket.getOutputStream();
        String hostAndPort = headerLine.get("Host");
        String host = hostAndPort.substring(0, hostAndPort.indexOf(":"));
        String serverName = serverConfig.get("host");
        if (!host.trim().equals(serverName.trim())) {
            int contentLength = response404(stringBuffer, outputStream);

            saveAccessLog(requestLine, headerLine, host, 404 + "", String.valueOf(contentLength));

            return;
        }

        String method;
        String uri;

        method = requestLine.get("Method");
        uri = requestLine.get("Uri");
        if (method == null) return;

        HashMap<String, String> dataFromPHP = null;
        if (isDynamicRequest(uri)) {
            Test test = new Test();
            try {
                dataFromPHP = test.run(header, data, webServerRoot);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        switch (method) {
            case "POST":
                doPost(dataFromPHP, requestLine, headerLine, host);
                break;
            case "GET":
            default:
                doGet(uri, dataFromPHP, requestLine, headerLine, host);
        }
    }

    private void doPost(
            HashMap<String, String> dataFromPHP,
            HashMap<String, String> requestLine,
            HashMap<String, String> headerLine,
            String host
    ) throws IOException {
        String httpStatus = null;
        String html = null;
        String contenType = "";
        String httpCode = "200";
        if (dataFromPHP != null) {
            html = dataFromPHP.get("content");
            contenType = dataFromPHP.get("ContentType");
            httpStatus = dataFromPHP.get("httpStatus");
            String[] httpStatusArr = httpStatus.split(" ");
            if (httpStatusArr.length > 0) {
                httpCode = httpStatusArr[0];
            }
        }

        if (httpStatus == null) {
            httpStatus = httpCode + " OK";
        }
        int contentLength = html.getBytes().length;
        String lineFlag = "" + (char) 10 + (char) 13;
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("HTTP/1.1 " + httpStatus + lineFlag);
        stringBuffer.append("Content-Length: " + contentLength + lineFlag);
        stringBuffer.append("Content-Type: " + contenType + lineFlag);
        stringBuffer.append("Connection: closed" + lineFlag);
        stringBuffer.append("" + lineFlag);
        stringBuffer.append(html == null ? "" : html);

        OutputStream outputStream = socket.getOutputStream();
        outputStream.write(stringBuffer.toString().getBytes());
        outputStream.flush();

        saveAccessLog(requestLine, headerLine, host, httpCode + "", String.valueOf(contentLength));
    }

    private void doGet(String filename,
                       HashMap<String, String> dataFromPHP,
                       HashMap<String, String> requestLine,
                       HashMap<String, String> headerLine,
                       String host
    ) throws IOException {
        System.out.println("GET 请求\t" + filename);

        StringBuffer stringBuffer = new StringBuffer();
        OutputStream outputStream = socket.getOutputStream();

        if (dataFromPHP == null) {
            String fileType = getFileType(filename);

            // 读取磁盘文件
            String absolutePath = webServerRoot + filename;
            File file = new File(absolutePath);
            if (file.isDirectory()) {
                int httpCode = 200;
                String indexesHtml = getRootIndexHtml(absolutePath, filename);
                String html = "HTTP/1.1 " + httpCode + " OK\r\nContent-Type: text/html;charset=UTF-8\r\n";
                int contentLength = indexesHtml.length();
                html += "Content-Length: " + contentLength + "\r\n";
                html += "Connection: closed\r\n\r\n";
                html += indexesHtml;
                OutputStream outputStream1 = socket.getOutputStream();
                outputStream1.write(html.getBytes());

                saveAccessLog(requestLine, headerLine, host, httpCode + "", String.valueOf(contentLength));

                return;
            }
            if (filename != null && file.exists()) {
                int httpCode = 200;
                stringBuffer.append("HTTP/1.1 " + httpCode + " OK\n");

                stringBuffer.append("Content-Type: " + fileType + "; charset=UTF-8\n");
                stringBuffer.append("Connection: closed\n");
                Long contentLength = file.length();
                stringBuffer.append("Content-Length: " + contentLength + "\n");

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
                dataOutputStream.close();

                saveAccessLog(requestLine, headerLine, host, httpCode + "", String.valueOf(contentLength));

            } else {
                int contentLength = response404(stringBuffer, outputStream);
                outputStream.close();

                int httpCode = 404;

                saveAccessLog(requestLine, headerLine, host, httpCode + "", String.valueOf(contentLength));
            }
        } else {
            String html = dataFromPHP.get("content");
            String contentType = dataFromPHP.get("ContentType");

            String httpStatus = dataFromPHP.get("httpStatus");
            if (httpStatus == null) {
                httpStatus = "HTTP/1.1 200 OK";
            }

            int contentLength = html.getBytes().length;
            stringBuffer.append("HTTP/1.1 " + httpStatus + "\r\n");
            stringBuffer.append("Content-Length:" + contentLength + "\r\n");
            stringBuffer.append("Content-Type:" + contentType + "\r\n");
            stringBuffer.append("\r\n");
            stringBuffer.append(html);
            outputStream.write(stringBuffer.toString().getBytes());

            int httpCode = 404;

            saveAccessLog(requestLine, headerLine, host, httpCode + "", String.valueOf(contentLength));
        }
    }

    private void saveAccessLog(HashMap<String, String> requestLine, HashMap<String, String> headerLine, String host, String s, String s2) {
        HashMap<String, String> logContent = new HashMap<>();
        logContent.put("host", host);
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YY-MM-D H:M:s");
        logContent.put("date", simpleDateFormat.format(date));
        logContent.put("requestLine", requestLine.get("Method") + " " + requestLine.get("Uri") + " " + requestLine.get("Http").replaceAll("\r", ""));
        logContent.put("httpCode", s);
        logContent.put("contentLength", s2);
        logContent.put("userAgent", headerLine.get("User-Agent"));
        String accessLog = serverConfig.get("access_log");
        ServerLog.accessLog(logContent, accessLog);
    }

    private int response404(StringBuffer stringBuffer, OutputStream outputStream) throws IOException {
        String notFoundHtml = "<html>\n" +
                "<head><title>404 Not Found</title></head>\n" +
                "<body>\n" +
                "<center><h1>404 Not Found</h1></center>\n" +
                "<hr><center>cg-java-web-server/1.0</center>\n" +
                "</body>\n" +
                "</html>\n" +
                "<!-- a padding to disable MSIE and Chrome friendly error page -->\n" +
                "<!-- a padding to disable MSIE and Chrome friendly error page -->\n" +
                "<!-- a padding to disable MSIE and Chrome friendly error page -->\n" +
                "<!-- a padding to disable MSIE and Chrome friendly error page -->\n" +
                "<!-- a padding to disable MSIE and Chrome friendly error page -->\n" +
                "<!-- a padding to disable MSIE and Chrome friendly error page -->";
        stringBuffer.append("HTTP/1.1 404 Not Found\r\n");
        int contentLength = notFoundHtml.getBytes().length;
        stringBuffer.append("Content-Length:" + contentLength + "\r\n");
        stringBuffer.append("\r\n");
        stringBuffer.append(notFoundHtml);
        outputStream.write(stringBuffer.toString().getBytes());

        return contentLength;
    }

    // todo 比较专业和繁琐，先简化处理
    private String getFileType(String filePath) {
        String fileType = "text/plain";

        if (filePath.endsWith("html")) {
            fileType = "text/html";
        } else {
//            fileType = "image/jpeg";
        }

        return fileType;
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

    public HashMap<String, String> parseHeadLines(String requestHeaderStr) {
        HashMap<String, String> parsedHeadLines = new HashMap<>();

        String delim = "" + (char) 13 + (char) 10;
        StringTokenizer stringTokenizer = new StringTokenizer(requestHeaderStr, delim);
        int i = 0;
        while (stringTokenizer.hasMoreElements()) {
            if (i == 0) {
                i++;
                stringTokenizer.nextElement();
                continue;
            }
            String headLine = stringTokenizer.nextElement().toString();
            String key = headLine.substring(0, headLine.indexOf(":")).replaceAll(" ", "");
            String value = headLine.substring(headLine.indexOf(":") + 1).replaceAll(" ", "");
            parsedHeadLines.put(key, value);
        }

        return parsedHeadLines;
    }

    public HashMap<String, String> parseRequestLine(String requestHeaderStr) {
        HashMap<String, String> parsedRequestLine = new HashMap<>();
        String delim = "" + (char) 10;
        String requestLineStr = requestHeaderStr.substring(0, requestHeaderStr.indexOf(delim));
        String[] requestLineArr = requestLineStr.split(" ");
        parsedRequestLine.put("Method", requestLineArr[0]);
        parsedRequestLine.put("Uri", requestLineArr[1]);
        parsedRequestLine.put("Http", requestLineArr[2]);

        return parsedRequestLine;
    }

    public HashMap<String, HashMap> getRequestHeaders(String requestHeaderStr) {
        HashMap requestLine = parseRequestLine(requestHeaderStr);
        HashMap headLines = parseHeadLines(requestHeaderStr);
        HashMap<String, HashMap> requestHeaders = new HashMap<>();
        requestHeaders.put("requestLine", requestLine);
        requestHeaders.put("headerLine", headLines);

        return requestHeaders;
    }

    private boolean isEnd(int asciiCode, int preAsciiCode, int prePreAsciiCode, boolean hasBoundary) {

        if (asciiCode * preAsciiCode * prePreAsciiCode == 0) {
            return false;
        }

        if (hasBoundary) {
            return (asciiCode == 10 && preAsciiCode == 13 && prePreAsciiCode == 45);
        } else {
            return (asciiCode == 10 && preAsciiCode == 13 && prePreAsciiCode == 10);
        }
    }

    public boolean isDynamicRequest(String uri) {
        String lowCaseUri = uri.toLowerCase();
        return lowCaseUri.contains(".php");
    }

    // 反向代理功能
    private void proxy(
            byte[] requestData,
            Socket originalSocket,
            String proxyPass,
            HashMap<String, HashMap> header
    ) throws IOException {

        if (header == null || header.isEmpty()) return;

        HashMap<String, String> requestLine = header.get("requestLine");
        HashMap<String, String> headerLine = header.get("headerLine");

        String[] hostAndPort = proxyPass.split(":");
        String host = hostAndPort[0];
        int port;
        if (hostAndPort.length > 1) {
            port = Integer.parseInt(hostAndPort[1]);
        } else {
            port = 80;
        }

        try {
            Socket socket = new Socket();
            SocketAddress socketAddress = new InetSocketAddress(host, port);
            socket.connect(socketAddress, 100);
            InputStream inputStream = socket.getInputStream();

            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(requestData);

            byte[] buff = new byte[1024];
            OutputStream outputStream1 = originalSocket.getOutputStream();
            while (inputStream.read(buff) != -1) {
                outputStream1.write(buff);
            }

            // todo 并不是这两个值，正确的值需要从php-fpm返回的数据中提取
            int contentLength = 100;
            int httpCode = 200;

            saveAccessLog(requestLine, headerLine, host, httpCode + "", String.valueOf(contentLength));

        } catch (Exception e) {
            // todo 有待调整，代理服务器的问题，归结为502，更具有概括性
            e.printStackTrace();

            int contentLength = response504();

            int httpCode = 504;

            saveAccessLog(requestLine, headerLine, host, httpCode + "", String.valueOf(contentLength));

            return;
        }
    }

    private int response504() {
        String html = "<html>\n" +
                "<head><title>504 Gateway timeout</title></head>\n" +
                "<body>\n" +
                "<center><h1>504 Gateway timeou</h1></center>\n" +
                "<hr><center>cg-java-web-server/1.0</center>\n" +
                "</body>\n" +
                "</html>\n" +
                "<!-- a padding to disable MSIE and Chrome friendly error page -->\n" +
                "<!-- a padding to disable MSIE and Chrome friendly error page -->\n" +
                "<!-- a padding to disable MSIE and Chrome friendly error page -->\n" +
                "<!-- a padding to disable MSIE and Chrome friendly error page -->\n" +
                "<!-- a padding to disable MSIE and Chrome friendly error page -->\n" +
                "<!-- a padding to disable MSIE and Chrome friendly error page -->";

        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("HTTP/1.1 504 Gateway timeout\r\n");
        int contentLength = html.getBytes().length;
        stringBuffer.append("Content-Length:" + contentLength + "\r\n");
        stringBuffer.append("\r\n");
        stringBuffer.append(html);
        OutputStream outputStream;
        try {
            outputStream = socket.getOutputStream();
            outputStream.write(stringBuffer.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return contentLength;
    }

    private ArrayList<HashMap> showIndex(String dirname) {
        File dir = new File(dirname);
        File[] files = dir.listFiles();
        ArrayList<HashMap> indexes = new ArrayList<>();

        for (File file : files) {
            HashMap<String, String> fileMeta = new HashMap<>();
            String name = file.getName();
            String lastModified = formateFileLastModified(file.lastModified());
            String length;

            if (file.isDirectory()) {
                length = "-";
            } else {
                length = Long.toString(file.length());
            }
            String[] nameArr = name.split("/");
            int nameArrLenth = nameArr.length;
            String nameWithoutPath = "";

            if (nameArrLenth >= 1) {
                nameWithoutPath = nameArr[nameArr.length - 1];
            }

            fileMeta.put("name", nameWithoutPath);
            fileMeta.put("lastModified", lastModified);
            fileMeta.put("length", length);

            indexes.add(fileMeta);
        }

        return indexes;
    }

    private String getRootIndexHtml(String dirname, String uri) {

        String formatedUri = formatUri(uri);

        ArrayList<HashMap> indexes = showIndex(dirname);
        String html = "<html>\n" +
                "<head><title>Index of /</title></head>\n" +
                "<body>\n<h1>Index of /</h1><hr><pre><a href=\"../\">../</a>\n";
        for (HashMap<String, String> index : indexes) {
            String name = index.get("name");
            String fullName = formatedUri + name;
            String lastModified = index.get("lastModified");
            String length = index.get("length");
            html += "<a href=\"" + fullName + "\">" + (name.replaceAll("/", "")) + "</a>"
                    + "             " + lastModified + "            " + length + "\n";
        }

        html += "</pre><hr></body>\n</html>";

        return html;
    }

    private String formatUri(String uri) {
        byte[] uriArr = uri.getBytes();
        int uriLength = uriArr.length;
        String suffix = "";
        while (uriLength >= 1) {
            byte element = uriArr[uriLength - 1];
            if (element != '/') {
                break;
            } else {
                suffix += '/';
                uriLength--;
            }
        }

        String formatedUri = uri;
        if (!suffix.equals("")) {
            formatedUri = uri.replaceAll(suffix, "/");
        }

        if (!formatedUri.endsWith("/")) {
            formatedUri += "/";
        }
        return formatedUri;
    }

    private String formateFileLastModified(long lastModified) {
        Date date = new Date();
        date.setTime(lastModified);
//        12-Nov-2018 16:51
        String strPattern = "d-MM-Y h:m:s";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(strPattern);
        String formatedFileLastModified = simpleDateFormat.format(date);

        return formatedFileLastModified;
    }
}
