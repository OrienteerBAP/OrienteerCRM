package org.orienteer.crm.model;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.orienteer.core.component.visualizer.UIVisualizersRegistry;
import org.orienteer.core.dao.DAO;
import org.orienteer.core.dao.DAOField;
import org.orienteer.core.dao.DAOFieldIndex;
import org.orienteer.core.dao.DAOHandler;
import org.orienteer.core.dao.DAOOClass;
import org.orienteer.core.dao.IMethodHandler;
import org.orienteer.core.dao.ODocumentWrapperProvider;
import org.orienteer.core.dao.handler.InvocationChain;

import com.google.inject.ProvidedBy;
import com.orientechnologies.orient.core.metadata.schema.OClass.INDEX_TYPE;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.metadata.security.OUser;
import com.orientechnologies.orient.core.type.ODocumentWrapper;

/**
 * Contact/Lead: heart of any CRM system
 */
@ProvidedBy(ODocumentWrapperProvider.class)
@DAOOClass(value = ILead.CLASS_NAME, 
		   nameProperty = "lastName",
		   orderOffset = 200,
		   displayable = {"score", "manager", "note", "followup", "funnelStage", "lastActivity"},
		   sortProperty = "lastActivity",
		   sortOrder = SortOrder.DESCENDING,
		   defaultTab = "funnel")
public interface ILead extends IPerson {
	public static final String CLASS_NAME = "Lead";
	
	/**
	 * {@link IMethodHandler} to update automatically lastActivity
	 */
	public static class UpdateLastActivity implements IMethodHandler<ODocumentWrapper> {

		private static final long serialVersionUID = 1L;

		@Override
		public Optional<Object> handle(ODocumentWrapper target, Object proxy, Method method, Object[] args,
				InvocationChain<ODocumentWrapper> chain) throws Throwable {
			Optional<Object> ret = chain.handle(target, proxy, method, args);
			IInteraction interaction = (IInteraction) args[0];
			if(interaction!=null) ((ILead) proxy).setLastActivity(interaction.getTimestamp());
			return ret;
		}
    	
    }
	
	public String getExternalId();
	public ILead setExternalId(String value);
	
	@DAOField(tab = "funnel")
	public Integer getScore();
	public ILead setScore(Integer value);
	
	@DAOField(linkedClass = OUser.CLASS_NAME, tab="funnel")
	public OUser getManager();
	public void setManager(OUser value);
	
	@DAOField(tab = "funnel", visualization = UIVisualizersRegistry.VISUALIZER_TEXTAREA)
	public String getNote();
	public ILead setNote(String value);
	
	@DAOField(type = OType.BINARY, notNull = false, tab="funnel")
	public byte[] getCma();
	public ILead setCma(byte[] value);
	
	@DAOField(tab = "funnel")
	public Double getFmv();
	public ILead setFmv(Double value);
	
	@DAOField(tab = "funnel")
	public Double getArv();
	public ILead setArv(Double value);
	
	@DAOField(tab = "funnel")
	public Double getFixUps();
	public ILead setFixUps(Double value);
	
//	@DAOField(uiReadOnly = true, script = "arv - fmv - fixUps")
	@DAOField(tab = "funnel")
	public Double getEquity();
	public ILead setEquity(Double value);

	@DAOField(visualization = UIVisualizersRegistry.VISUALIZER_LISTBOX, tab="funnel")
	public IFunnelStage getFunnelStage();
	public ILead setFunnelStage(IFunnelStage value);
	
	@DAOField(type=OType.DATE, tab="funnel")
	public Date getFollowup();
	public ILead setFollowup(Date value);
	
	@DAOField(tab = "funnel", uiReadOnly = true)
	public IInteraction getLastInteraction();
	@DAOHandler(UpdateLastActivity.class)
	public ILead setLastInteraction(IInteraction value);
	
	@DAOField(tab = "funnel", uiReadOnly = true)
	@DAOFieldIndex(type = INDEX_TYPE.NOTUNIQUE)
	public Date getLastActivity();
	public void setLastActivity(Date value);
	
	@DAOField(uiReadOnly = true, inverse = "leads", visualization = UIVisualizersRegistry.VISUALIZER_TABLE, tab="campaigns")
	public List<ICampaign> getCampaigns();
	
	public default ILead prepandNote(String prepand) {
		String note = getNote();
		setNote(note!=null?prepand+note:prepand);
		return this;
	}
	
	public default ILead postpandNote(String postpand) {
		String note = getNote();
		setNote(note!=null?note+"\n"+postpand:postpand);
		return this;
	}
	
	public default ILead touchLastActivity() {
		setLastActivity(new Date());
		return this;
	}
}
