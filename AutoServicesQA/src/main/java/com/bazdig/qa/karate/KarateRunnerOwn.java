package com.bazdig.qa.karate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intuit.karate.FileUtils;
import com.intuit.karate.cucumber.CucumberRunner;
import com.bazdig.qa.karate.KarateFeatureOwn;
import com.intuit.karate.cucumber.KarateJunitAndJsonReporter;
import com.intuit.karate.cucumber.KarateJunitFormatter;
import com.bazdig.qa.karate.KarateRuntimeOwn;
import com.bazdig.qa.karate.KarateRuntimeOptionsOwn;
import com.intuit.karate.cucumber.KarateStats;
import com.intuit.karate.filter.TagFilter;
import com.intuit.karate.filter.TagFilterException;

import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.CucumberTagStatement;

public class KarateRunnerOwn extends CucumberRunner{
	
	private static final Logger logger = LoggerFactory.getLogger(CucumberRunner.class);
	
	
	public static KarateStats parallelJar(List<String> tags, List<String> paths, int threadCount, String reportDir) {
		KarateRuntimeOptionsOwn kro = new KarateRuntimeOptionsOwn(tags, paths);
        List<KarateFeatureOwn> karateFeatures = KarateFeatureOwn.loadFeatures(kro);
        return parallelJar(karateFeatures, threadCount, reportDir);
    }
	
    private static KarateStats parallelJar(List<KarateFeatureOwn> karateFeatures, int threadCount, String userReportDir) {
        String reportDir = userReportDir == null ? "target/surefire-reports" : userReportDir;
        logger.info("Karate version: {}", FileUtils.getKarateVersion());
        KarateStats stats = KarateStats.startTimer();
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        try {
            int count = karateFeatures.size();
            int filteredCount = 0;
            List<Callable<KarateJunitAndJsonReporter>> callables = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                KarateFeatureOwn karateFeature = karateFeatures.get(i);
                int index = i + 1;
                CucumberFeature feature = karateFeature.getFeature();
                filterOnTags(feature);
                if (!feature.getFeatureElements().isEmpty()) {
                    callables.add(() -> {
                        // we are now within a separate thread. the reporter filters logs by self thread
                        String threadName = Thread.currentThread().getName();
                        KarateJunitAndJsonReporter reporter = karateFeature.getReporter(reportDir);
                        KarateRuntimeOwn runtime = karateFeature.getRuntime(reporter);
                        try {
                            feature.run(reporter, reporter, runtime);
                            runtime.afterFeature();
                            logger.info("<<<< feature {} of {} on thread {}: {}", index, count, threadName, feature.getPath());
                        } catch (Exception e) {
                            logger.error("karate xml/json generation failed for: {}", feature.getPath());
                            reporter.setFailureReason(e);
                        } finally { // try our best to close the report file gracefully so that report generation is not broken
                            reporter.done();
                        }
                        return reporter;
                    });
                } else {
                    filteredCount++;
                }
            }
            stats.setFeatureCount(count - filteredCount);

            List<Future<KarateJunitAndJsonReporter>> futures = executor.invokeAll(callables);
            stats.stopTimer();
            for (Future<KarateJunitAndJsonReporter> future : futures) {
                KarateJunitAndJsonReporter reporter = future.get(); // guaranteed to be not-null
                KarateJunitFormatter formatter = reporter.getJunitFormatter();
                if (reporter.getFailureReason() != null) {
                    logger.error("karate xml/json generation failed: {}", formatter.getFeaturePath());
                    logger.error("karate xml/json error stack trace", reporter.getFailureReason());
                }
                stats.addToTestCount(formatter.getTestCount());
                stats.addToFailCount(formatter.getFailCount());
                stats.addToSkipCount(formatter.getSkipCount());
                stats.addToTimeTaken(formatter.getTimeTaken());
                if (formatter.isFail()) {
                    stats.addToFailedList(formatter.getFeaturePath(), formatter.getFailMessages() + "");
                }
            }
        } catch (Exception e) {
            logger.error("karate parallel runner failed: ", e.getMessage());
            stats.setFailureReason(e);
        } finally {
            executor.shutdownNow();
        }
        stats.printStats(threadCount);
        return stats;
    }
    
    private static void filterOnTags(CucumberFeature feature) throws TagFilterException {
        final List<CucumberTagStatement> featureElements = feature.getFeatureElements();
        ServiceLoader<TagFilter> loader = ServiceLoader.load(TagFilter.class);
        for (Iterator<CucumberTagStatement> iterator = featureElements.iterator(); iterator.hasNext();) {
            CucumberTagStatement cucumberTagStatement = iterator.next();
            for (TagFilter implClass : loader) {
                logger.info("Tag filter found: {}", implClass.getClass().getSimpleName());
                final boolean isFiltered = implClass.filter(feature, cucumberTagStatement);
                if (isFiltered) {
                    logger.info("skipping feature element {} of feature {} due to feature-tag-filter {} ",
                            cucumberTagStatement.getVisualName(),
                            feature.getPath(), implClass.getClass().getSimpleName());
                    iterator.remove();
                    break;
                }
            }
        }
    }



}
