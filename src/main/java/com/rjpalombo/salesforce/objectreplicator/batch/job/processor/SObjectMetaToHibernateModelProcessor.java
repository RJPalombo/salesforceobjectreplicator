package com.rjpalombo.salesforce.objectreplicator.batch.job.processor;

import java.util.LinkedList;
import java.util.List;

import org.springframework.batch.item.ItemProcessor;

import com.rjpalombo.salesforce.objectreplicator.config.SalesforceConfig;
import com.rjpalombo.salesforce.objectreplicator.domain.HibernateModel;
import com.rjpalombo.salesforce.objectreplicator.domain.HibernateModel.Clazz;
import com.rjpalombo.salesforce.objectreplicator.domain.HibernateModel.Id;
import com.rjpalombo.salesforce.objectreplicator.domain.HibernateModel.Property;
import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.Field;
import com.sforce.soap.partner.FieldType;

public class SObjectMetaToHibernateModelProcessor implements ItemProcessor<DescribeSObjectResult, HibernateModel> {

	List<String> typesToInclude = new LinkedList<>();
	
	public SObjectMetaToHibernateModelProcessor(SalesforceConfig sfdcConfig) {
		for(String fieldType : sfdcConfig.getIncludedfieldtypes()) {
			typesToInclude.add(fieldType.toLowerCase());
		}
	}

	@Override
	public HibernateModel process(DescribeSObjectResult so) throws Exception {
		HibernateModel model = new HibernateModel(so.getName(), so.getName());
		Clazz cls = model.getClazz();
		
		if(so.getIdEnabled()) {
			HibernateModel.Id id = new Id("Id", "Id", "String");
			cls.setId(id);
		}
		
		List<Property> properties = new LinkedList<>();
		for(Field field : so.getFields()) {
			if(!typesToInclude.contains(field.getType().toString().toLowerCase())) {
				continue;
			}
			
			Property property = new Property();
			property.setName(field.getName());
			property.setColumn(field.getName());

			FieldType t = field.getType();
			switch (t.toString().toLowerCase()) {
				case "boolean":
					property.setType("boolean");
					break;
				case "date":
					property.setType("date");
					break;
				case "double":
				case "currency":
				case "percent":
					property.setType("big_decimal");
					break;
				case "integer":
					property.setType("integer");
					break;
				case "datetime":
				case "time":
					property.setType("calendar_date");
				case "base64":
					property.setType("blob");
					break;
				default:
					property.setType("string");
					break;
			}
			properties.add(property);
		}
		cls.setProperties(properties);
		
		return model;
	}

}
