package com.bazdig.qa.startMain;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.Test;


import org.apache.commons.io.FileUtils;
import org.junit.runner.JUnitCore;
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.bazdig.qa.karate.KarateRunnerOwn;
import com.intuit.karate.cucumber.KarateStats;

import net.masterthought.cucumber.Configuration;
import net.masterthought.cucumber.ReportBuilder;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;



public class MainApplication {

	private final static String INITIAL_PATH = "/Users/jchong/eclipse-workspace/AutoServicesQA/test/target/";
	private final static String SUNFIRE_REPORT_PATH = "surefire-reports";
	private final static String CUCUMBER_REPORT_PATH = "";//"cucumber-html-reports";
	
	public static void main(String[] args) {
		
		JUnitCore.main(MainApplication.class.getCanonicalName());

	}
	
	
    @Test
    public void executeTetingParallel() {
    	
    	
        List<String> tags = Arrays.asList();
        List<String> features = Arrays.asList("classpath:com/bazdig/qa/test/");
        String[] paths = folderCreation() ;
 
        KarateStats stats = KarateRunnerOwn.parallelJar(tags, features, 1, paths[0]);
        generateReport(paths[0], paths[1]);
        System.out.println("there are scenario failures: "+ stats.getFailCount());
    }
	
	
    public static void generateReport(String karateOutputPath, String cucumberPath) {
        Collection<File> jsonFiles = FileUtils.listFiles(new File(karateOutputPath), new String[] {"json"}, true);
        List<String> jsonPaths = new ArrayList(jsonFiles.size());
        jsonFiles.forEach(file -> jsonPaths.add(file.getAbsolutePath()));
        Configuration config = new Configuration(new File(cucumberPath), "demo");
        ReportBuilder reportBuilder = new ReportBuilder(jsonPaths, config);
        reportBuilder.generateReports();        
    }
    
    private static String[] folderCreation() {
    	
    	
    	LocalDate now = LocalDate.now();
    	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    	String formattedDate = now.format(formatter);
    	LocalDate parsedDate = LocalDate.parse(formattedDate, formatter);
    	
    	final String YEAR_PATH = INITIAL_PATH  + parsedDate.getYear();
    	final String MONTH_PATH = YEAR_PATH + "/" + parsedDate.getMonthValue();
    	final String DAY_PATH = MONTH_PATH + "/" + parsedDate.getDayOfMonth();
    	final String BUILD_NUMBER = DAY_PATH +"/"+ getLastBuildOfTheDay(DAY_PATH);
    	
    	final String SUNFIRE_LOCAL_PATH = BUILD_NUMBER + "/" + SUNFIRE_REPORT_PATH;
    	final String CUCUMBER_LOCAL_PATH = BUILD_NUMBER + "/" + CUCUMBER_REPORT_PATH;
    	
    			
    	createDirectory(YEAR_PATH);
    	createDirectory(MONTH_PATH);
    	createDirectory(DAY_PATH);
    	
    	createDirectory(BUILD_NUMBER);
    	createDirectory(SUNFIRE_LOCAL_PATH);
    	//createDirectory(CUCUMBER_LOCAL_PATH);
 
    	return new String[]{SUNFIRE_LOCAL_PATH, CUCUMBER_LOCAL_PATH};
    	
    }
    
    private static String getLastBuildOfTheDay(String pathName) {
    	
        List<String> fileNames = new ArrayList<>();
        try {
          DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(pathName));
          for (Path path : directoryStream) {
            fileNames.add(path.toString());
          }
        } catch (IOException ex) {
        }
        System.out.println("File Count:"+fileNames.size());
        return String.valueOf(fileNames.size());
    
    }
    
    private static void createDirectory(String pathName) {
    
    			
    	        Path dirPathObj = Paths.get(pathName);
    	
    	        boolean dirExists = Files.exists(dirPathObj);
    	
    	        if(!dirExists) {
    	
    	        	try {
    	            
    	                Files.createDirectories(dirPathObj);
    	
    	                System.out.println("! New Directory "+pathName+" Successfully Created !");
    	
    	            } catch (IOException ioExceptionObj) {
    	
    	                System.out.println("Problem Occured While Creating The Directory Structure  "+pathName+" = " + ioExceptionObj.getMessage());
    	
    	            }
    	
    	        } 
    	        
    	        
    
    }
}
