package org.orienteer.crm;

import org.apache.wicket.Application;
import org.apache.wicket.IInitializer;
import org.orienteer.core.OrienteerWebApplication;

/**
 * {@link IInitializer} for 'orienteer-crm' module
 */
public class Initializer implements IInitializer
{
	@Override
	public void init(Application application) {
		OrienteerWebApplication app = (OrienteerWebApplication)application;
		app.registerModule(CRMModule.class);
		app.mountPackage("org.orienteer.crm.web");
	}

	@Override
	public void destroy(Application application) {
		OrienteerWebApplication app = (OrienteerWebApplication)application;
		app.unmountPackage("org.orienteer.crm.web");
		app.unregisterModule(CRMModule.class);
	}
	
}
