package com.rjpalombo.salesforce.objectreplicator.domain;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("hibernate-mapping")
public class HibernateModel {
	
	@XStreamAlias("class")
	private Clazz clazz;
	
	public HibernateModel(String name, String table) {
		Clazz clazz = new Clazz(name, table);
		this.clazz = clazz;
	}
	
	public Clazz getClazz() {
		return clazz;
	}
	public void setClazz(Clazz clazz) {
		this.clazz = clazz;
	}
	
	public static class Clazz {
		@XStreamAlias("name")
		@XStreamAsAttribute
		private String name;
		
		@XStreamAlias("table")
		@XStreamAsAttribute
		private String table;
		
		@XStreamAlias("id")
		private Id id;
		
		@XStreamImplicit(itemFieldName="property")
		private List<Property> properties;
		
		public Clazz(String name, String table) {
			this.name = name;
			this.table = table;
		}
		
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}

		public String getTable() {
			return table;
		}
		public void setTable(String table) {
			this.table = table;
		}

		public Id getId() {
			return id;
		}
		public void setId(Id id) {
			this.id = id;
		}

		public List<Property> getProperties() {
			return properties;
		}
		public void setProperties(List<Property> properties) {
			this.properties = properties;
		}
		
	}

	public static class Id {
		@XStreamAlias("name")
		@XStreamAsAttribute
		private String name;
		
		@XStreamAlias("column")
		@XStreamAsAttribute
		private String column;
		
		@XStreamAlias("type")
		@XStreamAsAttribute
		private String type;
		
		public Id(String name, String column, String type) {
			this.name = name;
			this.column = column;
			this.type = type;
		}

		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getColumn() {
			return column;
		}
		public void setColumn(String column) {
			this.column = column;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
	}
	
	public static class Property {
		@XStreamAlias("name")
		@XStreamAsAttribute
		private String name;
		
		@XStreamAlias("column")
		@XStreamAsAttribute
		private String column;
		
		@XStreamAlias("type")
		@XStreamAsAttribute
		private String type;

		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getColumn() {
			return column;
		}
		public void setColumn(String column) {
			this.column = column;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
	}

}
