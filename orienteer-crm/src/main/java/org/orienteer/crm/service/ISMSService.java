package org.orienteer.crm.service;

import org.orienteer.crm.model.IInteraction;
import org.orienteer.crm.service.impl.TwilioSMSService;

import com.google.inject.ImplementedBy;

/**
 * SMS Service interface to be able communicate with a person
 */
@ImplementedBy(TwilioSMSService.class)
public interface ISMSService {
	
	public IInteraction tryToSend(IInteraction interaction);
}
