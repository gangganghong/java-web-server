package com.company;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Test {
    private String webServerRoot = "/Users/cg/data/www/cg/tool";

    public HashMap<String, String> run(HashMap<String, HashMap> header, byte[] dataFromWebServer) throws Exception {
        Socket client = new Socket("127.0.0.1", 9000);

        InputStream in = client.getInputStream();
        OutputStream out = client.getOutputStream();

        // protocol sequence
        int request_id = 5678;
        //1.begin request
        byte[] begin_request_body = new byte[8];
        begin_request_body[0] = 0; // roleB1
        begin_request_body[1] = fcgi_role.FCGI_RESPONDER; //roleB0
        begin_request_body[2] = 0; // flags
        byte[] begin_request = fcgi.fcgiPacket(fcgi_request_type.FCGI_BEGIN_REQUEST, request_id, begin_request_body);
        System.out.println("FCGI_BEGIN_REQUEST:\n" + Base64.getEncoder().encodeToString(begin_request));
        //2.params
        String entity2 = "POST /post.html HTTP/1.1\n" +
                "Host: localhost:2000\n" +
                "Connection: keep-alive\n" +
                "Content-Length: 254195\n" +
                "Pragma: no-cache\n" +
                "Cache-Control: no-cache\n" +
                "Origin: http://dev.cg.com\n" +
                "Upgrade-Insecure-Requests: 1\n" +
                "Content-Type: multipart/form-data; boundary=----WebKitFormBoundaryIqmAOK1WzKVEtB5o\n" +
                "User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.108 Safari/537.36\n" +
                "Sec-Fetch-User: ?1\n" +
                "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3\n" +
                "Sec-Fetch-Site: cross-site\n" +
                "Sec-Fetch-Mode: navigate\n" +
                "Referer: http://dev.cg.com/tool/form.html\n" +
                "Accept-Encoding: gzip, deflate, br\n" +
                "Accept-Language: zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7\n" +
                "\n" +
                "------WebKitFormBoundaryIqmAOK1WzKVEtB5o\n" +
                "Content-Disposition: form-data; name=\"name\"\n" +
                "\n" +
                "ç®¡ç\u0090†å‘˜\n" +
                "------WebKitFormBoundaryIqmAOK1WzKVEtB5o\n" +
                "Content-Disposition: form-data; name=\"desc\"\n" +
                "\n" +
                "å\u0090¾é—®æ— ä¸ºè°“\n" +
                "------WebKitFormBoundaryIqmAOK1WzKVEtB5o\n" +
                "Content-Disposition: form-data; name=\"picture\"; filename=\"2015110214261032002.jpg\"\n" +
                "Content-Type: image/jpeg";
//        String postData = new String("\r\n------we\r\n" +
//                "Content-Disposition: form-data; name=\"nameer\"\r\n------234");
//        String postData = new String("Content-Disposition: form-data;name=\"name\"\r\n------2345WebKitFormBoundaryIqmAOK1WzKVEtB5o--");
        byte[] postDatByte = dataFromWebServer;
        HashMap<String, String> requestHeaders = header.get("requestLine");
        HashMap<String, String> headLine = header.get("headerLine");
        String uri = requestHeaders.get("Uri");
        String contentType = headLine.get("Content-Type");
        String contentLength = headLine.get("Content-Length");

        Map<String, String> params = new LinkedHashMap<>();
        params.put("GATEWAY_INTERFACE", "FastCGI/1.0");
        params.put("REQUEST_METHOD", requestHeaders.get("Method"));
        params.put("SCRIPT_FILENAME", webServerRoot + uri);
        params.put("SCRIPT_NAME", uri);
        params.put("QUERY_STRING", "");
        params.put("REQUEST_URI", uri);
        params.put("DOCUMENT_URI", uri);
        params.put("SERVER_SOFTWARE", "php/fcgiclient");
        params.put("REMOTE_ADDR", "127.0.0.1");
        params.put("REMOTE_PORT", "9985");
        params.put("SERVER_ADDR", "127.0.0.1");
        params.put("SERVER_PORT", "80");
        params.put("SERVER_NAME", "DESKTOP-NCL22GF");
        params.put("SERVER_PROTOCOL", "HTTP/1.1");
        params.put("CONTENT_TYPE", contentType == null ? "" : contentType);
//        params.put("CONTENT_TYPE", "application/x-www-form-urlencoded");

        params.put("CONTENT_LENGTH", "" + (contentLength == null ? 0 : contentLength));
        params.put("AUTHOR", "cg");

        System.out.println("\nparam pair base64:");
        ByteBuffer paramContainer = ByteBuffer.allocate(1024);
        for (String key : params.keySet()) {
            byte[] onePair = fcgi.fcgiParam(key, params.get(key));
            paramContainer.put(onePair);

            System.out.println(key + " - " + params.get(key) + ": " + Base64.getEncoder().encodeToString(onePair));
        }
        paramContainer.flip();
        byte[] fcgi_param_byte = new byte[paramContainer.remaining()];
        paramContainer.get(fcgi_param_byte);
        byte[] fcgi_params = fcgi.fcgiPacket(fcgi_request_type.FCGI_PARAMS, request_id, fcgi_param_byte);

        System.out.println("\nFCGI_PARAMS:\n" + Base64.getEncoder().encodeToString(fcgi_params));
        byte[] fcgi_params_end = fcgi.fcgiPacket(fcgi_request_type.FCGI_PARAMS, request_id, new byte[0]);
        System.out.println("\nFCGI_PARAMS end with empty content:\n" + Base64.getEncoder().encodeToString(fcgi_params_end));
        //3.stdin
        byte[] fcgi_stdin = fcgi.fcgiPacket(fcgi_request_type.FCGI_STDIN, request_id, postDatByte);
        System.out.println("\nFCGI_STDIN:\n" + Base64.getEncoder().encodeToString(fcgi_stdin));

        byte[] end_request_body = new byte[8];
        end_request_body[0] = (byte) ((0 >> 24) & 0xff);
        end_request_body[1] = (byte) ((0 >> 16) & 0xff);
        end_request_body[2] = (byte) ((0 >> 8) & 0xff);
        end_request_body[3] = (byte) (0 & 0xff);
        end_request_body[4] = (byte) 0;
        byte[] end_request = fcgi.fcgiPacket(fcgi_request_type.FCGI_END_REQUEST, request_id, end_request_body);
        ByteBuffer onePacket = ByteBuffer.allocate(begin_request.length + fcgi_params.length +
                fcgi_params_end.length + fcgi_stdin.length + end_request.length);
        onePacket.put(begin_request);
        onePacket.put(fcgi_params);
        onePacket.put(fcgi_params_end);
        onePacket.put(fcgi_stdin);
        onePacket.put(end_request);

        onePacket.flip();
        System.out.println("\nall:\n" + onePacket);

        out.write(onePacket.array());

        System.out.println("\noutput:");
        byte[] buf = new byte[8];

        boolean startContent = false;
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024*1024);
        while (in.read(buf) != -1) {
            if(!startContent && (buf[1] == 5 || buf[1] == 6 || buf[1] == 7)){
                startContent = true;
                continue;
            }

            if(startContent && buf[1] == 3){
                startContent = false;
            }

            if(startContent){
                byteBuffer.put(buf);
            }

        }
        System.out.println("from php start");
//        这个方法，留意
        byteBuffer.flip();
        byte[] data = new byte[byteBuffer.remaining()];
        byteBuffer.get(data);

        String resultFromPHP = new String(data, "UTF-8");
        System.out.println(resultFromPHP);
        System.out.println("from php end\t结束");
        int headerEndIndex = resultFromPHP.indexOf("\r\n\r\n");
        String headerStr = resultFromPHP.substring(0, headerEndIndex);
        String[] headerArr = headerStr.split("\r\n");

        String httpStatus = null;
        String powered;
        String phpDataContentType;

        if(headerArr.length > 2){
//            Primary script unknown\nStatus: 404 Not Found
            httpStatus = headerArr[0].split(":")[1];
            powered = headerArr[1];
            phpDataContentType = headerArr[2].split(":")[1];
//            phpDataContentType = headerArr[3].split(":")[1];
        }else{
            powered = headerArr[0];
            phpDataContentType = headerArr[1].split(":")[1];
        }

        String content = resultFromPHP.substring(headerEndIndex + 1);
        HashMap<String, String> result = new HashMap<>();
        result.put("httpStatus", httpStatus);
        result.put("powered", powered);
        result.put("ContentType", phpDataContentType);
        result.put("content", content);

        return result;
    }
}

