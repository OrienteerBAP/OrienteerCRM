package org.orienteer.crm.model;

import org.orienteer.core.dao.ODocumentWrapperProvider;
import org.orienteer.core.dao.dm.IOEnum;
import org.orienteer.transponder.annotation.EntityType;
import org.orienteer.transponder.orientdb.OrientDBProperty;

import com.google.inject.ProvidedBy;

/**
 * Funnel stage of a perticular lead/contact 
 */
@ProvidedBy(ODocumentWrapperProvider.class)
@EntityType(IFunnelStage.CLASS_NAME)
public interface IFunnelStage extends IOEnum {
	public static final String CLASS_NAME = "FunnelStage";
	
	@OrientDBProperty(defaultValue = "false")
	public boolean getDelist();
	public IFunnelStage setDelist(boolean value);
	
	@OrientDBProperty(defaultValue = "false")
	public boolean isDoNotCall();
	public IFunnelStage setDoNotCall(boolean value);
	
	public String getAvatarUrl();
	public IFunnelStage setAvatarUrl(String value);
	
}
