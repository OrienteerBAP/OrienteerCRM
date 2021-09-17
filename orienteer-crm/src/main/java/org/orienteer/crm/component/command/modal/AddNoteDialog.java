package org.orienteer.crm.component.command.modal;

import java.util.Optional;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.orienteer.core.widget.DashboardPanel;
import org.orienteer.crm.model.ILead;


/**
 * Dialog to enter additional note to be added to a {@link ILead}
 *
 * @param <T> the type of main data object
 */
public abstract class AddNoteDialog<T> extends GenericPanel<String> {
	
	public AddNoteDialog(String id, IModel<String> noteModel) {
		super(id, noteModel);
		Form<T> form = new Form<T>("addNoteForm");
		form.add(new TextArea<String>("note", noteModel).setOutputMarkupId(true));
		form.add(new AjaxButton("addNote") {
			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				onAddNote(Optional.of(target));
			}
		});
		add(form);
	}
	
	protected abstract void onAddNote(Optional<AjaxRequestTarget> targetOptional);

}
