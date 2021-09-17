package org.orienteer.crm.component.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.orienteer.core.OrienteerWebSession;
import org.orienteer.core.component.FAIcon;
import org.orienteer.core.component.FAIconType;
import org.orienteer.core.dao.DAO;
import org.orienteer.core.web.ODocumentPage;
import org.orienteer.core.widget.AbstractWidget;
import org.orienteer.crm.model.IDAOCRM;
import org.orienteer.crm.model.IFunnelStage;
import org.orienteer.crm.model.IInteraction;
import org.orienteer.crm.model.ILead;
import org.orienteer.crm.model.IPerson;
import org.orienteer.vuecket.DataFiber;
import org.orienteer.vuecket.VueBehavior;
import org.orienteer.vuecket.extensions.VueAdvancedChat;
import org.orienteer.vuecket.extensions.VueAdvancedChat.ChatMessage;
import org.orienteer.vuecket.extensions.VueAdvancedChat.ChatUser;
import org.orienteer.vuecket.extensions.VueAdvancedChat.Room;
import org.orienteer.vuecket.method.IVuecketMethod;
import org.orienteer.vuecket.method.IVuecketMethod.Context;
import org.orienteer.vuecket.method.VueOn;

import com.google.inject.Inject;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.record.impl.ODocument;

import lombok.extern.slf4j.Slf4j;
import ru.ydn.wicket.wicketorientdb.model.ODocumentModel;

/**
 * Abstract parent widget to be used by any widget which are going to show interactions with contacts
 * @param <T> the type of main data object linked to this widget
 */
@Slf4j
public abstract class AbstractInteractionsWidget<T> extends AbstractWidget<T> {
	
	public static final CssResourceReference INTERACTIONS_CSS 
				= new CssResourceReference(AbstractInteractionsWidget.class, "AbstractInteractionsWidget.css");
	
	// 2 times even less rather then needed to fix OrientDB issue
	public static final Integer MAX_LIMIT = Integer.MAX_VALUE >> 2; 
	
	@Inject
	protected IDAOCRM dao;

	private IModel<List<Room>> rooms = new LoadableDetachableModel<List<Room>>() {
		
		@Override
		protected List<Room> load() {
			IPerson currentUser = IPerson.getCurrentUserAsPerson();
			ChatUser thisUser = currentUser!=null?currentUser.asChatUser():null;
			int limit = roomsToLoad.getObject();
			List<Room> rooms = getRooms(limit).map(p -> {
				Room room = new Room()
						.setRoomId(DAO.asDocument(p).getIdentity().toString())
						.setRoomName(p.getFullName());
				List<ChatUser> users = new ArrayList<>();
				boolean allUsersLoad = Objects.equals(DAO.asDocument(p), currentRoom.getObject());
				if(allUsersLoad) {
					users.addAll(dao.getAllPartiesContactedWith(p).stream()
										.map(IPerson::asChatUser).collect(Collectors.toList()));
					users.add(p.asChatUser());
				} 
				
				if(p instanceof ILead) {
					ILead lead = (ILead)p;
					IFunnelStage stage = lead.getFunnelStage();
					if(stage!=null) {
						room.setAvatar(stage.getAvatarUrl());
					}
					if(!allUsersLoad) {
						IInteraction interaction = lead.getLastInteraction();
						if(interaction!=null) {
							IPerson fromPerson = interaction.getFrom();
							IPerson toPerson = interaction.getTo();
							room.setLastMessage(IDAOCRM.interactionToChatMessage(lead.getLastInteraction()));
							if(fromPerson!=null) users.add(fromPerson.asChatUser());
							if(toPerson!=null) users.add(toPerson.asChatUser());
						}
					}
				}
				if(thisUser!=null && users.contains(thisUser)) users.add(thisUser);
				room.setUsers(users);
				return room;
			}).filter(r -> !Objects.equals(r.getRoomId(), thisUser.getId()))
					.collect(Collectors.toList());
			if(rooms.size()<limit) {
				roomsToLoad.setObject(MAX_LIMIT);
				if(chatWindow!=null)chatWindow.getDataFibers()
										.getDataFiberByName("rooms-loaded")
										.ifPresent(df -> ((DataFiber<Boolean>)df).setValue(true));
			}
			return rooms;
		}
	};
	
