package org.orienteer.crm.model;

import org.orienteer.core.OClassDomain;
import org.orienteer.core.dao.DAODefaultValue;
import org.orienteer.core.dao.DAOField;
import org.orienteer.core.dao.DAOOClass;
import org.orienteer.core.dao.ODocumentWrapperProvider;
import org.orienteer.core.module.IOrienteerModule;

import com.google.inject.ProvidedBy;
import com.orientechnologies.orient.core.metadata.security.OUser;
import com.twilio.Twilio;

/**
 * DAO to configure/customize CRM Module 
 */
@ProvidedBy(ODocumentWrapperProvider.class)
@DAOOClass(value = ICRMModuleConfiguration.CLASS_NAME, 
			superClasses = {IOrienteerModule.OMODULE_CLASS},
			domain = OClassDomain.SPECIFICATION,
			orderOffset = 50)
public interface ICRMModuleConfiguration {
	public static final String CLASS_NAME = "CRMModule";
	
	public String getTwilioAccountSID();
	public void setTwilioAccountSID(String value);
	
	public String getTwilioAccountToken();
	public void setTwilioAccountToken(String value);
	
	public String getTwilioEndPoint();
	public void setTwilioEndPoint(String value);
	
	@DAOField(defaultValue = "false")
	@DAODefaultValue("false")
	public boolean getRequestStatusReport();
	public void setRequestStatusReport();
	
	@DAOField(linkedClass = OUser.CLASS_NAME)
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
