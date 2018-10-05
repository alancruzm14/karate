package com.bazdig.qa.karate;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;

import com.intuit.karate.FileUtils;
import com.intuit.karate.ScriptEnv;
import com.intuit.karate.cucumber.FeatureWrapper;

public class FeatureWrapperOwn {

	public static FeatureWrapper fromFileJar(File file, ScriptEnv env) {

		// Se necesita el inputstream pero a nivel del jar
		// String text = FileUtils.toString(new FileInputStream(file));
		String text = "";

		if (file != null && file.getPath().contains("!")) {
			String path = file.getPath().split("\\!")[1];

			System.out.println("Feature file in jar:"
					+ FeatureWrapperOwn.class.getClassLoader().getResource(path.replaceFirst("/", "")));
			if(FeatureWrapperOwn.class.getClassLoader().getResource(path.replaceFirst("/", "")) == null) {
				text = FileUtils.toString(file);
			}else {
				text = FileUtils
						.toString(FeatureWrapperOwn.class.getClassLoader().getResourceAsStream(path.replaceFirst("/", "")));
			}
			
		} else {

			text = FileUtils.toString(file);
		}

		return FeatureWrapper.fromString(text, env, file.getPath());
	}
}
