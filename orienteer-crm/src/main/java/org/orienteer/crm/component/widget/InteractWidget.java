package org.orienteer.crm.component.widget;

import java.util.Date;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.StringResourceModel;
import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.core.OrienteerWebSession;
import org.orienteer.core.component.FAIcon;
import org.orienteer.core.component.FAIconType;
import org.orienteer.core.dao.DAO;
import org.orienteer.core.model.DAOModel;
import org.orienteer.core.widget.AbstractWidget;
import org.orienteer.core.widget.Widget;
import org.orienteer.crm.model.IDAOCRM;
import org.orienteer.crm.model.IInteraction;
import org.orienteer.crm.model.ILead;
import org.orienteer.crm.model.IPerson;
import org.orienteer.crm.model.InteractionStatus;
import org.orienteer.crm.service.ISMSService;
import org.orienteer.vuecket.VueBehavior;
import org.orienteer.vuecket.extensions.VueAdvancedChat;
import org.orienteer.vuecket.extensions.VueAdvancedChat.SendMessage;
import org.orienteer.vuecket.method.IVuecketMethod;

import com.google.inject.Inject;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.twilio.rest.api.v2010.account.Message;

import lombok.extern.slf4j.Slf4j;

/**
 * Widget ti interact with a lead/person
 */
@Widget(id="interact", domain="document", tab="interact", autoEnable = true,selector=IPerson.CLASS_NAME, order=10)
@Slf4j
public class InteractWidget extends AbstractWidget<ODocument> {
	
	@Inject
	private IDAOCRM dao;

	public InteractWidget(String id, IModel<ODocument> model, IModel<ODocument> widgetDocumentModel) {
		super(id, model, widgetDocumentModel);
		add(new VueBehavior());
		add(new VueAdvancedChat("chat", dao.getRoomsForPerson(DAOModel.of(model.getObject())), 
				LoadableDetachableModel.of(() -> dao.getConversationAsChatMessages(getModelObject()))) {

					@Override
					public void onSendMessage(IVuecketMethod.Context ctx, SendMessage message) {
						sendMessage(ctx, message);
					}

					@Override
					public String getCurrentUserId() {
						return OrienteerWebSession.get().isSignedIn()
								? OrienteerWebSession.get().getUserAsODocument().getIdentity().toString()
								: null;
					}
			
		});
	}
	
	public static void sendMessage(IVuecketMethod.Context ctx, SendMessage message) {
		
		log.info("Message Recieved: "+message);
		IPerson person = DAO.provide(IPerson.class, new ORecordId(message.getRoomId().toString()));
		IInteraction interaction = DAO.create(IInteraction.class)
									.setContent(message.getContent())
									.setFrom(DAO.provide(IPerson.class, OrienteerWebSession.get().getUserAsODocument()))
									.setStatus(InteractionStatus.CREATED)
									.setTo(person)
									.setToPhone(person.getPhone())
									.setTimestamp(new Date());
		OrienteerWebApplication.get().getServiceInstance(ISMSService.class).tryToSend(interaction);
		DAO.save(interaction);
		if(person instanceof ILead) {
			ILead lead = (ILead)person;
			lead.setLastInteraction(interaction);
			DAO.save(lead);
		}
		OrienteerWebSession.get().getDatabaseSession().commit();
		OrienteerWebSession.get().getDatabaseSession().begin();
		ctx.getVueBehavior().getDataFibers().detach();
		IVuecketMethod.pushDataFibers(ctx, false, "messages", "messages-loaded");
		
	}

	@Override
	protected FAIcon newIcon(String id) {
		return new FAIcon(id, FAIconType.file_text);
	}

	@Override
	protected IModel<String> getDefaultTitleModel() {
		return new StringResourceModel("widget.interact");
	}
	
	@Override
	protected void onConfigure() {
		super.onConfigure();
		setVisible(getModelObject().getIdentity().isPersistent());
	}

}
