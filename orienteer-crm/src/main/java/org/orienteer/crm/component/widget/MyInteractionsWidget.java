package org.orienteer.crm.component.widget;

import java.util.List;
import java.util.stream.Stream;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.orienteer.core.OrienteerWebSession;
import org.orienteer.core.dao.DAO;
import org.orienteer.core.widget.Widget;
import org.orienteer.crm.model.IInteraction;
import org.orienteer.crm.model.ILead;
import org.orienteer.crm.model.IPerson;

import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.record.impl.ODocument;

/**
 * Widget to show all interactions which current user had before or involved into 
 */
@Widget(id="my-interactions", domain="browse", tab = "myInteractions", autoEnable = true, selector=IInteraction.CLASS_NAME, order = 40)
public class MyInteractionsWidget extends AbstractInteractionsWidget<OClass> {

	
	public MyInteractionsWidget(String id, IModel<OClass> model, IModel<ODocument> widgetDocumentModel) {
		super(id, model, widgetDocumentModel);
	}

	@Override
	protected Stream<IPerson> getRooms(int limit) {
		return dao.getManagersRooms(DAO.provide(IPerson.class, OrienteerWebSession.get().getUserAsODocument()), limit)
					.stream().filter(p -> p instanceof ILead);
	}

}
