package org.orienteer.crm.component.widget;

import java.util.Date;
import java.util.List;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.orienteer.core.component.FAIcon;
import org.orienteer.core.component.FAIconType;
import org.orienteer.core.widget.AbstractWidget;
import org.orienteer.vuecket.descriptor.VueJson;
import org.orienteer.vuecket.extensions.FullCalendar;
import org.orienteer.vuecket.extensions.FullCalendar.Event;

import com.orientechnologies.orient.core.record.impl.ODocument;

/**
 * Abstract class for all calendars widgets
 * @param <T> the type of main data object linked to this widget
 */
@VueJson
public abstract class AbstractCalendarContentWidget<T> extends AbstractWidget<T> {
	
	public AbstractCalendarContentWidget(String id, IModel<T> model, IModel<ODocument> widgetDocumentModel) {
		super(id, model, widgetDocumentModel);
		add(new FullCalendar("fullCalendar") {
			@Override
			public List<Event> lookupEvents(Date start, Date end) {
				return AbstractCalendarContentWidget.this.lookupEvents(start, end);
			}
		});
	}

	@Override
	protected FAIcon newIcon(String id) {
		return new FAIcon(id, FAIconType.calendar);
	}

	@Override
	protected IModel<String> getDefaultTitleModel() {
		return new ResourceModel("widget.calendar");
	}
	
	public abstract List<Event> lookupEvents(Date start, Date end);

}
