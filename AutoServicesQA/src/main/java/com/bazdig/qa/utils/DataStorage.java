package com.bazdig.qa.utils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class DataStorage {
    Properties prop = new Properties();
    OutputStream output = null;
    InputStream input = null;
    private static String FILE_NAME = "src/main/java/com/bazdig/qa/utils/test.properties";
    
    
    public static void main( String... args){
    	DataStorage dStorage = new DataStorage();
    	try {
			
			System.out.println(dStorage.read("hola"));
			Map<String, Object> config = new HashMap<String, Object>();
			config.put("key", "Adios");
			dStorage.write(config);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }
    

    public void write(Map<String, Object> config) throws IOException {

        String key = (String) config.get("key");
        output = new FileOutputStream(FILE_NAME);
        prop.setProperty("key", key);
        prop.store(output, null);
    }

    public String read(String key) throws IOException {
    	ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    	InputStream input = classLoader.getResourceAsStream(FILE_NAME);
        input = new FileInputStream(FILE_NAME);
        prop.load(input);
        return prop.getProperty(key);
    }
}