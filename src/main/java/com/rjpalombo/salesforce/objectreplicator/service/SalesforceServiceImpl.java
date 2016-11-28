package com.rjpalombo.salesforce.objectreplicator.service;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rjpalombo.salesforce.objectreplicator.config.SalesforceConfig;
import com.sforce.async.AsyncApiException;
import com.sforce.async.BulkConnection;
import com.sforce.soap.partner.DescribeGlobalSObjectResult;
import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

@Service
public class SalesforceServiceImpl implements SalesforceService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private SalesforceConfig config;
	private PartnerConnection connection;
	private BulkConnection bulkConnection;

	@Autowired
	private SalesforceServiceImpl(SalesforceConfig config) throws ConnectionException, AsyncApiException {
		this.config = config;

		ConnectorConfig partnerConfig = new ConnectorConfig();
		partnerConfig.setAuthEndpoint(config.getAuthendpoint() + config.getApiversion());
		partnerConfig.setUsername(config.getUsername());
		partnerConfig.setPassword(config.getPassword());
		partnerConfig.setPrettyPrintXml(true);
		this.connection = new PartnerConnection(partnerConfig);
		
		String soapEndpoint = partnerConfig.getServiceEndpoint();
		String restEndpoint = soapEndpoint.substring(0, soapEndpoint.indexOf("Soap/")) + "async/" + config.getApiversion();
		partnerConfig.setRestEndpoint(restEndpoint);
		this.bulkConnection = new BulkConnection(partnerConfig);
	}
	
	public PartnerConnection getConnection() {
		return connection;
	}

	public BulkConnection getBulkConnection() {
		return bulkConnection;
	}

	public Map<String, DescribeGlobalSObjectResult> getAvailableObjects() throws ConnectionException {
		Map<String, DescribeGlobalSObjectResult> grMap = new LinkedHashMap<>();
		for (DescribeGlobalSObjectResult gr : connection.describeGlobal().getSobjects()) {
			grMap.put(gr.getName(), gr);
		}
		return grMap;
	}

	public Map<String, DescribeSObjectResult> getReplicatableObjects() throws ConnectionException {
		Map<String, DescribeGlobalSObjectResult> availableObjects = getAvailableObjects();
		Map<String, DescribeSObjectResult> soMap = new LinkedHashMap<>();
		for (String sObjectName : config.getIncludedobjects()) {
			String caseSensitiveObjectName = caseSensitiveStringValueFromArray(sObjectName,
					availableObjects.keySet().toArray(new String[availableObjects.keySet().size()]));
			if (caseSensitiveObjectName.isEmpty()) {
				logger.warn(sObjectName + " is not listed in your Salesforce org");
				continue;
			}
			DescribeGlobalSObjectResult globalSObject = availableObjects.get(caseSensitiveObjectName);
			if (!globalSObject.getReplicateable() || !globalSObject.getQueryable() || !globalSObject.getRetrieveable()
					|| globalSObject.getDeprecatedAndHidden()) {
				logger.warn(globalSObject.getName() + " is not replicatable");
				continue;
			}
			DescribeSObjectResult sObject = describeSObject(globalSObject.getName());
			soMap.put(caseSensitiveObjectName, sObject);
		}
		return soMap;
	}

	public DescribeSObjectResult describeSObject(String sObject) throws ConnectionException {
		return connection.describeSObject(sObject);
	}

	public Map<String, DescribeSObjectResult> describeSObjects(String[] sObjects) throws ConnectionException {
		Map<String, DescribeSObjectResult> dsorMap = new LinkedHashMap<>();
		for (String sObjectName : sObjects) {
			DescribeSObjectResult sObject = describeSObject(sObjectName);
			dsorMap.put(sObjectName, sObject);
		}
		return dsorMap;
	}

	// Bulk

	// Helpers
	private String caseSensitiveStringValueFromArray(String value, String[] array) {
		String lowerVal = value.toLowerCase();
		String retVal = "";
		for (String arrayVal : array) {
			if (arrayVal.toLowerCase().equals(lowerVal)) {
				retVal = arrayVal;
				break;
			}
		}
		return retVal;
	}

}
