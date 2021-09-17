package org.orienteer.crm.model;

import java.util.List;

import org.orienteer.core.component.visualizer.UIVisualizersRegistry;
import org.orienteer.core.dao.DAOField;
import org.orienteer.core.dao.DAOOClass;
import org.orienteer.core.dao.ODocumentWrapperProvider;

import com.google.inject.ProvidedBy;

/**
 * Template to be used to interact with a lead 
 */
@ProvidedBy(ODocumentWrapperProvider.class)
@DAOOClass(value = ITemplate.CLASS_NAME,
		   displayable = {"name", "template"})
public interface ITemplate {
	public static final String CLASS_NAME = "Template";
	
	public String getName();
	public void setName(String value);
	
	@DAOField(visualization = UIVisualizersRegistry.VISUALIZER_TEXTAREA)
	public String getTemplate();
	public void setTemplate(String value);

	@DAOField(inverse = "templates")
	public List<ICampaign> getCampaigns();
	public void setCampaigns(List<ICampaign> value);
	
	
	
}
