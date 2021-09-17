package org.orienteer.crm.service.impl;

import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.SharedResourceReference;
import org.orienteer.crm.model.ICRMModuleConfiguration;
import org.orienteer.crm.model.IDAOCRM;
import org.orienteer.crm.model.IInteraction;
import org.orienteer.crm.model.InteractionStatus;
import org.orienteer.crm.service.ISMSService;

import com.google.inject.Inject;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.api.v2010.account.MessageCreator;

/**
 * Implementation of {@link ISMSService} which use Twilio
 */
public class TwilioSMSService implements ISMSService {
	
	@Inject
	private IDAOCRM dao;
	
	@Override
	public IInteraction tryToSend(IInteraction interaction) {
		ICRMModuleConfiguration config = dao.getConnectModuleConfiguration();
		if(interaction.getToPhone()!=null 
				&& config.isConfigValid()) {
			String endPoint = config.getTwilioEndPoint();
			MessageCreator msgCreator;
			if(endPoint.startsWith("+")) { //Just one phone specified
				msgCreator = Message.creator(
						new com.twilio.type.PhoneNumber(interaction.getToPhone()),
						new com.twilio.type.PhoneNumber(endPoint),
						interaction.getContent());
			} else { //Messaging service was specified
				msgCreator = Message.creator(
						new com.twilio.type.PhoneNumber(interaction.getToPhone()),
						endPoint,
						interaction.getContent());
			}
			if(config.getRequestStatusReport()) {
				String callBack =  RequestCycle.get().getUrlRenderer()
						.renderFullUrl(Url.parse("/twilio/status"))
									.toString();
				msgCreator.setStatusCallback(callBack);
			}
			Message msgSent = msgCreator.create();
			interaction.setExternalId(msgSent.getSid())
						.setStatus(InteractionStatus.SENT)
						.setFromPhone(endPoint);
		} else {
			interaction.setStatus(InteractionStatus.ERROR);
		}
		return interaction;
	}
}
