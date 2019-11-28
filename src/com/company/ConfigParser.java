package com.company;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;

public class ConfigParser {

    public HashMap<String, HashMap> parse(String configFile) {
        File file = new File(configFile);
        // 一般多大capacity合适？
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024 * 1024);
        try {
            FileInputStream fileInputStream = new FileInputStream(file);

            byte[] buff = new byte[1024];
            while (fileInputStream.read(buff) != -1) {
                byteBuffer.put(buff);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        byteBuffer.flip();
        byte[] content = new byte[byteBuffer.remaining()];
        byteBuffer.get(content);
        String fileContent = new String(content);
        JSONObject jsonobject = JSON.parseObject(fileContent);

        JSONArray features = jsonobject.getJSONArray("http");//解析的是json数组
        HashMap<String, HashMap> config = new HashMap<>();
        for (int i = 0; i < features.size(); i++) {
            HashMap<String, String> serverConfig = new HashMap<>();
            String listen = features.getJSONObject(i).getString("listen");
            String host = features.getJSONObject(i).getString("server_name");
            String root = features.getJSONObject(i).getString("root");
            String proxyPass = features.getJSONObject(i).getString("proxy_pass");
//            todo 解析嵌套json
            String index = features.getJSONObject(i).getString("index");
            serverConfig.put("host", host);
            serverConfig.put("port", listen);
            serverConfig.put("root", root);
            serverConfig.put("proxy_pass", proxyPass);
            config.put(host, serverConfig);
        }

        return config;
    }
}
