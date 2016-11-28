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
import com.rjpalombo.salesforce.objectreplicator.domain.HibernateModel;
import com.thoughtworks.xstream.XStream;

@StepScope
public class HibernateModelXMLItemWriter<T> implements ItemWriter<T> {

	private final String FILE_EXT = ".xml";
	private final String DIR = "hibernate-models";

	ApplicationConfig appConfig;

	public HibernateModelXMLItemWriter(ApplicationConfig appConfig) {
		this.appConfig = appConfig;
	}

	@Override
	public void write(List<? extends T> items) throws Exception {
		for (T o : items) {
			HibernateModel hm = (HibernateModel) o;
			writeFile(hm);
		}
	}

	private void writeFile(HibernateModel model) throws IOException {
		File file = new File(appConfig.getDatadirectory() + "/" + DIR + "/" + model.getClazz().getName() + FILE_EXT);
		file.getParentFile().mkdirs();
		boolean restarted = file.exists();
		FileUtils.setUpOutputFile(file, restarted, true, true);

		XStream xs = new XStream();
		xs.autodetectAnnotations(true);

		StringBuffer sb = new StringBuffer();
		sb.append("<?xml version=\"1.0\"?>");
		sb.append("\n<!DOCTYPE hibernate-mapping PUBLIC \"-//Hibernate/Hibernate Mapping DTD 3.0//EN\"");
		sb.append("\n\"http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd\">");
		sb.append("\n" + xs.toXML(model));

		FileOutputStream os = new FileOutputStream(file, true);

		BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(os, Charset.forName("UTF-8")));
		bufferedWriter.write(sb.toString());
		bufferedWriter.close();
	}

}
