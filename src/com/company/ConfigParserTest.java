package com.company;

import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConfigParserTest {

    private ConfigParser configParser = new ConfigParser();

    @Test
    void parse() {

        HashMap<String, HashMap> config1 = new HashMap<>();
        HashMap<String, String> server1 = new HashMap<>();
        String host1 = "test.com";
        server1.put("port", "2000");
        server1.put("host", host1);
        server1.put("root", "html");
        config1.put(host1, server1);

        HashMap<String, String> server2 = new HashMap<>();
        String host2 = "test2.com";
        server2.put("port", "2001");
        server2.put("host", host2);
        server2.put("root", "html");
        config1.put(host2, server2);


        String configFile = "/Users/cg/data/code/wheel/java/demo/html/config.json";
        HashMap<String, HashMap> config2 = configParser.parse(configFile);

        assertEquals(config1, config2);
    }
}