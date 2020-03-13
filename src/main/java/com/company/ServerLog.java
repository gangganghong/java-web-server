package com.company;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class ServerLog {

    public static void accessLog(HashMap<String, String> logContent, String logFilename) {
        String host = logContent.get("host");
        String date = logContent.get("date");

        String requestLine = logContent.get("requestLine");

        String httpCode = logContent.get("httpCode");

        String contentLength = logContent.get("contentLength");

        String userAgent = logContent.get("userAgent");


        String logFormat = "%s - - [%s] \"%s\" %s %s \"-\" \"%s\"\"-\"\"-\"-0.000-\n";
        String log = String.format(logFormat, host, date, requestLine, httpCode, contentLength, userAgent);
        FileWriter fileWriter = null;
        try {
//            File file = new File(logFilename);
//            FileOutputStream fileOutputStream = new FileOutputStream(file);
//            fileOutputStream.write(log.getBytes());
            // 文件追加
            fileWriter = new FileWriter(logFilename, true);
            fileWriter.write(log);
//            fileWriter.flush();

        } catch (Exception e) {
            e.printStackTrace();
            return;
        } finally {
            try {
                if (fileWriter != null) {
                    fileWriter.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
//        POST /upload.php HTTP/1.1
//        Host: test3.com:2003
//        Connection: keep-alive
//        Content-Length: 152244
//        Pragma: no-cache
//        Cache-Control: no-cache
//        Origin: http://test3.com:2003
//        Upgrade-Insecure-Requests: 1
//        Content-Type: multipart/form-data; boundary=----WebKitFormBoundarykXpg3ao39qHTxcvq
//        User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.108 Safari/537.36
//        Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3
//Referer: http://test3.com:2003/form.html
//Accept-Encoding: gzip, deflate
//Accept-Language: zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7
//        127.0.0.1 - - [28/Nov/2019:13:15:38 +0800] "GET / HTTP/1.1" 200 8851 "-" "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.108 Safari/537.36" "-""-"-0.000-
    }

    // todo 后期再实现，这个日志的内容构成不明确
    // 实在要明确也可以：时间 + 错误级别 + 错误内容 + client(访问者）+ server(什么）+ request uri + upstream + host
    public void errorLog() {
        String date = "";
        String level = "error";
        String content = "";
//        2019/11/29 13:23:47 [error] 47324#0: *51 FastCGI sent in stderr: "Primary script unknown" while reading response header from upstream, client: 127.0.0.1, server: dev.alg.com, request: "GET /SqlParser.php HTTP/1.1", upstream: "fastcgi://127.0.0.1:9000", host: "test2.com:2001"
    }
}
