package com.rjpalombo.salesforce.objectreplicator.batch.job.tasklet;

import java.io.File;
import java.io.FileOutputStream;
import java.util.LinkedList;
import java.util.List;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import com.rjpalombo.salesforce.objectreplicator.config.ApplicationConfig;
import com.rjpalombo.salesforce.objectreplicator.config.SalesforceConfig;
import com.rjpalombo.salesforce.objectreplicator.domain.SOQLQueries;
import com.rjpalombo.salesforce.objectreplicator.domain.SOQLQueries.SOQLQuery;
import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.Field;
import com.thoughtworks.xstream.XStream;

@StepScope
public class CreateSOQLQueriesTasklet implements Tasklet {
	
	private final String WRITE_FILENAME = "sobject-queries.xml";
	private final String OBJECT_META_FILE_EXT = ".xml";
	private final String OBJECT_META_DIR = "sobject-meta";
	
	private final ApplicationConfig appConfig;
	private List<String> typesToInclude = new LinkedList<>();
	
	public CreateSOQLQueriesTasklet(ApplicationConfig appConfig, SalesforceConfig sfConfig) {
		this.appConfig = appConfig;
		for(String fieldType : sfConfig.getIncludedfieldtypes()) {
			typesToInclude.add(fieldType.toLowerCase());
		}
	}

	@Override
	public RepeatStatus execute(StepContribution arg0, ChunkContext arg1) throws Exception {
		File metaDir = new File(appConfig.getDatadirectory() + "/" + OBJECT_META_DIR);
		File[] files = metaDir.listFiles();
		
		// Read describeSObjectResults
		List<DescribeSObjectResult> describeSObjectResults = new LinkedList<>();
		if (files != null && files.length > 0) {
			for (File file : files) {
				if(!file.getName().toLowerCase().endsWith(OBJECT_META_FILE_EXT)) {
					continue;
				}
				XStream xStream = new XStream();
				DescribeSObjectResult sObject = (DescribeSObjectResult) xStream.fromXML(file);
				describeSObjectResults.add(sObject);
			}
		}
		
		// Process describeSObjectResults
		SOQLQueries queries = new SOQLQueries();
		for(DescribeSObjectResult sObject : describeSObjectResults) {
			SOQLQuery soqlXML = new SOQLQuery();

			String name = sObject.getName();
			Field[] fields = sObject.getFields();

			String soql = "SELECT ";
			for (Field field : fields) {
				if (!typesToInclude.contains(field.getType().toString().toLowerCase())) {
					continue;
				}

				soql += field.getName() + ", ";
			}
			soql = soql.substring(0, soql.length() - 2);
			soql += " FROM " + sObject.getName();

			if (fields.length > 0) {
				soqlXML.setsObjectName(name);
				soqlXML.setQuery(soql);
				queries.getQueries().add(soqlXML);
			}
		}
		
		// Write XML		
		XStream xs = new XStream();
		xs.autodetectAnnotations(true);
		
		File file = new File(appConfig.getDatadirectory() + "/" + WRITE_FILENAME);
		FileOutputStream out = new FileOutputStream(file, true);
		xs.toXML(queries, out);
		
		return RepeatStatus.FINISHED;
	}

}
