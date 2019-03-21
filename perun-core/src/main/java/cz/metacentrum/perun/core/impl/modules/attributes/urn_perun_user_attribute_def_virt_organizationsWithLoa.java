package cz.metacentrum.perun.core.impl.modules.attributes;

import javax.security.cert.CertificateException;
import javax.security.cert.X509Certificate;

import java.text.DateFormat;
import java.util.HashMap;

import org.apache.commons.codec.binary.Base64;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleImplApi;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import cz.metacentrum.perun.core.api.BeansUtils;

/**
 * Get specified user oraganizations with Loa
 *
 * @author Michal Šťava <stavamichal@gmail.com>
 */
public class urn_perun_user_attribute_def_virt_organizationsWithLoa extends UserVirtualAttributesModuleAbstract implements UserVirtualAttributesModuleImplApi {

	Map<String, Pair<String, String>> mapOfExtSourcesNames = new HashMap<>();

	@Override
	public Attribute getAttributeValue(PerunSessionImpl sess, User user, AttributeDefinition attributeDefinition) throws InternalErrorException {
		Attribute attribute = new Attribute(attributeDefinition);
		HashMap<String, String> organizationsWithLoa = new LinkedHashMap<>();

		List<UserExtSource> extSources = sess.getPerunBl().getUsersManagerBl().getUserExtSources(sess, user);
		if(extSources == null || extSources.isEmpty()) return attribute; //If no userExtSources, so no Loa for any of them.

		String version = attributeDefinition.getFriendlyNameParameter();
		if(version == null) throw new InternalErrorException("There is no parameter (cs or en) in attribute " + attributeDefinition);

		UserExtSource userExtSourceForCreating = null;
		UserExtSource userExtSourceForModifiing = null;

		//Initialize MapOfExtSource
		initializeMapOfExtSourceName();

		for(UserExtSource uES: extSources) {
			String uEName = uES.getExtSource().getName();
			String uELoa = String.valueOf(uES.getLoa());

			if(uES.getCreatedAt() != null) {
				Date testingDate = null;
				Date lastUsedDate = null;
				boolean parsed = true;
				try {
					testingDate = BeansUtils.getDateFormatter().parse(uES.getCreatedAt());
				} catch (Exception ex) {
					//Not Parsed correctly
					parsed = false;
				}
				if(parsed) {
					if(userExtSourceForCreating == null || userExtSourceForCreating.getCreatedAt() == null) userExtSourceForCreating = uES;
					else {
						try {
							lastUsedDate = BeansUtils.getDateFormatter().parse(userExtSourceForCreating.getCreatedAt());
							if(testingDate != null && testingDate.compareTo(lastUsedDate) < 0) {
								userExtSourceForCreating = uES;
							}
						} catch (Exception ex) {
							//Not Parsed correctly
							userExtSourceForCreating = uES;
						}
					}
				}
			}

			if(uES.getModifiedAt() != null) {
				Date testingDate = null;
				Date lastUsedDate = null;
				boolean parsed = true;
				try {
					testingDate = BeansUtils.getDateFormatter().parse(uES.getModifiedAt());
				} catch (Exception ex) {
					//Not Parsed correctly
					parsed = false;
				}
				if(parsed) {
					if(userExtSourceForModifiing == null || userExtSourceForModifiing.getModifiedAt() == null) userExtSourceForModifiing = uES;
					else {
						try {
							lastUsedDate = BeansUtils.getDateFormatter().parse(userExtSourceForModifiing.getModifiedAt());
							if(testingDate != null && testingDate.compareTo(lastUsedDate) < 0) {
								userExtSourceForModifiing = uES;
							}
						} catch (Exception ex) {
							//Not Parsed correctly
							userExtSourceForModifiing = uES;
						}
					}
				}
			}

			String uESimpleName = getSimpleNameOfExtSource(uEName, version.equals("cs"));
			organizationsWithLoa.put(uESimpleName, uELoa);
		}

		//Set created,modified by userExtSources
		if(userExtSourceForCreating != null) {
			attribute.setValueCreatedAt(userExtSourceForCreating.getCreatedAt());
			attribute.setValueCreatedBy(userExtSourceForCreating.getCreatedBy());
		}
		if(userExtSourceForModifiing != null) {
			attribute.setValueModifiedAt(userExtSourceForModifiing.getModifiedAt());
			attribute.setValueModifiedBy(userExtSourceForModifiing.getModifiedBy());
		}
		attribute.setValue(organizationsWithLoa);
		return attribute;
	}

	/**
	 * This method get simple name of some existing extSource (if is known), if isn't known return default extSourceName
	 * if giveMeCzechLanguage is true = return czech Simple Name of ExtSource
	 * if giveMeCzechLanguage is false = return english Simple Name of ExtSource
	 *
	 * @param extSourceName default extSourceName (example: https://www.vutbr.cz/SSO/saml2/idp)
	 * @param giveMeCzechLanguage if true return czech, if false return english
	 * @return simpleName of any ExtSource if is known (example: Brno University of Technology)
	 */
	private String getSimpleNameOfExtSource(String extSourceName, boolean giveMeCzechLanguage) {
		if(mapOfExtSourcesNames.containsKey(extSourceName)) {
			if(giveMeCzechLanguage) {
				return mapOfExtSourcesNames.get(extSourceName).getLeft();
			} else {
				return mapOfExtSourcesNames.get(extSourceName).getRight();
			}
		} else {
			return extSourceName;
		}
	}

