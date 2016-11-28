package com.rjpalombo.salesforce.objectreplicator.service;

import java.util.Map;

import com.sforce.async.BulkConnection;
import com.sforce.soap.partner.DescribeGlobalSObjectResult;
import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;

public interface SalesforceService {
	public PartnerConnection getConnection();
	public BulkConnection getBulkConnection();
	public Map<String, DescribeGlobalSObjectResult> getAvailableObjects() throws ConnectionException;
	public Map<String, DescribeSObjectResult> getReplicatableObjects() throws ConnectionException;
	public DescribeSObjectResult describeSObject(String sObject) throws ConnectionException;
	public Map<String, DescribeSObjectResult> describeSObjects(String[] sObjects) throws ConnectionException;
}