class fcgi {
    static byte VERSION = 1;

    public static byte[] fcgiPacket(byte type, int id, byte[] content) {
        //header
        byte[] header = new byte[8];
        header[0] = fcgi.VERSION;
        header[1] = type;
        header[2] = (byte) ((id >> 8) & 0xff);//requestIdB1
        header[3] = (byte) (id & 0xff); // requestIdB0, big endian
        header[4] = (byte) ((content.length >> 8) & 0xff);//contentLengthB1
        header[5] = (byte) (content.length & 0xff); //contentLengthB0
        header[6] = 0; //padding length
        header[7] = 0; //reserved

        //combine header and content to one byte[]
        byte[] packet = new byte[header.length + content.length];
        System.arraycopy(header, 0, packet, 0, header.length);
        System.arraycopy(content, 0, packet, header.length, content.length);

        return packet;
    }

    public static byte[] fcgiParam(String name, String value) {
        int nameLength = name.length();
        int valueLength = value.length();
        byte[] nameLen, valueLen;
        nameLen = getBytes(nameLength);
        valueLen = getBytes(valueLength);
        byte[] onePair = new byte[nameLen.length + valueLen.length + name.getBytes().length + value.getBytes().length];
        System.arraycopy(nameLen, 0,
                onePair, 0, nameLen.length);
        System.arraycopy(valueLen, 0,
                onePair, nameLen.length, valueLen.length);
        System.arraycopy(name.getBytes(), 0,
                onePair, nameLen.length + valueLen.length, name.getBytes().length);
        System.arraycopy(value.getBytes(), 0,
                onePair, nameLen.length + valueLen.length + name.getBytes().length, value.getBytes().length);
        return onePair;
    }

