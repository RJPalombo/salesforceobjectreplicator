package com.rjpalombo.salesforce.objectreplicator.domain;

import java.util.LinkedList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("queries")
public class SOQLQueries {
	
	@XStreamImplicit(itemFieldName="soqlQuery")
	private List<SOQLQuery> queries = new LinkedList<>();

	public List<SOQLQuery> getQueries() {
		return queries;
	}
	public void setQueries(List<SOQLQuery> queries) {
		this.queries = queries;
	}
	
	public static class SOQLQuery {
		
		@XStreamAlias("sObjectName")
		private String sObjectName;
		
		@XStreamAlias("query")
		private String query;
		
		public String getsObjectName() {
			return sObjectName;
		}
		public void setsObjectName(String sObjectName) {
			this.sObjectName = sObjectName;
		}
		
		public String getQuery() {
			return query;
		}
		public void setQuery(String query) {
			this.query = query;
		}
		
	}
	
}
