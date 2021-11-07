package org.orienteer.crm.model;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.orienteer.core.component.visualizer.UIVisualizersRegistry;
import org.orienteer.core.dao.DAO;
import org.orienteer.core.dao.ODocumentWrapperProvider;
import org.orienteer.core.dao.OrienteerOClass;
import org.orienteer.core.dao.OrienteerOProperty;
import org.orienteer.transponder.annotation.AdviceAnnotation;
import org.orienteer.transponder.annotation.EntityProperty;
import org.orienteer.transponder.annotation.EntityPropertyIndex;
import org.orienteer.transponder.annotation.EntityType;
import org.orienteer.transponder.orientdb.ODriver;
import org.orienteer.transponder.orientdb.OrientDBProperty;

import com.google.inject.ProvidedBy;
import com.orientechnologies.orient.core.metadata.schema.OClass.INDEX_TYPE;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.metadata.security.OUser;
import com.orientechnologies.orient.core.type.ODocumentWrapper;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.Advice.*;

/**
 * Contact/Lead: heart of any CRM system
 */
@ProvidedBy(ODocumentWrapperProvider.class)
@EntityType(value = ILead.CLASS_NAME, orderOffset = 200)
@OrienteerOClass(nameProperty = "lastName",
		   displayable = {"score", "manager", "note", "followup", "funnelStage", "lastActivity"},
		   sortProperty = "lastActivity",
		   sortOrder = SortOrder.DESCENDING,
		   defaultTab = "funnel")
public interface ILead extends IPerson {
	public static final String CLASS_NAME = "Lead";
	
	/**
	 * Advice to update automatically lastActivity
	 */
	public static class UpdateLastActivity {
		
		@Advice.OnMethodExit
		public static void handle(@Advice.This ILead thisLead, @Advice.Argument(0) IInteraction interaction ) {
			thisLead.setLastActivity(interaction.getTimestamp());
		}
		
    }
	
	public String getExternalId();
	public ILead setExternalId(String value);
	
	@OrienteerOProperty(tab = "funnel")
	public Integer getScore();
	public ILead setScore(Integer value);
	
	@EntityProperty(referencedType = OUser.CLASS_NAME)
	@OrienteerOProperty(tab="funnel")
	public OUser getManager();
	public void setManager(OUser value);
	
	@OrienteerOProperty(tab = "funnel", visualization = UIVisualizersRegistry.VISUALIZER_TEXTAREA)
	public String getNote();
	public ILead setNote(String value);
	
	@OrientDBProperty(type = OType.BINARY, notNull = false)
	@OrienteerOProperty(tab="funnel")
	public byte[] getCma();
	public ILead setCma(byte[] value);
	
	@OrienteerOProperty(tab = "funnel")
	public Double getFmv();
	public ILead setFmv(Double value);
	
	@OrienteerOProperty(tab = "funnel")
	public Double getArv();
	public ILead setArv(Double value);
	
	@OrienteerOProperty(tab = "funnel")
	public Double getFixUps();
	public ILead setFixUps(Double value);
	
//	@DAOField(uiReadOnly = true, script = "arv - fmv - fixUps")
	@OrienteerOProperty(tab = "funnel")
	public Double getEquity();
	public ILead setEquity(Double value);

	@OrienteerOProperty(visualization = UIVisualizersRegistry.VISUALIZER_LISTBOX, tab="funnel")
	public IFunnelStage getFunnelStage();
	public ILead setFunnelStage(IFunnelStage value);
	
	@OrientDBProperty(type=OType.DATE)
	@OrienteerOProperty(tab="funnel")
	public Date getFollowup();
	public ILead setFollowup(Date value);
	
	@OrienteerOProperty(tab = "funnel", uiReadOnly = true)
	public IInteraction getLastInteraction();
	@AdviceAnnotation(UpdateLastActivity.class)
	public ILead setLastInteraction(IInteraction value);
	
	@EntityPropertyIndex(type = ODriver.OINDEX_NOTUNIQUE)
	@OrienteerOProperty(tab = "funnel", uiReadOnly = true)
	public Date getLastActivity();
	public void setLastActivity(Date value);
	
	@EntityProperty(inverse = "leads")
	@OrienteerOProperty(uiReadOnly = true, visualization = UIVisualizersRegistry.VISUALIZER_TABLE, tab="campaigns")
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
