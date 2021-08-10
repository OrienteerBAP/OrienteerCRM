package org.orienteer.crm.demo;

import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.core.module.PerspectivesModule;

public class MyWebApplication extends OrienteerWebApplication
{
	@Override
	public void init()
	{
		super.init();
		mountPackage("org.orienteer.crm.demo.web");
		registerModule(DataModel.class);
	}
	
}
