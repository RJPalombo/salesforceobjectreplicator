package com.rjpalombo.salesforce.objectreplicator.batch.job.processor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import com.rjpalombo.salesforce.objectreplicator.domain.SFDCJobInfo;
import com.rjpalombo.salesforce.objectreplicator.service.SalesforceService;
import com.sforce.async.AsyncApiException;
import com.sforce.async.BatchInfo;
import com.sforce.async.BatchStateEnum;
import com.sforce.async.BulkConnection;
import com.sforce.async.JobInfo;
import com.sforce.async.JobStateEnum;

public class SFDCJobInfoProcessor implements ItemProcessor<SFDCJobInfo, SFDCJobInfo> {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final Long sleepTime = 10000L;

	private BulkConnection bulkConnection;

	public SFDCJobInfoProcessor(SalesforceService sfService) {
		this.bulkConnection = sfService.getBulkConnection();
	}

	@Override
	public SFDCJobInfo process(SFDCJobInfo sfdcJobInfo) throws Exception {
		String jobId = sfdcJobInfo.getJobInfo().getId();
		JobInfo job = sfdcJobInfo.getJobInfo();
		List<BatchInfo> batchInfos = sfdcJobInfo.getBatchInfos();

		closeJob(jobId);
		awaitCompletion(job, batchInfos);

		return sfdcJobInfo;
	}

	private void closeJob(String jobId) throws AsyncApiException {
		JobInfo job = new JobInfo();
		job.setId(jobId);
		job.setState(JobStateEnum.Closed);
		bulkConnection.updateJob(job);
	}

	private void awaitCompletion(JobInfo job, List<BatchInfo> batchInfoList) throws AsyncApiException {
		Set<String> incomplete = new HashSet<String>();
		for (BatchInfo bi : batchInfoList) {
			incomplete.add(bi.getId());
		}
		while (!incomplete.isEmpty()) {
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
			}
			logger.info("Awaiting results..." + incomplete.size());
			BatchInfo[] statusList = bulkConnection.getBatchInfoList(job.getId()).getBatchInfo();
			for (BatchInfo b : statusList) {
				if (b.getState() == BatchStateEnum.Completed || b.getState() == BatchStateEnum.Failed) {
					if (incomplete.remove(b.getId())) {
						logger.info("BATCH STATUS:\n" + b);
					}
				}
			}
		}
	}

}