    private static byte[] getBytes(int nameLength) {
        byte[] nameLen;
        if (nameLength < 128) {
            nameLen = new byte[1];
            nameLen[0] = (byte) nameLength;
        } else {
            nameLen = new byte[4];
            nameLen[0] = (byte) ((nameLength >> 24) & 0xff);
            nameLen[1] = (byte) ((nameLength >> 16) & 0xff);
            nameLen[2] = (byte) ((nameLength >> 8) & 0xff);
            nameLen[3] = (byte) (nameLength & 0xff);
        }
        return nameLen;
    }
}

class fcgi_request_type {
    static byte FCGI_BEGIN_REQUEST = 1;
    static byte FCGI_ABORT_REQUEST = 2;
    static byte FCGI_END_REQUEST = 3;
    static byte FCGI_PARAMS = 4;
    static byte FCGI_STDIN = 5;
    static byte FCGI_STDOUT = 6;
    static byte FCGI_STDERR = 7;
    static byte FCGI_DATA = 8;
    static byte FCGI_GET_VALUES = 9;
    static byte FCGI_GET_VALUES_RESULT = 10;
}

class fcgi_role {
    static byte FCGI_RESPONDER = 1;
    static byte FCGI_AUTHORIZER = 2;
    static byte FCGI_FILTER = 3;
}
