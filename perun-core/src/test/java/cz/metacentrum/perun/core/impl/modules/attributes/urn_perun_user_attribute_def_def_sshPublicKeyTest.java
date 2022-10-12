package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

public class urn_perun_user_attribute_def_def_sshPublicKeyTest {

	private static urn_perun_user_attribute_def_def_sshPublicKey classInstance;
	private static PerunSessionImpl session;
	private static User user;
	private static Attribute attributeToCheck;

	@Before
	public void setUp() {
		classInstance = new urn_perun_user_attribute_def_def_sshPublicKey();
		session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
		user = new User();
		attributeToCheck = new Attribute();
	}

	@Test
	public void testCheckCorrectRSAAttributeSyntax() throws Exception {
		System.out.println("testCheckCorrectRSAAttributeSyntax()");
		List<String> value = new ArrayList<>();
		value.add("ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABgQC7FPq20sXf+83P/mvfEBntaGUkVJu36X2gLIi5TioYPSqGVIPV+ztnhNUuJHQZ3HYRDhGw/5c32mIYKQvsAB0T/WT6hgs9zVHU1s5ieJSduxx9DqbEkHaZUirmukd8uF97QJm6Ve/cvS3YUb3yxWXcRiJX5jy1aRazoJgm/Vocgz/1PHInq46IQUN6I62ge7u5YrpSxym6Ehw8ZGCr7QyIyg5TdNVbK4flkf6LM/uKh0JuODfm+/R/3TjzbR/7oDzfkQR4TZE3sCHXpSEwaHbb4SM6if1di2PKefhlx9m7w0oMwaE6Epoq/US1FHxR0up+PQYqqwE+/fi9C88byT1Kjz7xpC3IV0bOdeP6nDcLDYsKssgotqU0YIrBCTes/an1efe1jrYZQvr54XvKNFWUnJsMJLosT2ZCWkNCyyrnL9V+KEJ07Qb4NAXfgcrVakP/6647FAXCgyY8Len9c/0aTn7SVd1aC3aTGRvLtvPNPzhbDJGKzjPs90So0GZ+q7s= martin@martin-ThinkPad-T480");
		attributeToCheck.setValue(value);

		classInstance.checkAttributeSyntax(session, user, attributeToCheck);
	}

	@Test
	public void testCheckCorrectEcdsaNistp256AttributeSyntax() throws Exception {
		System.out.println("testCheckCorrectEcdsaNistp256AttributeSyntax()");
		List<String> value = new ArrayList<>();
		value.add("ecdsa-sha2-nistp256 AAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBGgt1/rkRvQJp92tP8uxLJfy340lJGSSxsPp3+W1JdMbk+S2qIPwM5o/oblTjGhVRzKcas4pLrBz7L/Mxn6D6qw= martin@martin-ThinkPad-T480");
		attributeToCheck.setValue(value);

		classInstance.checkAttributeSyntax(session, user, attributeToCheck);
	}

	@Test
	public void testCheckCorrectEcdsaNistp384AttributeSyntax() throws Exception {
		System.out.println("testCheckCorrectEcdsaNistp384AttributeSyntax()");
		List<String> value = new ArrayList<>();
		value.add("ecdsa-sha2-nistp384 AAAAE2VjZHNhLXNoYTItbmlzdHAzODQAAAAIbmlzdHAzODQAAABhBEi2C/YeOHQ3RVo1WhZwOkEj5nHeX6eFJvpdVmxBIeiCNyVXGrUO1PXj13/ywlz6YaT9rs6CfpbV99gsvp7uZP1rbaGHm0ZuxWbt+C9WV3Br/hf2CZtNljDXCpNwjvCt0A== martin@martin-ThinkPad-T480");
		attributeToCheck.setValue(value);

		classInstance.checkAttributeSyntax(session, user, attributeToCheck);
	}

