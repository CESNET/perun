package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.GroupsManager;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceUnsupportedOperationException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import static cz.metacentrum.perun.core.impl.ExtSourceLdap.log;
import cz.metacentrum.perun.core.implApi.ExtSourceApi;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

/**
 * This extSource is just for use loading users from LDAP of EGI SSO
 * Need to be concrete because special parsing of user certificates.
 *
 * @author Michal Stava <stavamichal@gmail.com>
 */
public class ExtSourceEGISSO extends ExtSourceLdap implements ExtSourceApi {

	@Override
	public List<Map<String, String>> getGroupSubjects(Map<String, String> attributes) throws InternalErrorException {
		List<Map<String, String>> subjects = new ArrayList<>();
		NamingEnumeration<SearchResult> results = null;

		String query = attributes.get(GroupsManager.GROUPMEMBERSQUERY_ATTRNAME);
		String base = "ou=People,dc=egi,dc=eu";

		List<String> ldapGroupSubjects = new ArrayList<>();
		try {
			SearchControls controls = new SearchControls();
			controls.setTimeLimit(5000);
			results = getContext().search(base, query, controls);
			while(results.hasMore()) {
				SearchResult searchResult = results.next();
				subjects.add(processResultToSubject(searchResult));
			}
		} catch (NamingException e) {
			log.error("LDAP exception during query {}.", query);
			throw new InternalErrorException("LDAP exception during running query " + query , e);
		} finally {
			try {
				if (results != null) { results.close(); }
			} catch (Exception e) {
				log.error("LDAP exception during closing result, while running query '{}'", query);
				throw new InternalErrorException(e);
			}
		}

		return subjects;
	}

	@Override
	public List<Map<String, String>> getSubjectGroups(Map<String, String> attributes) throws InternalErrorException, ExtSourceUnsupportedOperationException {
		throw new ExtSourceUnsupportedOperationException();
	}

	@Override
	protected List<Map<String, String>> querySource(String query, String base, int maxResults) throws InternalErrorException {
		List<Map<String, String>> subjects = new ArrayList<Map<String, String>>();
		NamingEnumeration<SearchResult> results = null;
		
		if(base == null || base.isEmpty()) {
			base = "ou=People,dc=egi,dc=eu";
		}

		if(query == null || query.isEmpty()) throw new InternalErrorException("Query can't be null when searching through EGI SSO.");

		try {
			SearchControls controls = new SearchControls();
			controls.setTimeLimit(5000);
			if (maxResults > 0) {
				controls.setCountLimit(maxResults);
			}
			results = getContext().search(base, query, controls);

			while (results.hasMore()) {
				SearchResult searchResult = (SearchResult) results.next();
				subjects.add(processResultToSubject(searchResult));
			}
			
			log.trace("Returning [{}] subjects", subjects.size());

		} catch (NamingException e) {
			log.error("LDAP exception during running query '{}'", query);
			throw new InternalErrorException("LDAP exception during running query: "+query+".", e);
		} finally {
			try {
				if (results != null) { results.close(); }
			} catch (Exception e) {
				log.error("LDAP exception during closing result, while running query '{}'", query);
				throw new InternalErrorException(e);
			}
		}

		return subjects;
	}

	protected Map<String,String> processResultToSubject(SearchResult sr) throws InternalErrorException {
		if(sr == null) throw new InternalErrorException("SearchResult is empty so cannot be proceed.");
		Map<String,String> subject = new HashMap<>();

		try {
			NamingEnumeration<? extends Attribute> attributes = sr.getAttributes().getAll();
			while(attributes.hasMore()) {
				Attribute attr = attributes.next();
				String attrName = attr.toString().replaceAll(":.*", "");
				boolean found = false;
				for(String key: mapping.keySet()) {
					String value = mapping.get(key);
					if(value.equals(attrName) && key.equals("certificates")) {
						NamingEnumeration<?> attrs = attr.getAll();
						int counter = 1;
						List<String> uniqueExtSources = new ArrayList<>();
						while(attrs.hasMore()) {
							byte[] cert = (byte[]) attrs.next();
							Pair<String, String> additionalues = getCertficiateSubjectAndIssure(cert, counter);
							//add to subject only if this extSource is unique
							if(!uniqueExtSources.contains(additionalues.getRight())) {
								uniqueExtSources.add(additionalues.getRight());
								subject.put(additionalues.getLeft(), additionalues.getRight());
								counter++;
							}
						}
						break;
					} else if(value.equals(attrName)) {
						//everything else has just 1 existence in LDAP for now
						if(attr.size() > 1) throw new InternalErrorException("Some attributes has more than 1 occurence and it is not attribute.");
						String attrValue = ((String) attr.get()).replaceAll("^.*: ", "");
						subject.put(key, attrValue);
						break;
					}
				}
			}
		} catch (NamingException ex) {
			log.error("Problem when listing through search results from EGI SSO.");
			throw new InternalErrorException("Problem when listing through search results from EGI SSO", ex);
		}

		return subject;
	}

	private Pair<String, String> getCertficiateSubjectAndIssure(byte[] certInDER, int counter) throws InternalErrorException {
		String additionalValue = "additionalues_" + counter;
		Pair<String, String> subjectCert;

		//save cartificate to /tmp directory to use it for openssl program
		Path tempDirectory = null;
		File byteFile = null;
		File infoFile = null;
		try {

			tempDirectory = Files.createTempDirectory("certificates");
			byteFile = File.createTempFile("egi-sso-cert-byte", ".txt", tempDirectory.toFile());
			infoFile = File.createTempFile("egi-sso-cert-info", ".txt", tempDirectory.toFile());

			try (FileOutputStream fos = new FileOutputStream(byteFile)) {
				fos.write(certInDER);
			}

			Process p;
			p = new ProcessBuilder("openssl", "x509", "-in", byteFile.getAbsolutePath(), "-subject", "-issuer", "-noout", "-inform", "DER")
					.inheritIO()
					.redirectOutput(infoFile)
					.redirectErrorStream(true)
					.directory(tempDirectory.toFile())
					.start();

			int exit;
			exit = p.waitFor();

			if(exit != 0) {
				throw new InternalErrorException("Return code of openssl is " + exit);
			}

			//read file again and export certificate infos
			String certIssuer = null;
			String certSubj = null;
			try(BufferedReader br = new BufferedReader(new FileReader(infoFile))) {
				String line;
				while((line = br.readLine()) != null) {
					if(line.matches("^issuer= .*")) {
						certIssuer = line.replaceAll("^issuer= ", "");
					} else if(line.matches("^subject= .*")) {
						certSubj = line.replaceAll("^subject= ", "");
					}
				}
			}

			if(certIssuer == null || certSubj == null) throw new InternalErrorException("CertSubject or CertIssuer is null.");
			subjectCert = new Pair(additionalValue, certIssuer + "|" + "cz.metacentrum.perun.core.impl.ExtSourceX509" + "|" + certSubj + "|2");
		} catch (IOException ex) {
			throw new InternalErrorException("IOException while working with files.", ex);
		} catch (InterruptedException ex) {
			throw new InternalErrorException("Process openssl was interrupted.", ex);
		} finally {
			try {
				if(byteFile != null) Files.deleteIfExists(byteFile.toPath());
				if(infoFile != null) Files.deleteIfExists(infoFile.toPath());
				if(tempDirectory != null) Files.delete(tempDirectory);
			} catch(IOException e) {
				throw new InternalErrorException("Can't delete some files", e);
			}
		}

		return subjectCert;
	}
}
