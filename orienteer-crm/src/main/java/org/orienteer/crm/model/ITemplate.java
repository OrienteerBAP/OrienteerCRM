package org.orienteer.crm.model;

import java.util.List;

import org.orienteer.core.component.visualizer.UIVisualizersRegistry;
import org.orienteer.core.dao.ODocumentWrapperProvider;
import org.orienteer.core.dao.OrienteerOClass;
import org.orienteer.core.dao.OrienteerOProperty;
import org.orienteer.transponder.annotation.EntityProperty;
import org.orienteer.transponder.annotation.EntityType;

import com.google.inject.ProvidedBy;

/**
 * Template to be used to interact with a lead 
 */
@ProvidedBy(ODocumentWrapperProvider.class)
@EntityType(ITemplate.CLASS_NAME)
@OrienteerOClass(displayable = {"name", "template"})
public interface ITemplate {
	public static final String CLASS_NAME = "Template";
	
	public String getName();
	public void setName(String value);
	
	@OrienteerOProperty(visualization = UIVisualizersRegistry.VISUALIZER_TEXTAREA)
	public String getTemplate();
	public void setTemplate(String value);

	@EntityProperty(inverse = "templates")
	public List<ICampaign> getCampaigns();
	public void setCampaigns(List<ICampaign> value);
	
	
	
}