	@Test
	public void testCheckCorrectEcdsaNistp521AttributeSyntax() throws Exception {
		System.out.println("testCheckCorrectEcdsaNistp521AttributeSyntax()");
		List<String> value = new ArrayList<>();
		value.add("ecdsa-sha2-nistp521 AAAAE2VjZHNhLXNoYTItbmlzdHA1MjEAAAAIbmlzdHA1MjEAAACFBAFtrrFATKkxbaAKKBvl/X8SIbpJZffbCS0v2v9KLEE+6VEHqQFIDreZEXKbvk5/3JZ9Foh6+SJLE8fSyZKJE/0zcgEhhHfsYMdQASwshLSKpoaBNApo5gIqHD8a0WFIed86MOFIuB/Ux90X3an23bPSyM7Ri4mko3BXc8iAvvLQwf10HA== martin@martin-ThinkPad-T480");
		attributeToCheck.setValue(value);

		classInstance.checkAttributeSyntax(session, user, attributeToCheck);
	}

	@Test
	public void testCheckCorrectEd25519AttributeSyntax() throws Exception {
		System.out.println("testCheckCorrectEd25519AttributeSyntax()");
		List<String> value = new ArrayList<>();
		value.add("ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIJhGU1cLG0UldPhYxbEjKcZmFSZsGznmAYvra2QPls7a martin@martin-ThinkPad-T480");
		attributeToCheck.setValue(value);

		classInstance.checkAttributeSyntax(session, user, attributeToCheck);
	}

	@Test
	public void testCheckCorrectEcdsaSkAttributeSyntax() throws Exception {
		System.out.println("testCheckCorrectEcdsaSkAttributeSyntax()");
		List<String> value = new ArrayList<>();
		value.add("sk-ecdsa-sha2-nistp256@openssh.com AAAAInNrLWVjZHNhLXNoYTItbmlzdHAyNTZAb3BlbnNzaC5jb20AAAAIbmlzdHAyNTYAAABBBM1agPWb+qLilWO+bpaMxNIeMb7Nx6hs3stZeYhnlZYw51vV5GMmc8OGDbm/hiPRiqqSt7mgzdR6WYpwyvssHlwAAAAEc3NoOg== martin@martin-ThinkPad-T480");
		attributeToCheck.setValue(value);

		classInstance.checkAttributeSyntax(session, user, attributeToCheck);
	}

	@Test
	public void testCheckCorrectSshDssAttributeSyntax() throws Exception {
		System.out.println("testCheckCorrectSshDssAttributeSyntax()");
		List<String> value = new ArrayList<>();
		value.add("ssh-dss AAAAB3NzaC1kc3MAAACBAIrmRnnZf7Gy2YbrdSMUDdcpvMzbuzN8yZjm7AUa6QmIKdWJpj0EdKbRifKW35DpWW0GCnGuxxJTbiftnfVlEdFxZU8JRD1NLeYtJRVAli1J+nZ4UA4lGnf7kIj+QnPucLN3WbE5WQilQC+ZOL5k1KFe71BdJEQFCl1lRrNAUqb5AAAAFQDYZ7PrEgux3j86ovJtNkW+hkYwgQAAAIEAh+sld4f25d+y+7byuNj3W3G95KL8Q5KSu+zVh97MMHgdcBLr+7YrcaRcR33PLh65LUEFe8lyOo1BMyUHlq6dSisc1dCNMWPQZ1vkpMHPOuZ9q06pd2EiKt0IVqT33sgUnS52Zy4gX1HmCis9fKrSdFMc2+YsVJYTDrgodQfNLdAAAACAV3gFMKSsqbzE5t3L1lJ3oNcAWybCWcVjzfpHvtE8uia6UKg77TD2yHlhX3zxGvlJVwajQ968wxdrqaHZpZWCyswv/9+h3HKVtviWXVUw9/6b8vtwL1bWdr/atDNO3RHtxb+cL4hSr8X8rhyW35wMl1x09MtsPa62TTB+dNdva2s= peter@peter");
		attributeToCheck.setValue(value);

		classInstance.checkAttributeSyntax(session, user, attributeToCheck);
	}

