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
import com.rjpalombo.salesforce.objectreplicator.domain.SFDCJobInfo;
import com.sforce.async.BatchInfo;
import com.sforce.async.JobInfo;
import com.thoughtworks.xstream.XStream;

@StepScope
public class SalesforceBulkJobInfoXMLItemWriter<T> implements ItemWriter<T> {

	private final String FILE_EXT = ".xml";
	private final String DIR = "salesforcebulk-jobinfo";

	ApplicationConfig appConfig;

	public SalesforceBulkJobInfoXMLItemWriter(ApplicationConfig appConfig) {
		this.appConfig = appConfig;
	}

	@Override
	public void write(List<? extends T> items) throws Exception {
		for (T i : items) {
			SFDCJobInfo info = (SFDCJobInfo) i;
			writeFile(info);
		}
	}

	private void writeFile(SFDCJobInfo info) throws IOException {
		File file = new File(appConfig.getDatadirectory() + "/" + DIR + "/" + info.getJobInfo().getObject() + FILE_EXT);
		file.getParentFile().mkdirs();
		boolean restarted = file.exists();
		FileUtils.setUpOutputFile(file, restarted, true, true);

		XStream xs = new XStream();
		xs.alias("SFDCJobInfo", SFDCJobInfo.class);
		xs.alias("JobInfo", JobInfo.class);
		xs.alias("BatchInfo", BatchInfo.class);

		StringBuffer sb = new StringBuffer();
		sb.append(xs.toXML(info));

		FileOutputStream os = new FileOutputStream(file, true);

		BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(os, Charset.forName("UTF-8")));
		bufferedWriter.write(sb.toString());
		bufferedWriter.close();
	}

}
