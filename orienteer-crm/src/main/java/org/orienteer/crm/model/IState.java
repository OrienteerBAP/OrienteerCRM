package org.orienteer.crm.model;

import org.orienteer.core.dao.DAOOClass;
import org.orienteer.core.dao.ODocumentWrapperProvider;
import org.orienteer.core.dao.dm.IOEnum;

import com.google.inject.ProvidedBy;

/**
 * State or other region
 */
@ProvidedBy(ODocumentWrapperProvider.class)
@DAOOClass(IState.CLASS_NAME)
public interface IState extends IOEnum {
	public static final String CLASS_NAME = "State";
}
