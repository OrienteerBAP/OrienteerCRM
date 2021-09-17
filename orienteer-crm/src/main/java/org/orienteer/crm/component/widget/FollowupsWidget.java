package org.orienteer.crm.component.widget;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.wicket.model.IModel;
import org.apache.wicket.request.UrlRenderer;
import org.apache.wicket.request.cycle.RequestCycle;
import org.orienteer.core.component.property.DisplayMode;
import org.orienteer.core.dao.DAO;
import org.orienteer.core.web.ODocumentPage;
import org.orienteer.core.widget.Widget;
import org.orienteer.crm.model.IDAOCRM;
import org.orienteer.crm.model.ILead;
import org.orienteer.vuecket.extensions.FullCalendar.Event;

import com.google.inject.Inject;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.record.impl.ODocument;

/**
 * Calendar widget for page with all content
 */
@Widget(id="cal-followups", domain="browse", tab = "followups", selector=ILead.CLASS_NAME, order=10, autoEnable=true)
public class FollowupsWidget extends AbstractCalendarContentWidget<OClass> {
	
	@Inject
	protected IDAOCRM dao;
	
	public FollowupsWidget(String id, IModel<OClass> model, IModel<ODocument> widgetDocumentModel) {
		super(id, model, widgetDocumentModel);
	}

	public List<Event> lookupEvents(Date start, Date end) {
		UrlRenderer renderer = RequestCycle.get().getUrlRenderer();
		return dao.getLeadsForFollowups(start, end).stream().map(l -> {
			Event e = new Event();
			e.setTitle(l.getFullName());
			e.setStart(l.getFollowup());
			e.setAllDay(true);
			e.setUrl(renderer.renderRelativeUrl(ODocumentPage.getLinkToTheDocument(DAO.asDocument(l), DisplayMode.VIEW)));
			return e;
		}).collect(Collectors.toList());
	}
	

}
