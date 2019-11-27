package com.company;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;

public class FastCGI {

    private HashMap<String, byte[]> headerBuf = new HashMap<>();


//    // 消息类型
//    enum fcgi_request_type {
//        FCGI_BEGIN_REQUEST      = 1,
//        FCGI_ABORT_REQUEST      = 2,
//        FCGI_END_REQUEST        = 3,
//        FCGI_PARAMS             = 4,
//        FCGI_STDIN              = 5,
//        FCGI_STDOUT             = 6,
//        FCGI_STDERR             = 7,
//        FCGI_DATA               = 8,
//        FCGI_GET_VALUES         = 9,
//        FCGI_GET_VALUES_RESULT  = 10,
//        FCGI_UNKOWN_TYPE        = 11
//    };
//
//    // 服务器希望fastcgi程序充当的角色, 这里只讨论 FCGI_RESPONDER 响应器角色
//    enum fcgi_role {
//        FCGI_RESPONDER      = 1,
//        FCGI_AUTHORIZER     = 2,
//        FCGI_FILTER         = 3
//    };
//
//    //消息头
//    struct fcgi_header {
//        unsigned char version;
//        unsigned char type;
//        unsigned char requestIdB1;
//        unsigned char requestIdB0;
//        unsigned char contentLengthB1;
//        unsigned char contentLengthB0;
//        unsigned char paddingLength;
//        unsigned char reserved;
//    };

    public static void main(String[] args){
        FastCGI fastCGI = new FastCGI();
        fastCGI.getFcgiBeginRequest();
    }

    public void getFcgiBeginRequest(){

        HashMap<String, Byte> header = new HashMap<>();
        header.put("version", (byte)1);
        header.put("type", (byte)1);
        header.put("requestIdB1", (byte)1);
        header.put("requestIdB0", (byte)0);
        header.put("contentLengthB1", (byte)0);
        header.put("contentLengthB0", (byte)0);
        header.put("paddingLength", (byte)3);
        header.put("reserved", (byte)0);

        HashMap<String, Byte> beginRequestBody = new HashMap<>();
        header.put("roleB1", (byte)0);
        header.put("roleB0", (byte)0);
        header.put("flags", (byte)3);
        header.put("reserved", (byte)0);

        HashMap<String, HashMap> beginRequestRecord = new HashMap<>();
        beginRequestRecord.put("header", header);
//        beginRequestRecord.put("body", beginRequestBody);

        try{
            Socket socket = new Socket("127.0.0.1", 9000);
            OutputStream outputStream = socket.getOutputStream();
            for(String key: beginRequestRecord.keySet()){
                HashMap<String, Byte> e = beginRequestRecord.get(key);
                for(String k : e.keySet()){
                    outputStream.write(e.get(k));
                }
            }

            outputStream.flush();

            InputStream inputStream = socket.getInputStream();
            byte[] buff = new byte[4096*4];
            System.out.println(inputStream.read(buff));

            System.out.println("over");

        }catch (Exception e){
            e.printStackTrace();
        }

    }

}
