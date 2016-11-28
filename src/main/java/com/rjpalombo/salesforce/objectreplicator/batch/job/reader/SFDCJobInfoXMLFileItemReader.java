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
import com.rjpalombo.salesforce.objectreplicator.domain.SFDCJobInfo;
import com.sforce.async.BatchInfo;
import com.sforce.async.JobInfo;
import com.thoughtworks.xstream.XStream;

@StepScope
public class SFDCJobInfoXMLFileItemReader implements ItemReader<SFDCJobInfo> {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private final String FILE_EXT = ".xml";
	private final String DIR = "salesforcebulk-jobinfo";
	private List<SFDCJobInfo> SFDCJobInfos = new LinkedList<>();
	private int nextRecordIndex;

	ApplicationConfig appConfig;

	public SFDCJobInfoXMLFileItemReader(ApplicationConfig appConfig) throws IOException {
		this.appConfig = appConfig;
		nextRecordIndex = 0;
		
		File metaDir = new File(appConfig.getDatadirectory() + "/" + DIR);
		File[] files = metaDir.listFiles();
		
		if (files != null && files.length > 0) {
			for (File file : files) {
				logger.info("Reading SFDC JobInfo File: " + file.getName());
				if(!file.getName().toLowerCase().endsWith(FILE_EXT)) {
					continue;
				}
				XStream xs = new XStream();
				xs.alias("SFDCJobInfo", SFDCJobInfo.class);
				xs.alias("JobInfo", JobInfo.class);
				xs.alias("BatchInfo", BatchInfo.class);
				
				SFDCJobInfo jobInfo = (SFDCJobInfo) xs.fromXML(file);
				SFDCJobInfos.add(jobInfo);
			}
		}
	}

	@Override
	public SFDCJobInfo read()
			throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
		SFDCJobInfo jobInfo = null;
		if (nextRecordIndex < SFDCJobInfos.size()) {
			jobInfo = SFDCJobInfos.get(nextRecordIndex);
			nextRecordIndex++;
		}
		return jobInfo;
	}

}
