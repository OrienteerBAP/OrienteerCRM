package org.orienteer.crm.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.FormParam;

import org.apache.wicket.model.IModel;
import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.core.OrienteerWebSession;
import org.orienteer.core.dao.Command;
import org.orienteer.core.dao.DAO;
import org.orienteer.core.dao.DAOHandler;
import org.orienteer.core.dao.DAOProvider;
import org.orienteer.core.dao.Query;
import org.orienteer.core.dao.handler.extra.SudoMethodHandler;
import org.orienteer.crm.CRMModule;
import org.orienteer.vuecket.extensions.VueAdvancedChat.ChatMessage;
import org.orienteer.vuecket.extensions.VueAdvancedChat.ChatUser;
import org.orienteer.vuecket.extensions.VueAdvancedChat.Room;

import com.google.inject.ProvidedBy;
import com.orientechnologies.orient.core.metadata.security.OUser;
import com.orientechnologies.orient.core.record.impl.ODocument;

/**
 * Data Access Object for Orienteer CRM related entities 
 */
@ProvidedBy(DAOProvider.class)
public interface IDAOCRM {
	
	@Query("select from "+IPerson.CLASS_NAME+" where phone=:phone")
	public IPerson getPersonByExactPhone(String phone);
	
	@Query("select from "+ICRMModuleConfiguration.CLASS_NAME+" where name=\""+CRMModule.NAME+"\"")
	public ICRMModuleConfiguration getConnectModuleConfiguration();
	
	@Query("select from "+IInteraction.CLASS_NAME+" where `from`=:person or `to`=:person order by timestamp")
	public List<IInteraction> getConversation(ODocument person);
	
	@Query("select from Lead where lastActivity is not null and lastInteraction.campaign is null and funnelStage.delist = false order by lastActivity desc limit :limit")
	public List<IPerson> getAllLeadsRooms(int limit);
	
	@Query("select expand(parties) from (select distinct(parties) "
			+ "from (select unionall(`from`, to) as parties, timestamp from Interaction"
				+ " where ( (`from` = :manager or to= :manager) and campaign is null)"
				+ " or campaign.manager=:manager order by timestamp desc unwind parties)"
			+ " where parties!=:manager) limit :limit")
	public List<IPerson> getManagersRooms(IPerson manager, int limit);
	
	@Query("select from (select expand(leads) from :campaign)"
			+ " where lastInteraction is not null"
				+ " and lastInteraction.campaign is null"
				+ " and funnelStage.delist = false"
				+ " order by lastActivity desc limit :limit")
	public List<IPerson> getCampaignRooms(ICampaign campaign, int limit);
	
	@Query("select expand(party) from (select distinct(`from`) as party from Interaction where to=:person)")
	public List<IPerson> getAllPartiesContactedWith(IPerson person);
	
	@Query("select from "+ILead.CLASS_NAME+" where followup between :start and :end")
	public List<ILead> getLeadsForFollowups(Date start, Date end);
	
	@Query("select from "+IInteraction.CLASS_NAME+" where status='PLANNED' and timestamp<=:moment order by timestamp")
	public List<IInteraction> getInteractionsToSend(Date moment);
	
	public default List<IInteraction> getConversation(IPerson person) {
		return getConversation(DAO.asDocument(person));
	}
	
	public default List<org.orienteer.vuecket.extensions.VueAdvancedChat.ChatMessage> getConversationAsChatMessages(ODocument person) {
		return getConversation(person).stream().map(IDAOCRM::interactionToChatMessage).collect(Collectors.toList());
	}
	
	public static ChatMessage interactionToChatMessage(IInteraction i) {
		if(i==null) return null;
		ChatMessage m = new ChatMessage()
				.setId(DAO.asDocument(i).getIdentity().toString())
				.setContent(i.getContent())
				.setSenderId(DAO.asDocument(i.getFrom()).getIdentity().toString())
				.setUsername(i.getFrom().getFullName())
				.setDateAndTimestamp(i.getTimestamp());
		InteractionStatus status = i.getStatus();
		if(status!=null) {
			m.setSaved(status.isSaved());
			m.setDistributed(status.isDistributed());
			m.setSeen(status.isSeen());
		}
		return m;
	}
	
