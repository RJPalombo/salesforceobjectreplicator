package com.rjpalombo.salesforce.objectreplicator.batch.job.writer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.List;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.util.FileUtils;

import com.rjpalombo.salesforce.objectreplicator.config.ApplicationConfig;
import com.sforce.soap.partner.DescribeSObjectResult;
import com.thoughtworks.xstream.XStream;

@StepScope
public class SObjectMetaXMLItemWriter<T> implements ItemWriter<T> {

	private final String FILE_EXT = ".xml";
	private final String DIR = "sobject-meta";

	ApplicationConfig appConfig;

	public SObjectMetaXMLItemWriter(ApplicationConfig appConfig) {
		this.appConfig = appConfig;
	}

	@Override
	public void write(List<? extends T> items) throws Exception {
		for (T o : items) {
			DescribeSObjectResult so = (DescribeSObjectResult) o;
			writeFile(so);
		}
	}

	private void writeFile(DescribeSObjectResult sObject) throws IOException {
		File file = new File(appConfig.getDatadirectory() + "/" + DIR + "/" + sObject.getName() + FILE_EXT);
		file.getParentFile().mkdirs();
		boolean restarted = file.exists();
		FileUtils.setUpOutputFile(file, restarted, true, true);

		XStream xs = new XStream();
		StringBuffer sb = new StringBuffer();
		sb.append(xs.toXML(sObject));

		FileOutputStream os = new FileOutputStream(file, true);

		BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(os, Charset.forName("UTF-8")));
		bufferedWriter.write(sb.toString());
		bufferedWriter.close();
	}

}
