package org.orienteer.crm.component.command;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.util.string.Strings;
import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.core.OrienteerWebSession;
import org.orienteer.core.component.BootstrapType;
import org.orienteer.core.component.FAIconType;
import org.orienteer.core.component.command.AbstractCheckBoxEnabledModalWindowCommand;
import org.orienteer.core.component.command.AbstractModalWindowCommand;
import org.orienteer.core.component.property.DisplayMode;
import org.orienteer.core.dao.DAO;
import org.orienteer.core.event.SwitchDashboardTabEvent;
import org.orienteer.core.method.OFilter;
import org.orienteer.core.method.OMethod;
import org.orienteer.core.method.filters.PlaceFilter;
import org.orienteer.core.web.AbstractWidgetPage;
import org.orienteer.core.widget.DashboardPanel;
import org.orienteer.core.widget.IDashboardContainer;
import org.orienteer.core.widget.command.modal.AddTabDialog;
import org.orienteer.crm.component.command.modal.AddNoteDialog;
import org.orienteer.crm.model.ILead;

import com.orientechnologies.orient.core.record.impl.ODocument;

/**
 * Command to add a new note to a {@link ILead}
 *
 * @param <T> the type of main object for a {@link DashboardPanel}
 */
@OMethod(order=100, bootstrap = BootstrapType.SUCCESS,
	filters={
			@OFilter(fClass = PlaceFilter.class, fData = "DATA_TABLE"),
	}, selector = ILead.CLASS_NAME)
public class AddNoteCommand<T> extends AbstractCheckBoxEnabledModalWindowCommand<ODocument> {
	private static final long serialVersionUID = 1L;
	
	private IModel<String> noteModel = Model.of((String)null);

	public AddNoteCommand(String id, IModel<ODocument> dashboardDocumentModel) {
		super(id, "command.addnote", dashboardDocumentModel);
		setIcon(FAIconType.plus_square);
		setRequireAtLeastOne(true);
	}

	@Override
	protected void initializeContent(final ModalWindow modal) {
		modal.setTitle(new ResourceModel("command.addnote"));
		modal.setContent(new AddNoteDialog<T>(modal.getContentId(), noteModel) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onAddNote(Optional<AjaxRequestTarget> targetOptional) {
				targetOptional.ifPresent(modal::close);
				
				performMultiAction(targetOptional.orElse(null), getSelected());
				resetSelection();
				noteModel.setObject("");
				targetOptional.ifPresent(target -> target.add(getTable()));
			}
		});
		modal.setAutoSize(true);
		modal.setMinimalWidth(300);
	}
	
	
	@Override
	protected void perfromSingleAction(AjaxRequestTarget target, ODocument object) {
		String timestamp = OrienteerWebApplication.DATE_TIME_CONVERTER
							.convertToString(new Date(), OrienteerWebSession.get().getLocale());
		ILead lead = DAO.provide(ILead.class, object);
		lead.postpandNote(timestamp+": "+noteModel.getObject());
		lead.touchLastActivity();
		DAO.save(lead);
	}

	@Override
	public void onSubmit(AjaxRequestTarget target) {
		super.onSubmit(target);
		Component content = modal.get(modal.getContentId());
		if(content instanceof MarkupContainer) {
			((MarkupContainer)content).visitChildren(FormComponent.class, (c, v) -> {
				if(c.getOutputMarkupId()) {
					target.focusComponent(c);
					v.stop();
				}
			});
		}
	}
}