package com.bazdig.qa.karate;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.intuit.karate.FileUtils;
import com.intuit.karate.cucumber.KarateJunitAndJsonReporter;
import com.intuit.karate.cucumber.KarateReporter;
import com.bazdig.qa.karate.KarateRuntimeOwn;
import com.bazdig.qa.karate.KarateRuntimeOptionsOwn;

import cucumber.runtime.model.CucumberFeature;

public class KarateFeatureOwn{

    private final KarateRuntimeOptionsOwn runtimeOptions;
    private final File file;    
    private final CucumberFeature feature;       

    public CucumberFeature getFeature() {
        return feature;
    }        
    
    public KarateFeatureOwn(CucumberFeature feature, KarateRuntimeOptionsOwn karateOptions) {
        file = FileUtils.resolveIfClassPath(feature.getPath(), karateOptions.getClassLoader());
        this.feature = feature;
        this.runtimeOptions = karateOptions;
    }
    
    public KarateFeatureOwn(File file) {
        this.file = file;
        runtimeOptions = new KarateRuntimeOptionsOwn(file);
        feature = runtimeOptions.loadFeatures().get(0);
    }
    
    public static List<KarateFeatureOwn> loadFeatures(KarateRuntimeOptionsOwn runtimeOptions) {
        List<CucumberFeature> features = runtimeOptions.loadFeatures();
        List<KarateFeatureOwn> karateFeatures = new ArrayList(features.size());
        for (CucumberFeature feature : features) {
        	KarateFeatureOwn kf = new KarateFeatureOwn(feature, runtimeOptions);
            karateFeatures.add(kf);
        }
        return karateFeatures;
    }
    
    public KarateRuntimeOwn getRuntime(KarateReporter reporter) {
        KarateRuntimeOwn kr = runtimeOptions.getRuntime(file, reporter);
        reporter.setLogger(kr.getLogger());
        return kr;
    }
    
    public KarateJunitAndJsonReporter getReporter(String reportDirPath) {
        File reportDir = new File(reportDirPath);
        String featurePackagePath = FileUtils.toPackageQualifiedName(feature.getPath());
        try {
            reportDir.mkdirs();
            reportDirPath = reportDir.getPath() + File.separator;
            String reportPath = reportDirPath + "TEST-" + featurePackagePath + ".xml";
            return new KarateJunitAndJsonReporter(featurePackagePath, reportPath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }        
    }
}
