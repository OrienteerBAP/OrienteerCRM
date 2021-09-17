package org.orienteer.crm.component.widget;

import java.util.List;
import java.util.stream.Stream;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.orienteer.core.dao.DAO;
import org.orienteer.core.widget.Widget;
import org.orienteer.crm.model.ICampaign;
import org.orienteer.crm.model.IInteraction;
import org.orienteer.crm.model.IPerson;

import com.orientechnologies.orient.core.record.impl.ODocument;

/**
 * Widget to show interaction within current campaign
 */
@Widget(id="campaign-interactions", domain="document", tab = "interactions", autoEnable = true, selector=ICampaign.CLASS_NAME, order = 40)
public class CampaignInteractionsWidget extends AbstractInteractionsWidget<ODocument> {

	
	public CampaignInteractionsWidget(String id, IModel<ODocument> model, IModel<ODocument> widgetDocumentModel) {
		super(id, model, widgetDocumentModel);
	}

	@Override
	protected Stream<IPerson> getRooms(int limit) {
		return dao.getCampaignRooms(DAO.provide(ICampaign.class, getModelObject()), limit).stream();
	}

}
