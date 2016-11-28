package com.rjpalombo.salesforce.objectreplicator.batch.job.reader;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import com.rjpalombo.salesforce.objectreplicator.config.ApplicationConfig;
import com.sforce.soap.partner.DescribeSObjectResult;
import com.thoughtworks.xstream.XStream;

@StepScope
public class SObjectMetaXMLFileItemReader implements ItemReader<DescribeSObjectResult> {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private final String FILE_EXT = ".xml";
	private final String DIR = "sobject-meta";
	private List<DescribeSObjectResult> describeSObjectResults = new LinkedList<>();
	private int nextRecordIndex;

	ApplicationConfig appConfig;

	public SObjectMetaXMLFileItemReader(ApplicationConfig appConfig) throws IOException {
		this.appConfig = appConfig;
		nextRecordIndex = 0;
		
		File metaDir = new File(appConfig.getDatadirectory() + "/" + DIR);
		File[] files = metaDir.listFiles();
		
		if (files != null && files.length > 0) {
			for (File file : files) {
				logger.info("Reading sObject Meta File: " + file.getName());
				if(!file.getName().toLowerCase().endsWith(FILE_EXT)) {
					continue;
				}
				XStream xStream = new XStream();
				DescribeSObjectResult sObject = (DescribeSObjectResult) xStream.fromXML(file);
				describeSObjectResults.add(sObject);
			}
		}
	}

	@Override
	public DescribeSObjectResult read()
			throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
		DescribeSObjectResult sObject = null;
		if (nextRecordIndex < describeSObjectResults.size()) {
			sObject = describeSObjectResults.get(nextRecordIndex);
			nextRecordIndex++;
		}
		return sObject;
	}

}
