package com.company;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class WorkThreadTest {

    WorkThread workThread = new WorkThread();

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
//        WorkThread workThread = new WorkThread(new Socket());
        actualFileMeta = workThread.parseFileMeta(line, actualFileMeta);
        actualFileMeta = workThread.parseFileMeta(line2, actualFileMeta);
        assertEquals(expectedFileMeta, actualFileMeta);


//        Content-Type: text/html
    }

    @Test
    void parseHeadLines() {
        String headLines = "POST /post.html HTTP/1.1\n" +
                "Host: localhost:2000\n" +
                "Connection: keep-alive\n";
//                "Content-Length: 432254\n" +
//                "Pragma: no-cache\n" +
//                "Cache-Control: no-cache\n" +
//                "Origin: http://dev.cg.com\n" +
//                "Upgrade-Insecure-Requests: 1\n" +
//                "Content-Type: multipart/form-data; boundary=----WebKitFormBoundarylsEWADr9iLjfKoRu\n" +
//                "User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.108 Safari/537.36\n" +
//                "Sec-Fetch-User: ?1\n" +
//                "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3\n" +
//                "Sec-Fetch-Site: cross-site\n" +
//                "Sec-Fetch-Mode: navigate\n" +
//                "Referer: http://dev.cg.com/tool/form.html\n" +
//                "Accept-Encoding: gzip, deflate, br\n" +
//                "Accept-Language: zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7";

        HashMap<String, String> expectedResult = new HashMap<>();
        expectedResult.put("Host", "localhost:2000");
        expectedResult.put("Connection", "keep-alive");
        HashMap<String, String> acutalResult = workThread.parseHeadLines(headLines);
        assertEquals(expectedResult, acutalResult);
    }

    @Test
    void parseRequestLine() {
        String headLines = "POST /post.html HTTP/1.1\n" +
                "Host: localhost:2000\n" +
                "Connection: keep-alive\n";
//                "Content-Length: 432254\n" +
//                "Pragma: no-cache\n" +
//                "Cache-Control: no-cache\n" +
//                "Origin: http://dev.cg.com\n" +
//                "Upgrade-Insecure-Requests: 1\n" +
//                "Content-Type: multipart/form-data; boundary=----WebKitFormBoundarylsEWADr9iLjfKoRu\n" +
//                "User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.108 Safari/537.36\n" +
//                "Sec-Fetch-User: ?1\n" +
//                "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3\n" +
//                "Sec-Fetch-Site: cross-site\n" +
//                "Sec-Fetch-Mode: navigate\n" +
//                "Referer: http://dev.cg.com/tool/form.html\n" +
//                "Accept-Encoding: gzip, deflate, br\n" +
//                "Accept-Language: zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7";

        HashMap<String, String> expectedResult = new HashMap<>();
        expectedResult.put("Method", "POST");
        expectedResult.put("Uri", "/post.html");
        expectedResult.put("Http", "HTTP/1.1");
        HashMap<String, String> acutalResult = workThread.parseRequestLine(headLines);
        assertEquals(expectedResult, acutalResult);
    }

    @Test
    void getRequestHeaders() {
        String str = new String("0");
        byte[] buff2 = str.getBytes();
        byte[] buff = new byte[1];
//        buff[0] = 0;
        buff[0] = 1;
        char a = (char) 0;
        System.out.println((char) 0);
    }

    @Test
    void isDynamicRequest() {
        String uri1 = "/post.html";
        assertEquals(false, workThread.isDynamicRequest(uri1));
        String uri2 = "/post.php";
        assertEquals(true, workThread.isDynamicRequest(uri2));
    }

    @Test
    void tool() {

//        for (int i = 0; i < features.size(); i++) {
//            String ff = features.getJSONObject(i).getString("properties");
//            JSONObject jsonObject = JSON.parseObject(ff);
//            System.out.print("cp:" + JSON.parseObject(ff).getJSONArray("cp").get(0) + " " + JSON.parseObject(ff).getJSONArray("cp").get(1)); //这一行是解析cp数组里的东西
//            System.out.print(ff + " ");
//            System.out.print("childNum:" + jsonObject.getString("childNum") + "  ");
//            System.out.print("id:" + jsonObject.getString("id") + "  ");
//            System.out.println("name:" + jsonObject.getString("name"));
//        }
    }

    public class Grade {

        private Long id;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<Student> getUsers() {
            return users;
        }

        public void setUsers(List<Student> users) {
            this.users = users;
        }

        private String name;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        private List<Student> users = new ArrayList<Student>();

        // 省略 setter、getter

        public void addStudent(Student student) {
            users.add(student);
        }

        @Override
        public String toString() {
            return "Grade{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", users=" + users +
                    '}';
        }
    }

    public class Student {

        private Long id;
        private String name;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
// 省略 setter、getter

        @Override
        public String toString() {
            return "Student{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    '}';
        }
    }
}