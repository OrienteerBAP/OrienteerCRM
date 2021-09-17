package org.orienteer.crm.web;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.core.dao.DAO;
import org.orienteer.crm.model.IDAOCRM;
import org.orienteer.crm.model.IInteraction;
import org.orienteer.crm.model.InteractionStatus;

import lombok.extern.slf4j.Slf4j;

/**
 * Resource for recieving OAuth callbacks
 * Sample: 
 * ToCountry=US&
 * ToState=IL&
 * SmsMessageSid=SMda6a11de26011dfb206b676b5b395c45&
 * NumMedia=0&
 * ToCity=CHICAGO&
 * FromZip=02026&
 * SmsSid=SMda6a11de26011dfb206b676b5b395c45&
 * FromState=MA&
 * SmsStatus=received&
 * FromCity=BOSTON&
 * Body=Test+2&
 * FromCountry=US&
 * To=%2B13125481722&
 * ToZip=60601&
 * NumSegments=1&
 * MessageSid=SMda6a11de26011dfb206b676b5b395c45&
 * AccountSid=AC7e248ccde10b44d53e16e291d9b0ceae&
 * From=%2B17817423193&
 * ApiVersion=2010-04-01
 */
@Path("")
@Slf4j
public class TwilioCallbacksResources {
	
	private static final String EMPTY_RESPONSE = "<Response></Response>";
	private static final String VOICE_DEAL_RESPONSE = "<Response> <Dial> %s </Dial> </Response>";
	private static final String NO_MANAGERS = "<Response>\r\n"
			+ "<Say voice=\"alice\">Thanks for the call. Currently there is no managers available. We will call you back as soon as possible.</Say>\r\n"
			+ "</Response>";
	
	@Path("sms")
	@GET
	@Produces("application/xml")
	public String incomingSMSByGet(@QueryParam("From") String from, 
							  @QueryParam("To") String to, 
							  @QueryParam("Body") String body, 
							  @QueryParam("MessageSid") String messageSID) {
		IDAOCRM dao = OrienteerWebApplication.lookupApplication().getServiceInstance(IDAOCRM.class);
		log.info("Incoming SMS (from: {}, to: {}, messageSID: {}, body: {})", from, to, messageSID, body);
		dao.incomingSMS(from, to, body, messageSID);
		return EMPTY_RESPONSE;
	}
	
	@Path("sms")
	@POST
	@Produces("application/xml")
	public String incomingSMSByPost(@FormParam("From") String from, 
								    @FormParam("To") String to, 
								    @FormParam("Body") String body, 
								    @FormParam("MessageSid") String messageSID) {
		return incomingSMSByGet(from, to, body, messageSID);
	}
	
	@Path("status")
	@POST
	@Produces("application/xml")
	public String statusUpdate(@FormParam("MessageSid") String messageSID,
							   @FormParam("MessageStatus") String status,
							   @FormParam("From") String from,
							   @FormParam("To") String to,
							   @FormParam("ErrorCode") String errorCode) {
		log.info("Status change: messageSID={}, status={}, from={}, to={}", messageSID, status, from, to);
		IDAOCRM dao = OrienteerWebApplication.lookupApplication().getServiceInstance(IDAOCRM.class);
		boolean updated = dao.statusUpdate(messageSID, status, from, to, errorCode);
		if(!updated) {
			log.error("Interaction was not found for MessageSid: "+messageSID);
		}
		return EMPTY_RESPONSE;
	}
	
	@Path("voice")
	@POST
	@Produces("application/xml")
	public String voiceCall(@FormParam("From") String from) {
		log.info("Voice call: from={}", from);
		IDAOCRM dao = OrienteerWebApplication.lookupApplication().getServiceInstance(IDAOCRM.class);
		String phone = dao.getManagerPhone(from);
		return phone!=null?String.format(VOICE_DEAL_RESPONSE, dao.getManagerPhone(from))
						  :NO_MANAGERS;
	}
}
