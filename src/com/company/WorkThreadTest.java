package com.company;

import java.net.Socket;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class WorkThreadTest {

    @org.junit.jupiter.api.Test
    void run() {
    }

    @org.junit.jupiter.api.Test
    void stringToAscii() {
    }

    @org.junit.jupiter.api.Test
    void parseFileMeta() {
        String line = "Content-Disposition: form-data; name=\"picture\"; filename=\"form.html\"";
        String line2 = "Content-Type: image/jpeg";
        HashMap<String, String> expectedFileMeta = new HashMap<>();
        HashMap<String, String> actualFileMeta = new HashMap<>();
        expectedFileMeta.put("Content-Disposition", "form-data");
        expectedFileMeta.put("name", "picture");
        expectedFileMeta.put("filename", "form.html");
        expectedFileMeta.put("Content-Type", "image/jpeg");
        WorkThread workThread = new WorkThread(new Socket());
        actualFileMeta = workThread.parseFileMeta(line, actualFileMeta);
        actualFileMeta = workThread.parseFileMeta(line2, actualFileMeta);
        assertEquals(expectedFileMeta, actualFileMeta);



//        Content-Type: text/html
    }
}