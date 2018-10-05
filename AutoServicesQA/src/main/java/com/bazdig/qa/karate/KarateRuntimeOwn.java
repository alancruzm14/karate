package com.bazdig.qa.karate;

import java.util.Collections;
import java.util.Set;

import com.intuit.karate.Logger;
import com.intuit.karate.ScriptContext;
import com.intuit.karate.ScriptEnv;
import com.intuit.karate.ScriptValue;
import com.intuit.karate.cucumber.CucumberUtils;
import com.intuit.karate.cucumber.KarateBackend;
import com.intuit.karate.cucumber.KarateReporter;
import com.intuit.karate.cucumber.KarateRuntimeOptions;
import com.intuit.karate.cucumber.StepResult;
import com.intuit.karate.http.HttpConfig;

import cucumber.runtime.CucumberScenarioImpl;
import cucumber.runtime.CucumberStats;
import cucumber.runtime.Runtime;
import cucumber.runtime.RuntimeGlue;
import gherkin.I18n;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Match;
import gherkin.formatter.model.Result;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.Step;
import gherkin.formatter.model.Tag;

public class KarateRuntimeOwn extends Runtime {

	   private final KarateBackend backend;
	    private final CucumberStats stats;
	    private CucumberScenarioImpl scenarioResult;
	    private boolean stopped;
	    private boolean aborted;
	    private ScriptContext prevContext;

	    public KarateRuntimeOwn(KarateRuntimeOptionsOwn kro, KarateBackend backend, RuntimeGlue glue) {
	        super(kro.getResourceLoader(), kro.getClassLoader(), Collections.singletonList(backend), kro.getRuntimeOptions(), glue);
	        this.backend = backend;
	        this.stats = new CucumberStats(kro.getRuntimeOptions().isMonochrome());
	    }
	    
	    public Logger getLogger() {
	        return backend.getEnv().logger;
	    }

	    private void addStepToCounterAndResult(Result result) {
	        scenarioResult.add(result);
	        stats.addStep(result);
	    }

	    @Override
	    public void runStep(String featurePath, Step step, Reporter reporter, I18n i18n) {
	        if (stopped) {
	            Match match = Match.UNDEFINED;
	            Result result = aborted ? StepResult.PASSED : Result.SKIPPED;
	            if (reporter instanceof KarateReporter) { // simulate cucumber flow to keep json-formatter happy                
	                ((KarateReporter) reporter).karateStep(step, match, result, backend.getCallContext(), backend.getStepDefs().getContext());
	            }
	            reporter.match(match);
	            addStepToCounterAndResult(result);
	            reporter.result(result);
	            return;
	        }
	        StepResult result = CucumberUtils.runStep(step, reporter, i18n, backend);
	        if (!result.isPass() || result.isAbort()) {
	            if (!result.isAbort()) {
	                addError(result.getError());
	                backend.setScenarioError(result.getError());
	            }
	            prevContext = backend.getStepDefs().getContext();
	            stopped = true; // skip remaining steps
	            aborted = result.isAbort(); // if skipped steps are to be marked as PASSED
	        }
	        addStepToCounterAndResult(result.getResult());
	    }

	    @Override
	    public void buildBackendWorlds(Reporter reporter, Set<Tag> tags, Scenario scenario) {
	        backend.buildWorld();
	        // tags only work at top-level, this does not apply to 'called' features
	        CucumberUtils.resolveTagsAndTagValues(backend, tags);
	        // 'karate.info' also does not apply to 'called' features
	        CucumberUtils.initScenarioInfo(scenario, backend);
	        scenarioResult = new CucumberScenarioImpl(reporter, tags, scenario);
	    }

	    @Override
	    public void disposeBackendWorlds(String scenarioDesignation) {
	        stats.addScenario(scenarioResult.getStatus(), scenarioDesignation);
	        prevContext = backend.getStepDefs().getContext();
	        invokeAfterHookIfConfigured(false);
	        backend.disposeWorld();
	        stopped = false; // else a failed scenario results in all remaining ones in the feature being skipped !
	    }

	    @Override
	    public void printSummary() {
	        stats.printStats(System.out, false);
	    }       
	    
	    public void afterFeature() {
	        invokeAfterHookIfConfigured(true);
	    }
	    
	    private void invokeAfterHookIfConfigured(boolean afterFeature) {
	        if (prevContext == null) { // edge case where there are zero scenarios, e.g. only a Background:
	            ScriptEnv env = backend.getEnv();
	            env.logger.warn("no runnable scenarios found: {}", env);
	            return;
	        }
	        HttpConfig config = prevContext.getConfig();
	        ScriptValue sv = afterFeature ? config.getAfterFeature() : config.getAfterScenario();
	        if (sv.isFunction()) {
	            try {
	                sv.invokeFunction(prevContext);
	            } catch (Exception e) {
	                String prefix = afterFeature ? "afterFeature" : "afterScenario";
	                prevContext.logger.warn("{} hook failed: {}", prefix, e.getMessage());
	            }
	        }
	    }
}
