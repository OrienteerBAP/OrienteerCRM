package org.orienteer.crm;

import java.util.Date;
import java.util.List;

import org.apache.wicket.ThreadContext;
import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.core.OrienteerWebSession;
import org.orienteer.core.dao.DAO;
import org.orienteer.crm.model.ICampaign;
import org.orienteer.crm.model.IDAOCRM;
import org.orienteer.crm.model.IInteraction;
import org.orienteer.crm.model.ILead;
import org.orienteer.crm.model.IPerson;
import org.orienteer.crm.model.InteractionStatus;
import org.orienteer.crm.service.ISMSService;
import org.orienteer.logger.OLogger;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.sql.executor.OResultSet;
import com.twilio.rest.api.v2010.account.Message;

import lombok.extern.slf4j.Slf4j;
import ru.ydn.wicket.wicketorientdb.utils.DBClosure;

/**
 * Class for handling invokations from scheduler 
 */
@Singleton
@Slf4j
public class CRMScheduler {
	
	@Inject
	private Provider<IDAOCRM> daoProvider;
	
	@Inject
	private Provider<ISMSService> smsServiceProvider;
	
	private volatile boolean locked=false;
	
	public static synchronized final CRMScheduler getInstance() {
		return OrienteerWebApplication.lookupApplication().getServiceInstance(CRMScheduler.class);
	}
	
	public void tick(long lookAhead) {
		if(!locked) {
			try {
				locked=true;
				new DBClosure<Boolean>() {
		
					@Override
					protected Boolean execute(ODatabaseSession db) {
						ThreadContext.setApplication(OrienteerWebApplication.lookupApplication());
						try {
							try {
								tick(db, lookAhead);
							} catch (Throwable e) {
								log.error("Problem in Connect Scheduler", e);
								OLogger.log(e);
							}
							return true;
						} finally {
							ThreadContext.detach();
						}
					}
				}.execute();
			} finally {
				locked = false;
			}
		}
	}
	
	protected void tick(ODatabaseSession db, long lookAhead) throws Throwable {
		IDAOCRM dao = daoProvider.get();
		ISMSService smsService = smsServiceProvider.get();
		Date time = new Date(System.currentTimeMillis()+lookAhead);
		List<IInteraction> interactions = dao.getInteractionsToSend(time);
		for (IInteraction i : interactions) {
			long deltaTime = i.getTimestamp().getTime()-System.currentTimeMillis();
			if(deltaTime>0) Thread.sleep(deltaTime);
			i.normilizePhones(false);
			smsService.tryToSend(i);
			DAO.save(i);
			IPerson toPerson = i.getTo();
			if(toPerson instanceof ILead 
					&& i.getCampaign()!=null
					&& i.getCampaign().getFunnelStage()!=null) {
				DAO.save(((ILead) toPerson)
						.setFunnelStage(i.getCampaign().getFunnelStage())
						.setLastInteraction(i));
			}
		}
	}
}