	private IModel<List<ChatMessage>> messages = new LoadableDetachableModel<List<ChatMessage>>() {

		@Override
		protected List<ChatMessage> load() {
			ODocument room = currentRoom.getObject();
			return room!=null? dao.getConversationAsChatMessages(room):new ArrayList<ChatMessage>();
		}
	};
	
	private IModel<ODocument> currentRoom = new ODocumentModel();
	private IModel<Integer> roomsToLoad = Model.of(10);
	private VueAdvancedChat chatWindow;
	
	public AbstractInteractionsWidget(String id, IModel<T> model, IModel<ODocument> widgetDocumentModel) {
		super(id, model, widgetDocumentModel);
		add(new VueBehavior());
		List<Room> rooms = this.rooms.getObject();
		if(rooms!=null && !rooms.isEmpty()) {
			currentRoom.setObject(new ORecordId(rooms.get(0).getRoomId().toString()).getRecord());
		}
		add(chatWindow = new VueAdvancedChat("chat", this.rooms, messages) {
			
			{
				roomsLoaded.setObject(false);
			}
			
			@Override
			public void onSendMessage(Context ctx, SendMessage message) {
				InteractWidget.sendMessage(ctx, message);
			}
			
			@Override
			public void onFetchMessage(Context ctx, FetchMessages fetchMessages) {
				ODocument roomDoc = new ORecordId(fetchMessages.getRoom().getRoomId().toString()).getRecord();
				currentRoom.setObject(roomDoc);
				ctx.getVueBehavior().getDataFibers().detach();
				IVuecketMethod.pushDataFibers(ctx, false, "rooms", "rooms-loaded", "messages", "messages-loaded");
			}
			
			@VueOn("fetch-more-rooms")
			public void onFetchMoreRooms(IVuecketMethod.Context ctx) {
				int limit = roomsToLoad.getObject();
				if(limit< Integer.MAX_VALUE-10)roomsToLoad.setObject(limit+10);
				ctx.getVueBehavior().getDataFibers().detach();
				IVuecketMethod.pushDataFibers(ctx, false, "rooms", "rooms-loaded");
			}
			
			@Override
			public void onRoomActionHandler(Context ctx, ActionHandlerEvent event) {
				super.onRoomActionHandler(ctx, event);
				onAction(ctx, event);
			}
			
			@Override
			public void onMenuActionHandler(Context ctx, ActionHandlerEvent event) {
				super.onMenuActionHandler(ctx, event);
				onAction(ctx, event);
			}
			
			public void onAction(Context ctx, ActionHandlerEvent event) {
				ODocument roomDoc = new ORecordId(event.getRoomId().toString()).getRecord();
				String action = event.getAction().getName();
				if(Objects.equals("open", action)) {
					setResponsePage(new ODocumentPage(roomDoc));
				} else if((Objects.equals("dnc", action) || Objects.equals("gone", action))
						  && roomDoc.getSchemaClass().isSubClassOf(ILead.CLASS_NAME)) {
					ILead lead = DAO.provide(ILead.class, roomDoc);
					IFunnelStage stage = DAO.create(IFunnelStage.class);
					if(stage.lookupByAlias(action)) {
						lead.setFunnelStage(stage);
						DAO.save(lead);
						IVuecketMethod.pushDataFibers(ctx, false, "rooms", "rooms-loaded");
					}
				}
			}

			@Override
			public String getCurrentUserId() {
				return OrienteerWebSession.get().isSignedIn()
				? OrienteerWebSession.get().getUserAsODocument().getIdentity().toString()
				: null;
			}
		}.addRoomActions("open", "dnc", "gone").addMenuActions("dnc", "gone"));
	}
	
	protected abstract Stream<IPerson> getRooms(int limit);
	

	@Override
	protected FAIcon newIcon(String id) {
		return new FAIcon(id, FAIconType.list_alt);
	}

	@Override
	protected IModel<String> getDefaultTitleModel() {
		return new StringResourceModel("widget.interactions");
	}
	
	@Override
	protected void onDetach() {
		super.onDetach();
		rooms.detach();
		messages.detach();
		currentRoom.detach();
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(INTERACTIONS_CSS));
	}

}
