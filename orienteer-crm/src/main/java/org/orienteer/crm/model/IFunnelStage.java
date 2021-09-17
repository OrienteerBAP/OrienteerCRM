package org.orienteer.crm.model;

import org.orienteer.core.dao.DAODefaultValue;
import org.orienteer.core.dao.DAOField;
import org.orienteer.core.dao.DAOOClass;
import org.orienteer.core.dao.Lookup;
import org.orienteer.core.dao.ODocumentWrapperProvider;
import org.orienteer.core.dao.dm.IOEnum;

import com.google.inject.ProvidedBy;

/**
 * Funnel stage of a perticular lead/contact 
 */
@ProvidedBy(ODocumentWrapperProvider.class)
@DAOOClass(IFunnelStage.CLASS_NAME)
public interface IFunnelStage extends IOEnum {
	public static final String CLASS_NAME = "FunnelStage";
	
	@DAOField(defaultValue = "false")
	@DAODefaultValue("false")
	public boolean getDelist();
	public IFunnelStage setDelist(boolean value);
	
	@DAOField(defaultValue = "false")
	@DAODefaultValue("false")
	public boolean isDoNotCall();
	public IFunnelStage setDoNotCall(boolean value);
	
	public String getAvatarUrl();
	public IFunnelStage setAvatarUrl(String value);
	
}
