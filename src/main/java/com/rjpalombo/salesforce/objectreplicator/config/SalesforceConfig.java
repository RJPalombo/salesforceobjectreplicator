package com.rjpalombo.salesforce.objectreplicator.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix="salesforce")
public class SalesforceConfig {

	private String authEndpoint;
	private String apiVersion;
	private String username;
	private String password;
	
	@Value("#{'${salesforce.includedfieldtypes}'.split(',')}") 
	private String[] includedfieldtypes;
	
	@Value("#{'${salesforce.includedobjects}'.split(',')}") 
	private String[] includedobjects;
	
	public String getAuthendpoint() {
		return authEndpoint;
	}
	public void setAuthendpoint(String authendpoint) {
		this.authEndpoint = authendpoint;
	}
	
	public String getApiversion() {
		return apiVersion;
	}
	public void setApiversion(String apiversion) {
		this.apiVersion = apiversion;
	}
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	public String[] getIncludedfieldtypes() {
		return includedfieldtypes;
	}
	public void setIncludedfieldtypes(String[] includedfieldtypes) {
		this.includedfieldtypes = includedfieldtypes;
	}
	
	public String[] getIncludedobjects() {
		return includedobjects;
	}
	public void setIncludedobjects(String[] includedobjects) {
		this.includedobjects = includedobjects;
	}
	
}
