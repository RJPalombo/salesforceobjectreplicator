package com.rjpalombo.salesforce.objectreplicator.domain;

import java.util.LinkedList;
import java.util.List;

import com.sforce.async.BatchInfo;
import com.sforce.async.JobInfo;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("SFDCJobInfo")
public class SFDCJobInfo {

	@XStreamAlias("JobInfo")
	private JobInfo jobInfo;
	
	@XStreamImplicit(itemFieldName="BatchInfos")
	private List<BatchInfo> batchInfos = new LinkedList<>();
	
	public SFDCJobInfo(JobInfo jobInfo) {
		this.jobInfo = jobInfo;
	}
	
	public JobInfo getJobInfo() {
		return jobInfo;
	}
	public void setJobInfo(JobInfo jobInfo) {
		this.jobInfo = jobInfo;
	}
	
	public List<BatchInfo> getBatchInfos() {
		return batchInfos;
	}
	public void setBatchInfos(List<BatchInfo> batchInfos) {
		this.batchInfos = batchInfos;
	}
	
}
