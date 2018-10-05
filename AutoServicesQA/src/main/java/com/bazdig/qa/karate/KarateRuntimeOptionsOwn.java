package com.bazdig.qa.karate;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.intuit.karate.CallContext;
import com.intuit.karate.FileUtils;
import com.intuit.karate.ScriptEnv;
import com.intuit.karate.cucumber.FeatureWrapper;
import com.intuit.karate.cucumber.KarateBackend;
import com.intuit.karate.cucumber.KarateReporter;
import com.intuit.karate.cucumber.KarateRuntime;
import com.intuit.karate.cucumber.KarateRuntimeOptions;

import cucumber.runtime.RuntimeGlue;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.RuntimeOptionsFactory;
import cucumber.runtime.UndefinedStepsTracker;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.xstream.LocalizedXStreams;

public class KarateRuntimeOptionsOwn{
	private final ClassLoader classLoader;
    private final RuntimeOptions runtimeOptions;
    private final ResourceLoader resourceLoader;

    public KarateRuntimeOptionsOwn(Class clazz) {
        classLoader = clazz.getClassLoader();
        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(clazz);
        runtimeOptions = runtimeOptionsFactory.create();
        resourceLoader = new MultiLoader(classLoader);
    }

    public KarateRuntimeOptionsOwn(File file) {
        this(null, Arrays.asList(file.getPath()));
    }

    public KarateRuntimeOptionsOwn(List<String> tags, List<String> files) {
        classLoader = FileUtils.createClassLoader(files.toArray(new String[]{}));
        if (tags != null) {
            List<String> temp = new ArrayList();
            for (String tag : tags) { // to support logical AND, user can do -t twice
                temp.add("-t");
                temp.add(tag);
            }
            temp.addAll(files);
            runtimeOptions = new RuntimeOptions(temp);
        } else {
            runtimeOptions = new RuntimeOptions(files);
        }
        resourceLoader = new MultiLoader(classLoader);
    }

    public KarateRuntimeOwn getRuntime(File file, KarateReporter reporter) {
    	
        File featureDir = file.getParentFile();
        ScriptEnv env = new ScriptEnv(null, featureDir, file.getName(), classLoader, reporter);
        CallContext callContext = new CallContext(null, true);
        FeatureWrapper fw = FeatureWrapperOwn.fromFileJar(file, env);
        KarateBackend backend = new KarateBackend(fw, callContext);
        RuntimeGlue glue = new RuntimeGlue(new UndefinedStepsTracker(), new LocalizedXStreams(classLoader));
        
        return new KarateRuntimeOwn(this, backend, glue);
    }
    
    

    public List<CucumberFeature> loadFeatures() {
        return runtimeOptions.cucumberFeatures(resourceLoader);
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }

    public RuntimeOptions getRuntimeOptions() {
        return runtimeOptions;
    }
}
