package org.orienteer.crm.model;

import java.util.Date;

import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.orienteer.core.dao.DAO;
import org.orienteer.core.dao.ODocumentWrapperProvider;
import org.orienteer.core.dao.OrienteerOClass;
import org.orienteer.core.dao.OrienteerOProperty;
import org.orienteer.transponder.annotation.EntityProperty;
import org.orienteer.transponder.annotation.EntityPropertyIndex;
import org.orienteer.transponder.annotation.EntityType;
import org.orienteer.transponder.annotation.Lookup;
import org.orienteer.transponder.orientdb.ODriver;
import org.orienteer.transponder.orientdb.OrientDBProperty;

import com.google.inject.ProvidedBy;
import com.orientechnologies.orient.core.metadata.schema.OClass.INDEX_TYPE;

/**
 * Single interaction with or by lead/contact
 */
@ProvidedBy(ODocumentWrapperProvider.class)
@EntityType(value = IInteraction.CLASS_NAME)
@OrienteerOClass(sortProperty = "timestamp",
				 sortOrder = SortOrder.DESCENDING, 
				 nameProperty = "timestamp")
public interface IInteraction {
	public static final String CLASS_NAME = "Interaction";
	
	@OrienteerOProperty(uiReadOnly = true)
	public Date getTimestamp();
	public IInteraction setTimestamp(Date value);
	
	@OrienteerOProperty(uiReadOnly = true)
	@EntityPropertyIndex(type = ODriver.OINDEX_NOTUNIQUE)
	public IPerson getFrom();
	public IInteraction setFrom(IPerson value);
	
	@OrienteerOProperty(uiReadOnly = true)
	@EntityPropertyIndex(type = ODriver.OINDEX_NOTUNIQUE)
	public String getFromPhone();
	public IInteraction setFromPhone(String value);
	
	@OrienteerOProperty(uiReadOnly = true)
	@EntityPropertyIndex(type = ODriver.OINDEX_NOTUNIQUE)
	public IPerson getTo();
	public IInteraction setTo(IPerson value);
	
	@OrienteerOProperty(uiReadOnly = true)
	@EntityPropertyIndex(type = ODriver.OINDEX_NOTUNIQUE)
	public String getToPhone();
	public IInteraction setToPhone(String value);
	
	@OrientDBProperty(collate = "ci")
	@OrienteerOProperty(uiReadOnly = true)
	public String getContent();
	public IInteraction setContent(String value);
	
	@OrienteerOProperty(uiReadOnly = true)
	public InteractionStatus getStatus();
	public IInteraction setStatus(InteractionStatus value);

	@OrienteerOProperty(uiReadOnly = true)
	public String getStatusDetails();
	public IInteraction setStatusDetails(String value);
	
	@OrienteerOProperty(uiReadOnly = true)
	@EntityPropertyIndex(type = ODriver.OINDEX_NOTUNIQUE)
	public String getExternalId();
	public IInteraction setExternalId(String value);

	@OrienteerOProperty(uiReadOnly = true)
	public ITemplate getTemplate();
	public IInteraction setTemplate(ITemplate value);
	
	@EntityProperty(inverse = "interactions")
	@OrienteerOProperty(uiReadOnly = true)
	public ICampaign getCampaign();
	public IInteraction setCampaign(ICampaign value);
	
	public default boolean normilizePhones(boolean doSave) {
		boolean save = false;
		if(getFromPhone()==null && getFrom()!=null) {
			setFromPhone(getFrom().getPhone());
			save = true;
		}
		if(getToPhone()==null && getTo()!=null) {
			setToPhone(getTo().getPhone());
			save = true;
		}
		if(save && doSave) DAO.save(this);
		return save;
	}
	
	@Lookup("select from "+IInteraction.CLASS_NAME+" where externalId = :externalId")
	public boolean lookupByExternalId(String externalId);
}
