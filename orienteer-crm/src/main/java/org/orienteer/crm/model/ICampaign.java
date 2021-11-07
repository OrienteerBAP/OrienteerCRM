package org.orienteer.crm.model;

import java.util.Date;
import java.util.List;

import org.apache.wicket.core.util.string.interpolator.PropertyVariableInterpolator;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.feedback.FeedbackMessage;
import org.orienteer.core.OrienteerWebSession;
import org.orienteer.core.component.BootstrapType;
import org.orienteer.core.component.FAIconType;
import org.orienteer.core.component.visualizer.UIVisualizersRegistry;
import org.orienteer.core.dao.DAO;
import org.orienteer.core.dao.ODocumentWrapperProvider;
import org.orienteer.core.dao.OrienteerOClass;
import org.orienteer.core.dao.OrienteerOProperty;
import org.orienteer.core.method.IMethodContext;
import org.orienteer.core.method.OFilter;
import org.orienteer.core.method.OMethod;
import org.orienteer.core.method.filters.PlaceFilter;
import org.orienteer.core.method.filters.WidgetTypeFilter;
import org.orienteer.transponder.annotation.DefaultValue;
import org.orienteer.transponder.annotation.EntityProperty;
import org.orienteer.transponder.annotation.EntityType;
import org.orienteer.transponder.orientdb.OrientDBProperty;

import com.google.inject.ProvidedBy;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;

/**
 * Marketing campaign 
 */
@ProvidedBy(ODocumentWrapperProvider.class)
@EntityType(value = ICampaign.CLASS_NAME)
@OrienteerOClass(
		   displayable = {"name", "status", "created", "startTime"},
		   sortProperty = "created",
		   sortOrder = SortOrder.DESCENDING)
public interface ICampaign {
	public static final String CLASS_NAME = "Campaign";
	
	public String getName();
	public void setName(String value);
	
	@OrientDBProperty(defaultValue = "DRAFT")
	@OrienteerOProperty(uiReadOnly = true)
	public CampaignStatus getStatus();
	public void setStatus(CampaignStatus value);
	
	@OrientDBProperty(type = OType.DATETIME, defaultValue = "sysdate()", readOnly = true)
	public Date getCreated();
	public void setCreated(Date value);
	
	@EntityProperty(inverse = "campaigns")
	@OrienteerOProperty(visualization = UIVisualizersRegistry.VISUALIZER_TABLE, tab = "leads")
	public List<ILead> getLeads();
	public void setLeads(List<ILead> value);
	
	@EntityProperty(inverse = "campaigns")
	@OrienteerOProperty(visualization = UIVisualizersRegistry.VISUALIZER_LISTBOX)
	public List<ITemplate> getTemplates();
	public void setTemplates(List<ITemplate> value);

	@OrientDBProperty(defaultValue = "50")
	public int getInteractionsPerTemplate();
	public void setInteractionsPerTemplate(int value);
	
	@OrientDBProperty(defaultValue = "10")
	public int getRate();
	public void setRate(int value);
	
	@OrientDBProperty(type = OType.DATETIME)
	public Date getStartTime();
	public void setStartTime(Date value);
	
	@EntityProperty(referencedType = "OUser")
	@OrientDBProperty( defaultValue = "ouser()", readOnly = true)
	public IPerson getAuthor();
	public void setAuthor(IPerson value);
	
	@OrienteerOProperty(visualization = UIVisualizersRegistry.VISUALIZER_LISTBOX)
	public IFunnelStage getFunnelStage();
	public void setFunnelStage(IFunnelStage value);
	
	@OrienteerOProperty(visualization = UIVisualizersRegistry.VISUALIZER_LISTBOX)
	public IFunnelStage getErrorFunnelStage();
	public void setErrorFunnelStage(IFunnelStage value);
	
	
	@EntityProperty(inverse = "campaign")
	@OrienteerOProperty(visualization = UIVisualizersRegistry.VISUALIZER_TABLE, tab="startingInteractions")
	public List<IInteraction> getInteractions();
	public void setInteractions(List<IInteraction> value);
	
	
	@OMethod(
			titleKey = "command.schedule", 
			order=10,bootstrap=BootstrapType.SUCCESS,icon = FAIconType.play,
			filters={
					@OFilter(fClass = PlaceFilter.class, fData = "STRUCTURE_TABLE"),
					@OFilter(fClass = WidgetTypeFilter.class, fData = "parameters"),
			}
	)
	public default void schedule(IMethodContext ctx) {
		Date startTime = getStartTime();
		Date now = new Date();
		if(startTime==null || startTime.before(now)) {
			startTime = now;
			setStartTime(startTime);
		}
		
		List<ILead> leads = getLeads();
		List<ITemplate> templates = getTemplates();
		int rate = getRate();
		int interactionsPerTemplate = getInteractionsPerTemplate();
		//Validate first
		if(leads==null || leads.isEmpty()) {
			ctx.showFeedback(FeedbackMessage.ERROR, "error.noleads", null);
			return;
		}
		if(templates==null || templates.isEmpty()) {
			ctx.showFeedback(FeedbackMessage.ERROR, "error.notemplates", null);
			return;
		}
		if(templates.size()*interactionsPerTemplate<leads.size()) {
			ctx.showFeedback(FeedbackMessage.ERROR, "error.notenoughtemplates", null);
			return;
		}
		setStatus(CampaignStatus.ACTIVE);
		DAO.save(this);
		IPerson sentFrom = DAO.provide(IPerson.class,OrienteerWebSession.get().getUserAsODocument()); 
		for(int i=0; i< leads.size(); i++) {
			ILead lead = leads.get(i);
			ITemplate template = templates.get( i % templates.size());
			long timeDelta = i * (60*1000/rate);
			Date sendTimeStamp = new Date(startTime.getTime()+timeDelta);
			IInteraction interaction = DAO.create(IInteraction.class);
			String content = new PropertyVariableInterpolator(template.getTemplate(), lead) {
				protected String getValue(String variableName) {
					String value = super.getValue(variableName);
					return value==null?"":value;
				};
			}.toString();
			interaction.setCampaign(this)
					   .setStatus(InteractionStatus.PLANNED)
					   .setTo(lead)
					   .setFrom(sentFrom)
					   .setTemplate(template)
					   .setTimestamp(sendTimeStamp)
					   .setContent(content);
			DAO.save(interaction);
		}
	}
	
}
