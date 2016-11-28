package com.rjpalombo.salesforce.objectreplicator.batch.job.processor;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.batch.item.ItemProcessor;

import com.rjpalombo.salesforce.objectreplicator.config.ApplicationConfig;
import com.rjpalombo.salesforce.objectreplicator.domain.SFDCJobInfo;
import com.rjpalombo.salesforce.objectreplicator.domain.SOQLQueries;
import com.rjpalombo.salesforce.objectreplicator.domain.SOQLQueries.SOQLQuery;
import com.rjpalombo.salesforce.objectreplicator.service.SalesforceService;
import com.sforce.async.BatchInfo;
import com.sforce.async.BulkConnection;
import com.sforce.async.ContentType;
import com.sforce.async.JobInfo;
import com.sforce.async.OperationEnum;
import com.sforce.soap.partner.DescribeSObjectResult;
import com.thoughtworks.xstream.XStream;

public class SObjectMetaToSalesforceBulkJobInfoProcessor implements ItemProcessor<DescribeSObjectResult, SFDCJobInfo> {

	private final String FILENAME = "sobject-queries.xml";

	private SalesforceService sfService;
	private Map<String, String> queryMap = new LinkedHashMap<>();

	public SObjectMetaToSalesforceBulkJobInfoProcessor(ApplicationConfig appConfig, SalesforceService sfService) {
		this.sfService = sfService;

		File queryFile = new File(appConfig.getDatadirectory() + "/" + FILENAME);

		if (queryFile.exists()) {
			XStream xs = new XStream();
			xs.autodetectAnnotations(true);
			xs.alias("queries", SOQLQueries.class);
			xs.alias("soqlQuery", SOQLQuery.class);

			SOQLQueries queries = (SOQLQueries) xs.fromXML(queryFile);
			for (SOQLQueries.SOQLQuery query : queries.getQueries()) {
				queryMap.put(query.getsObjectName().toLowerCase(), query.getQuery());
			}
		}
	}

	@Override
	public SFDCJobInfo process(DescribeSObjectResult sObject) throws Exception {
		BulkConnection bulkConnection = sfService.getBulkConnection();
		String query = queryMap.get(sObject.getName().toLowerCase());

		// Create Job
		JobInfo jobInfo = new JobInfo();
		jobInfo.setObject(sObject.getName());
		jobInfo.setOperation(OperationEnum.query);
		jobInfo.setContentType(ContentType.CSV);
		jobInfo = bulkConnection.createJob(jobInfo);

		// Add Batch to Job
		ByteArrayInputStream bout = new ByteArrayInputStream(query.getBytes());
		BatchInfo batchInfo = bulkConnection.createBatchFromStream(jobInfo, bout);

		// Create SFDCJobInfo
		SFDCJobInfo info = new SFDCJobInfo(jobInfo);
		info.getBatchInfos().add(batchInfo);

		return info;
	}

}
