package com.tester.mockall.utils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by vivek.agr on 12/4/14.
 */
public class Utility {

    public static String getConfigValue(String param, String filename) {
        Properties prop = new Properties();

        InputStream is = Utility.class.getClassLoader().getResourceAsStream(filename);
        try {
            prop.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(is == null) {
            System.out.println("No configuration present in the filename");
        }

        return (prop.getProperty(param));
    }

    public static Map readConfigFile() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(new File("properties.conf")));
        String line = null;

        Map<String, String> map = new HashMap<String, String>();
        while ((line = reader.readLine()) != null) {
            if (line.contains("=")) {
                String[] strings = line.split("=");
                map.put(strings[0].trim(), strings[1].trim());
            }
        }
        return map;
    }
}
