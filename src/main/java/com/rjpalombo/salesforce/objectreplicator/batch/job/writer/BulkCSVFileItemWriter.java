package com.rjpalombo.salesforce.objectreplicator.batch.job.writer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;

import com.rjpalombo.salesforce.objectreplicator.config.ApplicationConfig;
import com.rjpalombo.salesforce.objectreplicator.domain.SFDCJobInfo;
import com.rjpalombo.salesforce.objectreplicator.service.SalesforceService;
import com.sforce.async.AsyncApiException;
import com.sforce.async.BatchInfo;
import com.sforce.async.BulkConnection;
import com.sforce.async.QueryResultList;

@StepScope
public class BulkCSVFileItemWriter<T> implements ItemWriter<T> {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final String FILE_EXT = ".csv";
	private final String DIR = "salesforcebulk-extracts";
	
	private BulkConnection bulkConnection;

	ApplicationConfig appConfig;

	public BulkCSVFileItemWriter(ApplicationConfig appConfig, SalesforceService sfService) {
		this.appConfig = appConfig;
		this.bulkConnection = sfService.getBulkConnection();
	}

	@Override
	public void write(List<? extends T> items) throws Exception {
		for (T i : items) {
			SFDCJobInfo info = (SFDCJobInfo) i;
			writeFile(info);
		}
	}

	private void writeFile(SFDCJobInfo info) throws IOException, AsyncApiException {
		File file = new File(appConfig.getDatadirectory() + "/" + DIR + "/" + info.getJobInfo().getObject() + FILE_EXT);
		file.getParentFile().mkdirs();
		
		String jobId = info.getJobInfo().getId();
		List<BatchInfo> batchInfos = info.getBatchInfos();
		
		// Write CSV File
		for(BatchInfo batchInfo : batchInfos) {
			String batchId = batchInfo.getId();
			QueryResultList resultList = bulkConnection.getQueryResultList(jobId, batchId);
			if(resultList.getResult().length > 1) {
				logger.warn("BulkCSVFileItemWriter: Expected only 1 result and received " + resultList.getResult().length);
			}
			for(String resultId : resultList.getResult()) {
				InputStream stream = bulkConnection.getQueryResultStream(jobId, batchId, resultId);
				FileUtils.copyInputStreamToFile(stream, file);
			}
		}
	}

}
