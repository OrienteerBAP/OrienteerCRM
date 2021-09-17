package org.orienteer.crm;

import static ru.ydn.wicket.wicketorientdb.security.OrientPermission.CREATE;
import static ru.ydn.wicket.wicketorientdb.security.OrientPermission.DELETE;
import static ru.ydn.wicket.wicketorientdb.security.OrientPermission.EXECUTE;
import static ru.ydn.wicket.wicketorientdb.security.OrientPermission.READ;
import static ru.ydn.wicket.wicketorientdb.security.OrientPermission.UPDATE;
import static ru.ydn.wicket.wicketorientdb.security.OrientPermission.combinedPermission;
 
import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.core.component.FAIconType;
import org.orienteer.core.dao.DAO;
import org.orienteer.core.dao.dm.IOEnum;
import org.orienteer.core.method.OMethodsManager;
import org.orienteer.core.module.AbstractOrienteerModule;
import org.orienteer.core.module.PerspectivesModule;
import org.orienteer.core.module.PerspectivesModule.IOPerspective;
import org.orienteer.core.util.CommonUtils;
import org.orienteer.core.util.OSchemaHelper;
import org.orienteer.crm.component.widget.InteractWidget;
import org.orienteer.crm.model.ICampaign;
import org.orienteer.crm.model.ICRMModuleConfiguration;
import org.orienteer.crm.model.IFunnelStage;
import org.orienteer.crm.model.IInteraction;
import org.orienteer.crm.model.ILead;
import org.orienteer.crm.model.IPerson;
import org.orienteer.crm.model.IState;
import org.orienteer.crm.model.ITemplate;
import org.orienteer.crm.web.TwilioCallbacksResources;
import org.orienteer.wicketjersey.WicketJersey;

import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.twilio.Twilio;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.metadata.security.ORole;
import com.orientechnologies.orient.core.metadata.security.OSecurity;
import com.orientechnologies.orient.core.metadata.security.OUser;
import com.orientechnologies.orient.core.metadata.security.ORule.ResourceGeneric;

/**
 * Orienteer Module for CRM: install data model, start and stop resources per lifecycle and etc.
 */
public class CRMModule extends AbstractOrienteerModule{
	
	public static final String NAME = "crm";
	

	protected CRMModule() {
		super(NAME, 1);
	}
	
	@Override
	public ODocument onInstall(OrienteerWebApplication app, ODatabaseSession db) {
		super.onInstall(app, db);
		installSchemaAndData(app, db);
		return DAO.asDocument(DAO.create(ICRMModuleConfiguration.class));
	}
	
	@Override
	public void onUpdate(OrienteerWebApplication app, ODatabaseSession db, int oldVersion, int newVersion) {
		super.onUpdate(app, db, oldVersion, newVersion);
		installSchemaAndData(app, db);
		for(int toVersion=oldVersion+1; toVersion<=newVersion; toVersion++) {
			updateTo(app, db, toVersion);
		}
	}
	
	protected void updateTo(OrienteerWebApplication app, ODatabaseSession db, int toVersion) {
		switch(toVersion) {
		case 8:
			db.command("update "+IFunnelStage.CLASS_NAME+" set delist=false");
			db.command("update "+IFunnelStage.CLASS_NAME+" set delist=true where alias='gone'");
			break;
		case 9:
			db.command("update "+IFunnelStage.CLASS_NAME+" set doNotCall=false");
			db.command("update "+IFunnelStage.CLASS_NAME+" set delist=true, doNotCall=true where alias='dnc'");
			break;
		case 10:
			db.command("update "+IFunnelStage.CLASS_NAME+" set delist=true, doNotCall=false where alias='error'");
			break;
		}
	}
	
	
	public void installSchemaAndData(OrienteerWebApplication app, ODatabaseSession db) {
		OSchemaHelper helper = OSchemaHelper.bind(db);
		DAO.describe(helper, IState.class, 
							 IPerson.class,
							 ILead.class, 
							 IFunnelStage.class, 
							 IInteraction.class,
							 ICRMModuleConfiguration.class,
							 ITemplate.class,
							 ICampaign.class);
		OClass oUserClass = db.getMetadata().getSchema().getClass(OUser.CLASS_NAME);
		OClass personClass = db.getMetadata().getSchema().getClass(IPerson.CLASS_NAME);
		if(!personClass.isSuperClassOf(oUserClass)) {
			oUserClass.addSuperClass(personClass);
		}
		createOrUpdateEnum(IFunnelStage.class, "funnelstage", "lead", "hotlead", "gone", "stale", "dnc", "error");
		
		helper.oClass("OFunction")
			.oDocument("name", "Distribute")
			.field("language", "nashorn")
			.field("code", CRMScheduler.class.getName()+".getInstance().tick(60 * 1000)")
			.saveDocument();
		ODocument schedulerFunc = helper.getODocument();
		helper.oClass("OSchedule")
				.oDocument("name", "OfferAIConnectSender")
				.field("rule", "0 * * * * ?")
				.field("function", schedulerFunc)
				.saveDocument();
		
		createOrUpdatePerspectives();
		reduceReaderRights(db);
		changeDefaultPerspective(db);
	}
	
