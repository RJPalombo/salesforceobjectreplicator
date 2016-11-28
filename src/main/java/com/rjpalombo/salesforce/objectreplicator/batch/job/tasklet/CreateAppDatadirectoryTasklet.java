package com.rjpalombo.salesforce.objectreplicator.batch.job.tasklet;

import java.io.File;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import com.rjpalombo.salesforce.objectreplicator.config.ApplicationConfig;

public class CreateAppDatadirectoryTasklet implements Tasklet {

	private ApplicationConfig appConfig;

	public CreateAppDatadirectoryTasklet(ApplicationConfig appConfig) {
		this.appConfig = appConfig;
	}

	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		File dir = new File(appConfig.getDatadirectory());
		if (!dir.exists()) {
			dir.mkdir();
		}
		return RepeatStatus.FINISHED;
	}

}