	public static IDAOCRM get() {
		return OrienteerWebApplication.lookupApplication().getServiceInstance(IDAOCRM.class);
	}
	
	@DAOHandler(SudoMethodHandler.class)
	public default void incomingSMS(String from, String to, String content, String externalId) {
		IPerson fromPerson = getPersonByPhone(from);
		IPerson toPerson = getPersonByPhone(to);
		
		IInteraction interaction = DAO.create(IInteraction.class)
				.setContent(content)
				.setFrom(fromPerson)
				.setFromPhone(from)
				.setStatus(InteractionStatus.RECEIVED)
				.setTo(toPerson)
				.setToPhone(to)
				.setTimestamp(new Date())
				.setExternalId(externalId);
		DAO.save(interaction);
		if(fromPerson instanceof ILead) {
			ILead lead = (ILead) fromPerson;
			lead.setLastInteraction(interaction);
			DAO.save(lead);
		}
	}
	
	@DAOHandler(SudoMethodHandler.class)
	public default boolean statusUpdate(String messageSID, String status, String from, String to, String errorCode) {
		IInteraction interaction = DAO.create(IInteraction.class);
		if(interaction.lookupByExternalId(messageSID)) {
			InteractionStatus statusEnum = InteractionStatus.fromTwilioStatus(status);
			boolean shouldSave = false;
			if(statusEnum!=null && !statusEnum.equals(interaction.getStatus())) {
				interaction.setStatus(statusEnum);
				shouldSave = true;
			}
			if(errorCode!=null) {
				interaction.setStatusDetails(errorCode);
				ICampaign campaign = interaction.getCampaign();
				IFunnelStage errorFunnelStage = campaign!=null?campaign.getErrorFunnelStage():null;
				if(errorFunnelStage!=null) {
					IPerson person = interaction.getTo();
					if(person instanceof ILead) {
						ILead lead = (ILead) person;
						lead.setFunnelStage(errorFunnelStage);
						DAO.save(lead);
					}
				}
				shouldSave = true;
			}
			if(shouldSave) {
				DAO.save(interaction);
			}
			return true;
		} else {
			return false;
		}
	}
	
	@DAOHandler(SudoMethodHandler.class)
	public default String getManagerPhone(String from) {
		IPerson person = getPersonByPhone(from);
		OUser managerUser=null;
		if(person instanceof ILead) {
			ILead lead = (ILead) person;
			managerUser = lead.getManager();
		}
		if(managerUser==null) {
			ICRMModuleConfiguration config = getConnectModuleConfiguration();
			managerUser = config.getDefaultManager();
		}
		IPerson manager = IPerson.fromOUser(managerUser);
		return manager!=null?manager.getPhone():null;
	}
	
	public default IPerson getPersonByPhone(String phone) {
		if(phone==null) return null;
		IPerson person = getPersonByExactPhone(phone);
		if(person==null && phone.startsWith("+1")) {
			person = getPersonByExactPhone(phone.substring(2));
		}
		return person;
	}
	
	public default IModel<List<Room>> getRoomsForPerson(IModel<IPerson> person) {
		return person.map(p -> {
			IPerson currentUser = IPerson.getCurrentUserAsPerson();
			ChatUser thisUser = currentUser!=null?currentUser.asChatUser():null;
			List<ChatUser> users = new ArrayList<>();
			users.addAll(getAllPartiesContactedWith(p).stream()
								.map(IPerson::asChatUser).collect(Collectors.toList()));
			users.add(p.asChatUser());
			if(thisUser!=null && users.contains(thisUser)) users.add(thisUser);
			return Arrays.asList(new Room()
								.setRoomId(DAO.asDocument(p).getIdentity().toString())
								.setRoomName(p.getFullName())
								.setUsers(users)
								.addMessage(new ChatMessage()
													.setId("-1")
													.setSystem(true)
													.setContent("Chat with "+p.getFullName())));
 		});
	}
	
}