	private void createOrUpdateEnum(Class<? extends IOEnum> daoClass, String prefix, String... values) {
		for (String value : values) {
			IOEnum e = DAO.create(daoClass);
			if(!e.lookupByAlias(value)) {
				e.setName(CommonUtils.getLocalizedStrings(prefix+"."+value))
							.setAlias(value);
				DAO.save(e);
			}
		}
	}
	
	private void createOrUpdatePerspectives() {
		IOPerspective perspective = IOPerspective.getOrCreateByAlias("connect", 
																	 "perspective.connect", 
																	 FAIconType.bolt.name(), 
																	 "/browse/"+ILead.CLASS_NAME);
		
		perspective.getOrCreatePerspectiveItem("leads", 
											   "perspective.connect.leads",
											   FAIconType.bolt.name(),
											   "/browse/"+ILead.CLASS_NAME);
		
		perspective.getOrCreatePerspectiveItem("campaigns", 
											   "perspective.connect.campaigns",
											   FAIconType.address_card.name(),
											   "/browse/"+ICampaign.CLASS_NAME);
		
		perspective.getOrCreatePerspectiveItem("templates", 
											   "perspective.connect.templates",
											   FAIconType.tasks.name(),
											   "/browse/"+ITemplate.CLASS_NAME);
		perspective.getOrCreatePerspectiveItem("interactions", 
											   "perspective.connect.interactions",
											   FAIconType.exchange.name(),
											   "/browse/"+IInteraction.CLASS_NAME+"?tab=myInteractions");
		
	}
	
	protected void reduceReaderRights(ODatabaseSession db) {
		OSecurity security = db.getMetadata().getSecurity();
		ORole readerRole = security.getRole("reader");
		int permissionToRevoke = combinedPermission(CREATE, READ, UPDATE, DELETE, EXECUTE);
		readerRole.revoke(ResourceGeneric.CLASS, null, permissionToRevoke);
		readerRole.revoke(ResourceGeneric.CLUSTER, null, permissionToRevoke);
	}
	
	protected void changeDefaultPerspective(final ODatabaseSession db) {
		final PerspectivesModule perspectivesModule = OrienteerWebApplication.get().getServiceInstance(PerspectivesModule.class);
		perspectivesModule.getPerspectiveByAliasAsDocument("connect").ifPresent((p) -> {
			for(ODocument role : db.getMetadata().getSecurity().getAllRoles()) {
				perspectivesModule.updateUserPerspective(role, p);
			}
		});
	}
	
	@Override
	public void onInitialize(OrienteerWebApplication app, ODatabaseSession db, ODocument moduleDoc) {
		app.registerWidgets(InteractWidget.class.getPackage().getName());
		OMethodsManager.get().addModule(CRMModule.class);
		ICRMModuleConfiguration config = DAO.provide(ICRMModuleConfiguration.class, moduleDoc);
		config.initIfPossible();
		WicketJersey.mount("/twilio", TwilioCallbacksResources.class.getPackage().getName());
	}
	
	@Override
	public void onConfigurationChange(OrienteerWebApplication app, ODatabaseSession db, ODocument moduleDoc) {
		ICRMModuleConfiguration config = DAO.provide(ICRMModuleConfiguration.class, moduleDoc);
		config.initIfPossible();
	}
	
	@Override
	public void onDestroy(OrienteerWebApplication app, ODatabaseSession db) {
		OMethodsManager.get().removeModule(CRMModule.class);
		app.unregisterWidgets(InteractWidget.class.getPackage().getName());
		Twilio.destroy();
		app.unmount("/twilio");
	}
	
}
