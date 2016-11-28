package com.rjpalombo.salesforce.objectreplicator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix="application")
public class ApplicationConfig {

	private String datadirectory;

	public String getDatadirectory() {
		return datadirectory;
	}
	public void setDatadirectory(String dataDirectory) {
		this.datadirectory = dataDirectory;
	}
	
}
