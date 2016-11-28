package com.rjpalombo.salesforce.objectreplicator.batch.job.reader;

import java.util.LinkedList;
import java.util.List;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import com.rjpalombo.salesforce.objectreplicator.service.SalesforceService;
import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.ws.ConnectionException;

@StepScope
public class SObjectMetaItemReader implements ItemReader<DescribeSObjectResult> {

	private List<DescribeSObjectResult> describeSObjectResults = new LinkedList<>();
	private int nextRecordIndex;
	
	SalesforceService sfService;
	
	public SObjectMetaItemReader(SalesforceService sfService) throws ConnectionException {
		this.sfService = sfService;
		nextRecordIndex = 0;
		for(DescribeSObjectResult sObject : sfService.getReplicatableObjects().values()) {
			describeSObjectResults.add(sObject);
		}
	}
	
	@Override
	public DescribeSObjectResult read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
		DescribeSObjectResult sObject = null;
        if (nextRecordIndex < describeSObjectResults.size()) {
        	sObject = describeSObjectResults.get(nextRecordIndex);
            nextRecordIndex++;
        }
        return sObject;
	}
	
}
