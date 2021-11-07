package org.orienteer.crm.model;

import org.apache.wicket.util.string.Strings;
import org.orienteer.core.OrienteerWebSession;
import org.orienteer.core.component.visualizer.UIVisualizersRegistry;
import org.orienteer.core.dao.DAO;
import org.orienteer.core.dao.ODocumentWrapperProvider;
import org.orienteer.core.dao.OrienteerOClass;
import org.orienteer.core.dao.OrienteerOProperty;
import org.orienteer.transponder.annotation.EntityPropertyIndex;
import org.orienteer.transponder.annotation.EntityType;
import org.orienteer.transponder.orientdb.ODriver;
import org.orienteer.vuecket.extensions.VueAdvancedChat.ChatUser;

import com.google.common.base.Joiner;
import com.google.inject.ProvidedBy;
import com.orientechnologies.orient.core.metadata.security.OUser;
import com.orientechnologies.orient.core.record.impl.ODocument;

/**
 * Super class to represent all required info about a person
 */
@ProvidedBy(ODocumentWrapperProvider.class)
@EntityType(value = IPerson.CLASS_NAME, orderOffset = 50)
@OrienteerOClass(displayable = {"firstName", "lastName", "phone", "address", "city", "state"})
public interface IPerson {
	public static final String CLASS_NAME = "Person";
	
	public String getFirstName();
	public IPerson setFirstName(String value);
	
	public String getMiddleName();
	public IPerson setMiddleName(String value);
	
	public String getLastName();
	public IPerson setLastName(String value);
	
	@EntityPropertyIndex(type = ODriver.OINDEX_NOTUNIQUE)
	public String getPhone();
	public IPerson setPhone(String value);
	
	public String getEMail();
	public IPerson setEMail(String value);
	
	public String getAddress();
	public IPerson setAddress(String value);
	
	public String getCity();
	public IPerson setCity(String value);
	
	@OrienteerOProperty(visualization = UIVisualizersRegistry.VISUALIZER_SUGGEST)
	public IState getState();
	public IPerson setState(IState value);
	
	public Integer getZip();
	public IPerson setZip(Integer value);	
	
	public default String getFullName() {
		String fullName = Joiner.on(' ').skipNulls().join(getFirstName(), getMiddleName(), getLastName());
		if(!Strings.isEmpty(fullName)) return fullName;
		else {
			ODocument doc = DAO.asDocument(this);
			if(doc.getSchemaClass().isSubClassOf("OUser")) {
				return doc.field("name");
			} else return "User "+DAO.asDocument(this).getIdentity();
		}
	}
	
	public default ChatUser asChatUser() {
		return new ChatUser().setId(DAO.asDocument(this).getIdentity().toString())
							 .setUsername(getFullName());
	}
	
	public static IPerson fromOUser(OUser user) {
		return user!=null?DAO.provide(IPerson.class, user.getDocument()):null;
	}
	
	public static IPerson fromOUserDocument(ODocument user) {
		return user!=null?DAO.provide(IPerson.class, user):null;
	}
	
	public static IPerson getCurrentUserAsPerson() {
		return OrienteerWebSession.get().isSignedIn()
		? IPerson.fromOUserDocument(OrienteerWebSession.get().getUserAsODocument())
		: null;
	}
	
}
