package com.rjpalombo.salesforce.objectreplicator.batch.job.tasklet;

import java.io.File;

import org.h2.store.fs.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import com.rjpalombo.salesforce.objectreplicator.config.ApplicationConfig;

public class CleanAppDatadirectoryTasklet implements Tasklet {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private ApplicationConfig appConfig;

	public CleanAppDatadirectoryTasklet(ApplicationConfig appConfig) {
		this.appConfig = appConfig;
	}

	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		File dir = new File(appConfig.getDatadirectory());

		// Remove all sub dirs
		File[] files = dir.listFiles();
		for (int i = 0; i < files.length; i++) {
			String filename = files[i].getName();
			if (!filename.toLowerCase().contains("log")) {
				logger.info("Clean App Data Directory: Attempting to delete - " + filename);
				FileUtils.deleteRecursive(files[i].getAbsolutePath(), false);
			} else {
				logger.warn("Clean App Data Directory: Skipping the removal of logs");
			}
		}
		
		return RepeatStatus.FINISHED;
	}

}