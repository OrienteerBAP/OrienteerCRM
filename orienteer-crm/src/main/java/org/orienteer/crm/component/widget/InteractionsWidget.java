package org.orienteer.crm.component.widget;

import java.util.List;
import java.util.stream.Stream;

import org.apache.wicket.model.IModel;
import org.orienteer.core.widget.Widget;
import org.orienteer.crm.model.IInteraction;
import org.orienteer.crm.model.ILead;
import org.orienteer.crm.model.IPerson;

import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.record.impl.ODocument;

/**
 * Widget to show all interactions with all possible or not leads
 */
@Widget(id="interactions", domain="browse", tab = "allLeadInteractions", autoEnable = true,selector=IInteraction.CLASS_NAME, order = 50)
public class InteractionsWidget extends AbstractInteractionsWidget<OClass> {

	public InteractionsWidget(String id, IModel<OClass> model, IModel<ODocument> widgetDocumentModel) {
		super(id, model, widgetDocumentModel);
	}
	
	@Override
	protected Stream<IPerson> getRooms(int limit) {
		return dao.getAllLeadsRooms(limit).stream();
	}
}
