package com.rjpalombo.salesforce.objectreplicator.batch.job;

import java.io.IOException;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.rjpalombo.salesforce.objectreplicator.batch.job.processor.SFDCJobInfoProcessor;
import com.rjpalombo.salesforce.objectreplicator.batch.job.processor.SObjectMetaToHibernateModelProcessor;
import com.rjpalombo.salesforce.objectreplicator.batch.job.processor.SObjectMetaToSalesforceBulkJobInfoProcessor;
import com.rjpalombo.salesforce.objectreplicator.batch.job.reader.SFDCJobInfoXMLFileItemReader;
import com.rjpalombo.salesforce.objectreplicator.batch.job.reader.SObjectMetaItemReader;
import com.rjpalombo.salesforce.objectreplicator.batch.job.reader.SObjectMetaXMLFileItemReader;
import com.rjpalombo.salesforce.objectreplicator.batch.job.tasklet.CleanAppDatadirectoryTasklet;
import com.rjpalombo.salesforce.objectreplicator.batch.job.tasklet.CreateAppDatadirectoryTasklet;
import com.rjpalombo.salesforce.objectreplicator.batch.job.tasklet.CreateSOQLQueriesTasklet;
import com.rjpalombo.salesforce.objectreplicator.batch.job.writer.BulkCSVFileItemWriter;
import com.rjpalombo.salesforce.objectreplicator.batch.job.writer.HibernateModelXMLItemWriter;
import com.rjpalombo.salesforce.objectreplicator.batch.job.writer.SObjectMetaXMLItemWriter;
import com.rjpalombo.salesforce.objectreplicator.batch.job.writer.SalesforceBulkJobInfoXMLItemWriter;
import com.rjpalombo.salesforce.objectreplicator.config.ApplicationConfig;
import com.rjpalombo.salesforce.objectreplicator.config.SalesforceConfig;
import com.rjpalombo.salesforce.objectreplicator.domain.HibernateModel;
import com.rjpalombo.salesforce.objectreplicator.domain.SFDCJobInfo;
import com.rjpalombo.salesforce.objectreplicator.service.SalesforceService;
import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.ws.ConnectionException;

@Configuration
@EnableBatchProcessing(modular = true)
public class ReplicationJobConfig {

	@Autowired
	private SalesforceService sfService;

	@Autowired
	private ApplicationConfig appConfig;
	
	@Autowired
	private SalesforceConfig sfConfig;

	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;

	@Bean
	public Job replicationJob() throws ConnectionException, IOException {
		return jobBuilderFactory.get("replicationJob")
				.incrementer(new RunIdIncrementer())
				.start(createAppDataDirectory())
				.next(cleanAppDataDirectory())
				.next(retrieveSObjectMeta())
				.next(createSOQLQueries())
				.next(submitBulkFileRequests())
				.next(retrieveBulkFiles())
				.next(createHibernateModels())
				.build();
	}

	// tag: Start Step - createAppDataDirectory
	@Bean
	protected Step createAppDataDirectory() {
		return stepBuilderFactory.get("createAppDataDirectory")
				.tasklet(new CreateAppDatadirectoryTasklet(appConfig))
				.build();
	}
	// tag: End Step - createAppDataDirectory

	// tag: Start Step - cleanAppDataDirectory
	@Bean
	protected Step cleanAppDataDirectory() {
		return stepBuilderFactory.get("cleanAppDataDirectory")
				.tasklet(new CleanAppDatadirectoryTasklet(appConfig))
				.build();
	}
	// tag: End Step - cleanAppDataDirectory

	// tag: Start Step - retrieveSObjectMeta
	@Bean
	protected Step retrieveSObjectMeta() throws ConnectionException {
		return stepBuilderFactory.get("retrieveSObjectMeta")
				.<DescribeSObjectResult, DescribeSObjectResult>chunk(1)
				.reader(new SObjectMetaItemReader(sfService))
				.writer(new SObjectMetaXMLItemWriter<DescribeSObjectResult>(appConfig))
				.build();
	}
	// tag: End Step - retrieveSObjectMeta
	
	// tag: Start Step - createSOQLQueries
	@Bean
	protected Step createSOQLQueries() throws ConnectionException, IOException {
		return stepBuilderFactory.get("createSOQLQueries")
				.tasklet(new CreateSOQLQueriesTasklet(appConfig, sfConfig))
				.build();
	}
	// tag: End Step - createSOQLQueries
	
	// tag: Start Step - submitBulkFileRequests
	@Bean
	protected Step submitBulkFileRequests() throws ConnectionException, IOException {
		return stepBuilderFactory.get("submitBulkFileRequests")
				.<DescribeSObjectResult, SFDCJobInfo>chunk(1)
				.reader(new SObjectMetaXMLFileItemReader(appConfig))
				.processor(new SObjectMetaToSalesforceBulkJobInfoProcessor(appConfig, sfService))
				.writer(new SalesforceBulkJobInfoXMLItemWriter<SFDCJobInfo>(appConfig))
				.build();
	}
	// tag: End Step - submitBulkFileRequests
	
	// tag: Start Step - retrieveBulkFiles
	@Bean
	protected Step retrieveBulkFiles() throws ConnectionException, IOException {
		return stepBuilderFactory.get("retrieveBulkFiles")
				.<SFDCJobInfo, SFDCJobInfo>chunk(1)
				.reader(new SFDCJobInfoXMLFileItemReader(appConfig))
				.processor(new SFDCJobInfoProcessor(sfService))
				.writer(new BulkCSVFileItemWriter<SFDCJobInfo>(appConfig, sfService))
				.build();
	}
	// tag: End Step - retrieveBulkFiles
	
	// tag: Start Step - createHibernateModels
	@Bean
	protected Step createHibernateModels() throws ConnectionException, IOException {
		return stepBuilderFactory.get("createHibernateModels")
				.<DescribeSObjectResult, HibernateModel>chunk(1)
				.reader(new SObjectMetaXMLFileItemReader(appConfig))
				.processor(new SObjectMetaToHibernateModelProcessor(sfConfig))
				.writer(new HibernateModelXMLItemWriter<HibernateModel>(appConfig))
				.build();
	}
	// tag: End Step - createHibernateModels

	/*
	 * @Bean
	 * 
	 * @Primary
	 * 
	 * @ConfigurationProperties(prefix = "spring.batch.datasource") public
	 * DataSource getJobDataSource() { return
	 * DataSourceBuilder.create().build(); }
	 * 
	 * @Bean
	 * 
	 * @ConfigurationProperties(prefix = "spring.datasource") public DataSource
	 * getDBDataSource() { return DataSourceBuilder.create().build(); }
	 */

}
