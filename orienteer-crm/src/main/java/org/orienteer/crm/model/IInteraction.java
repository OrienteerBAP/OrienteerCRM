package org.orienteer.crm.model;

import java.util.Date;

import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.orienteer.core.dao.DAO;
import org.orienteer.core.dao.DAOField;
import org.orienteer.core.dao.DAOFieldIndex;
import org.orienteer.core.dao.DAOOClass;
import org.orienteer.core.dao.Lookup;
import org.orienteer.core.dao.ODocumentWrapperProvider;

import com.google.inject.ProvidedBy;
import com.orientechnologies.orient.core.metadata.schema.OClass.INDEX_TYPE;

/**
 * Single interaction with or by lead/contact
 */
@ProvidedBy(ODocumentWrapperProvider.class)
@DAOOClass(value = IInteraction.CLASS_NAME,
		   sortProperty = "timestamp",
		   sortOrder = SortOrder.DESCENDING, 
		   nameProperty = "timestamp")
public interface IInteraction {
	public static final String CLASS_NAME = "Interaction";
	
	@DAOField(uiReadOnly = true)
	public Date getTimestamp();
	public IInteraction setTimestamp(Date value);
	
	@DAOField(uiReadOnly = true)
	@DAOFieldIndex(type = INDEX_TYPE.NOTUNIQUE)
	public IPerson getFrom();
	public IInteraction setFrom(IPerson value);
	
	@DAOField(uiReadOnly = true)
	@DAOFieldIndex(type = INDEX_TYPE.NOTUNIQUE)
	public String getFromPhone();
	public IInteraction setFromPhone(String value);
	
	@DAOField(uiReadOnly = true)
	@DAOFieldIndex(type = INDEX_TYPE.NOTUNIQUE)
	public IPerson getTo();
	public IInteraction setTo(IPerson value);
	
	@DAOField(uiReadOnly = true)
	@DAOFieldIndex(type = INDEX_TYPE.NOTUNIQUE)
	public String getToPhone();
	public IInteraction setToPhone(String value);
	
	@DAOField(uiReadOnly = true, collate = "ci")
	public String getContent();
	public IInteraction setContent(String value);
	
	@DAOField(uiReadOnly = true)
	public InteractionStatus getStatus();
	public IInteraction setStatus(InteractionStatus value);

	@DAOField(uiReadOnly = true)
	public String getStatusDetails();
	public IInteraction setStatusDetails(String value);
	
	@DAOField(uiReadOnly = true)
	@DAOFieldIndex(type = INDEX_TYPE.NOTUNIQUE)
	public String getExternalId();
	public IInteraction setExternalId(String value);

	@DAOField(uiReadOnly = true)
	public ITemplate getTemplate();
	public IInteraction setTemplate(ITemplate value);
	
	@DAOField(uiReadOnly = true, inverse = "interactions")
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
