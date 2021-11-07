package org.orienteer.crm.model;

import org.orienteer.core.OClassDomain;
import org.orienteer.core.dao.ODocumentWrapperProvider;
import org.orienteer.core.dao.OrienteerOClass;
import org.orienteer.core.module.IOrienteerModule;
import org.orienteer.transponder.annotation.DefaultValue;
import org.orienteer.transponder.annotation.EntityProperty;
import org.orienteer.transponder.annotation.EntityType;
import org.orienteer.transponder.orientdb.OrientDBProperty;

import com.google.inject.ProvidedBy;
import com.orientechnologies.orient.core.metadata.security.OUser;
import com.twilio.Twilio;

/**
 * DAO to configure/customize CRM Module 
 */
@ProvidedBy(ODocumentWrapperProvider.class)
@EntityType(value = ICRMModuleConfiguration.CLASS_NAME, 
			superTypes = {IOrienteerModule.OMODULE_CLASS},
			orderOffset = 50)
@OrienteerOClass(domain = OClassDomain.SPECIFICATION)
public interface ICRMModuleConfiguration {
	public static final String CLASS_NAME = "CRMModule";
	
	public String getTwilioAccountSID();
	public void setTwilioAccountSID(String value);
	
	public String getTwilioAccountToken();
	public void setTwilioAccountToken(String value);
	
	public String getTwilioEndPoint();
	public void setTwilioEndPoint(String value);
	
	@OrientDBProperty(defaultValue = "false")
	public boolean getRequestStatusReport();
	public void setRequestStatusReport(boolean value);
	
	@EntityProperty(referencedType = OUser.CLASS_NAME)
	public OUser getDefaultManager();
	public void setDefaultManager(OUser value);
	
	public default boolean isConfigValid() {
		return getTwilioAccountSID()!=null && getTwilioAccountToken()!=null && getTwilioEndPoint()!=null;
	}
	
	public default boolean initIfPossible() {
		if(isConfigValid()) {
			Twilio.init(getTwilioAccountSID(), getTwilioAccountToken());
			return true;
		} else return false;
	}
	
}