	private void initializeMapOfExtSourceName() {
		mapOfExtSourcesNames.put("https://idp.upce.cz/idp/shibboleth", new Pair("Univerzita Pardubice", "University in Pardubice"));
		mapOfExtSourcesNames.put("https://idp.slu.cz/idp/shibboleth", new Pair("Univerzita v Opavě", "University in Opava"));
		mapOfExtSourcesNames.put("https://login.feld.cvut.cz/idp/shibboleth", new Pair("Fakulta elektrotechnická, České vysoké učení technické v Praze", "Faculty of Electrical Engineering, Czech Technical University In Prague"));
		mapOfExtSourcesNames.put("https://www.vutbr.cz/SSO/saml2/idp", new Pair("Vysoké učení technické v Brně", "Brno University of Technology"));
		mapOfExtSourcesNames.put("https://shibboleth.nkp.cz/idp/shibboleth", new Pair("Národní knihovna České republiky", "The National Library of the Czech Republic"));
		mapOfExtSourcesNames.put("https://idp2.civ.cvut.cz/idp/shibboleth", new Pair("České vysoké účení technické v Praze", "Czech Technical University In Prague"));
		mapOfExtSourcesNames.put("https://shibbo.tul.cz/idp/shibboleth", new Pair("Technická univerzita v Liberci", "Technical University of Liberec"));
		mapOfExtSourcesNames.put("https://idp.mendelu.cz/idp/shibboleth", new Pair("Mendlova univerzita v Brně", "Mendel University in Brno"));
		mapOfExtSourcesNames.put("https://cas.cuni.cz/idp/shibboleth", new Pair("Univerzita Karlova v Praze", "Charles University in Prague"));
		mapOfExtSourcesNames.put("https://wsso.vscht.cz/idp/shibboleth", new Pair("Vysoká škola chemicko-technická v Praze", "Institute of Chemical Technology Prague"));
		mapOfExtSourcesNames.put("https://idp.vsb.cz/idp/shibboleth", new Pair("VŠB - Technická univerzita Ostrava", "VSB – Technical University of Ostrava"));
		mapOfExtSourcesNames.put("https://whoami.cesnet.cz/idp/shibboleth", new Pair("CESNET, z. s. p. o.", "CESNET, a. l. e."));
		mapOfExtSourcesNames.put("https://helium.jcu.cz/idp/shibboleth", new Pair("Jihočeská univerzita v Českých Budějovicích", "University of South Bohemia"));
		mapOfExtSourcesNames.put("https://idp.ujep.cz/idp/shibboleth", new Pair("Univerzita Jana Evangelisty Purkyně v Ústí nad Labem", "Jan Evangelista Purkyne University in Usti nad Labem"));
		mapOfExtSourcesNames.put("https://idp.amu.cz/idp/shibboleth", new Pair("Akademie múzických umění v Praze", "Academy of Performing Arts in Prague"));
		mapOfExtSourcesNames.put("https://idp.lib.cas.cz/idp/shibboleth", new Pair("Knihovna AV ČR, v. v. i.", "Academy of Sciences Library"));
		mapOfExtSourcesNames.put("https://shibboleth.mzk.cz/simplesaml/metadata.xml", new Pair("Moravská zemská knihovna", "Moravian  Library"));
		mapOfExtSourcesNames.put("https://idp2.ics.muni.cz/idp/shibboleth", new Pair("Masarykova univerzita", "Masaryk University"));
		mapOfExtSourcesNames.put("https://idp.upol.cz/idp/shibboleth", new Pair("Univerzita Palackého v Olomouci", "Palacky University, Olomouc"));
		mapOfExtSourcesNames.put("https://idp.fnplzen.cz/idp/shibboleth", new Pair("FN Plzeň", "FN Plzen"));
		mapOfExtSourcesNames.put("https://id.vse.cz/idp/shibboleth", new Pair("Vysoká škola ekonomická v Praze", "University of Economics, Prague"));
		mapOfExtSourcesNames.put("https://shib.zcu.cz/idp/shibboleth", new Pair("Západočeská univerzita v Plzni", "University of West Bohemia"));
		mapOfExtSourcesNames.put("https://idptoo.osu.cz/simplesaml/saml2/idp/metadata.php", new Pair("Ostravská Univerzita v Ostravě", "University of Ostrava"));
		mapOfExtSourcesNames.put("https://login.ics.muni.cz/idp/shibboleth", new Pair("MetaCentrum", "MetaCentrum"));
	}

	/*public AttributeDefinition getAttributeDefinition() {
		return null;
	}*/
}