	@Test
	public void testCheckCorrectSshRSAWithPrefixAttributeSyntax() throws Exception {
		System.out.println("testCheckCorrectSshRSAWithPrefixAttributeSyntax()");
		List<String> value = new ArrayList<>();
		value.add("command=\"cat - > ~/VO_cesnet_neant-tape_tape/sb/l-`date +%12s`.bumbrleetschek\" ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABgQC7FPq20sXf+83P/mvfEBntaGUkVJu36X2gLIi5TioYPSqGVIPV+ztnhNUuJHQZ3HYRDhGw/5c32mIYKQvsAB0T/WT6hgs9zVHU1s5ieJSduxx9DqbEkHaZUirmukd8uF97QJm6Ve/cvS3YUb3yxWXcRiJX5jy1aRazoJgm/Vocgz/1PHInq46IQUN6I62ge7u5YrpSxym6Ehw8ZGCr7QyIyg5TdNVbK4flkf6LM/uKh0JuODfm+/R/3TjzbR/7oDzfkQR4TZE3sCHXpSEwaHbb4SM6if1di2PKefhlx9m7w0oMwaE6Epoq/US1FHxR0up+PQYqqwE+/fi9C88byT1Kjz7xpC3IV0bOdeP6nDcLDYsKssgotqU0YIrBCTes/an1efe1jrYZQvr54XvKNFWUnJsMJLosT2ZCWkNCyyrnL9V+KEJ07Qb4NAXfgcrVakP/6647FAXCgyY8Len9c/0aTn7SVd1aC3aTGRvLtvPNPzhbDJGKzjPs90So0GZ+q7s= martin@martin-ThinkPad-T480");
		value.add("principals=\"oskar\" ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIJhGU1cLG0UldPhYxbEjKcZmFSZsGznmAYvra2QPls7a martin@martin-ThinkPad-T480");
		value.add("environment=\"GIT_AUTHOR_NAME=John Doe\",environment=\"GIT_AUTHOR_EMAIL=john.doe@email.cz\" ecdsa-sha2-nistp521 AAAAE2VjZHNhLXNoYTItbmlzdHA1MjEAAAAIbmlzdHA1MjEAAACFBAFtrrFATKkxbaAKKBvl/X8SIbpJZffbCS0v2v9KLEE+6VEHqQFIDreZEXKbvk5/3JZ9Foh6+SJLE8fSyZKJE/0zcgEhhHfsYMdQASwshLSKpoaBNApo5gIqHD8a0WFIed86MOFIuB/Ux90X3an23bPSyM7Ri4mko3BXc8iAvvLQwf10HA==");
		attributeToCheck.setValue(value);

		classInstance.checkAttributeSyntax(session, user, attributeToCheck);
	}


	@Test(expected = WrongAttributeValueException.class)
	public void testCheckSshED25519AttributeSyntaxWithWrongPrefix() throws Exception {
		System.out.println("testCheckSshED25519AttributeSyntaxWithWrongPrefix()");
		List<String> value = new ArrayList<>();
		value.add("ssh-rsa environment=\"GIT_AUTHOR_NAME=John Doe\",environment=\"GIT_AUTHOR_EMAIL=john.doe@email.cz\" ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIJhGU1cLG0UldPhYxbEjKcZmFSZsGznmAYvra2QPls7a martin@martin-ThinkPad-T480");
		attributeToCheck.setValue(value);

		classInstance.checkAttributeSyntax(session, user, attributeToCheck);
	}

	@Test(expected = WrongAttributeValueException.class)
	public void testCheckAttributeSyntaxWithWrongValue() throws Exception {
		System.out.println("testCheckAttributeSyntaxWithWrongValue()");
		List<String> value = new ArrayList<>();
		value.add("bad_example\n");
		attributeToCheck.setValue(value);

		classInstance.checkAttributeSyntax(session, user, attributeToCheck);
	}

	@Test(expected = WrongAttributeValueException.class)
	public void testCheckAttributeSyntaxWithEmptyKey() throws Exception {
		System.out.println("testCheckAttributeSyntaxWithEmptyKey()");
		List<String> value = new ArrayList<>();
		value.add("");
		attributeToCheck.setValue(value);

		classInstance.checkAttributeSyntax(session, user, attributeToCheck);
	}

	@Test(expected = WrongAttributeValueException.class)
	public void testCheckAttributeSyntaxWithInvalidTypePosition() throws Exception {
		System.out.println("testCheckAttributeSyntaxWithWrongValue()");
		List<String> value = new ArrayList<>();
		value.add("ecdsa-sha2-nistp256 some_random_text AAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBGgt1/rkRvQJp92tP8uxLJfy340lJGSSxsPp3+W1JdMbk+S2qIPwM5o/oblTjGhVRzKcas4pLrBz7L/Mxn6D6qw= martin@martin-ThinkPad-T480");
		attributeToCheck.setValue(value);

		classInstance.checkAttributeSyntax(session, user, attributeToCheck);
	}

}
