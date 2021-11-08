package cz.metacentrum.perun.core.impl.modules.attributes;

import com.google.api.client.util.Base64;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleImplApi;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;

import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.DSAPublicKeySpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Jakub Peschel <jakubpeschel@gmail.com>
 */
public class urn_perun_user_attribute_def_def_sshPublicKey extends UserAttributesModuleAbstract implements UserAttributesModuleImplApi {

	private final Pattern pattern = Pattern.compile("^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)?$");
	final String RSA = "ssh-rsa";
	final String DSA = "ssh-dsa";
	final String ECDSA_SHA2_NISTP256 = "ecdsa-sha2-nistp256";
	final String ECDSA_SHA2_NISTP384 = "ecdsa-sha2-nistp384";
	final String ECDSA_SHA2_NISTP521 = "ecdsa-sha2-nistp521";
	final String ED25519 = "ssh-ed25519";
	final String SK_ED25519 = "sk-ed25519";
	final String SK_ECDSA = "sk-ecdsa";

	@Override
	public void checkAttributeSyntax(PerunSessionImpl perunSession, User user, Attribute attribute) throws WrongAttributeValueException {
		//Null in value is ok here
		if (attribute.getValue() == null) return;

		//Testing if some ssh key contains new line character
		List<String> sshKeys = attribute.valueAsList();
		for (String sshKey : sshKeys) {
			if (sshKey != null) {
				if (sshKey.contains("\n"))
					throw new WrongAttributeValueException(attribute, user, "One of keys in attribute contains new line character. New line character is not allowed here.");
				try {
					validateSSH(sshKey);
				} catch (Exception e) {
					throw new WrongAttributeValueException("Invalid SSH key format: " + e.getMessage());
				}
			}
		}
	}

	/**
	 * Checks whether is the SSH key in the correct format.
	 *
	 * @param sshKey SSH key to be checked
	 * @throws Exception that is thrown whenever SSH key is not in correct format
	 */
	private void validateSSH(String sshKey) throws Exception {
		byte[] bytes = null;
		int[] pos = {0};

		// look for the Base64 encoded part of the line to decode
		boolean typeFound = false;
		String sshKeyTypeValue = "";

		List<String> sshTypes = Arrays.asList(RSA, DSA, ECDSA_SHA2_NISTP256, ECDSA_SHA2_NISTP384, ECDSA_SHA2_NISTP521, ED25519);

		for (String part : sshKey.split(" ")) {
			Matcher matcher = pattern.matcher(part);
			if (matcher.matches()) {
				if (typeFound) {
					bytes = Base64.decodeBase64(part);
					break;
				} else {
					throw new IllegalArgumentException("type hasn't been found before the key part");
				}
			} else if (typeFound) {
				throw new IllegalArgumentException("Base64 encoded part expected to be right after ssh type");
			}
			typeFound = sshTypes.contains(part) || part.startsWith(SK_ECDSA) || part.startsWith(SK_ED25519);
			if (typeFound) {
				sshKeyTypeValue = part;
			}
		}
		if (bytes == null) {
			throw new IllegalArgumentException("Base64 part hasn't been found");
		}

		String sshKeyType = decodeType(bytes, pos);
		if (!sshKeyType.equals(sshKeyTypeValue)) {
			throw new IllegalArgumentException("the type in the key part and the type before the key part does not match");
		}

		try {
			if (sshKeyType.equals(RSA)) {
				decodeRSA(bytes, pos);
			} else if (sshKeyType.equals(DSA)) {
				decodeDSA(bytes, pos);
			} else if (sshKeyType.equals(ECDSA_SHA2_NISTP256)
				|| sshKeyType.equals(ECDSA_SHA2_NISTP384)
				|| sshKeyType.equals(ECDSA_SHA2_NISTP521)) {
				decodeEcdsa(bytes, pos);
			} else if (!(sshKeyType.startsWith(SK_ECDSA) || sshKeyType.equals(ED25519) || sshKeyType.startsWith(SK_ED25519))) {
				throw new IllegalArgumentException("unknown type " + sshKeyType);
			}
		} catch (InvalidKeySpecException e) {
			throw new IllegalArgumentException("wrong key specification");
		}
	}

	/**
	 * Checks whether is the key in the correct ecdsa format.
	 *
	 * @param bytes Data of SSH key encoded in Base64
	 * @param pos   Position in the 'bytes'
	 * @throws NoSuchAlgorithmException Should never occur since hardcoded value is used
	 * @throws InvalidKeySpecException  Thrown if the used algorithm does not match the EC specification
	 */
	private void decodeEcdsa(byte[] bytes, int[] pos) throws NoSuchAlgorithmException, InvalidKeySpecException {
		// Based on RFC 5656, section 3.1 (https://tools.ietf.org/html/rfc5656#section-3.1)
		String identifier = decodeType(bytes, pos);
		BigInteger publicKey = decodeBigInt(bytes, pos);
		ECPoint ecPoint = getECPoint(publicKey, identifier);
		ECParameterSpec ecParameterSpec = getECParameterSpec(identifier);
		ECPublicKeySpec spec = new ECPublicKeySpec(ecPoint, ecParameterSpec);
		KeyFactory.getInstance("EC").generatePublic(spec);
	}

	/**
	 * Checks whether is the key in the correct dsa format.
	 *
	 * @param bytes Data of SSH key encoded in Base64
	 * @param pos   Position in the 'bytes'
	 * @throws NoSuchAlgorithmException Should never occur since hardcoded value is used
	 * @throws InvalidKeySpecException  Thrown if the used algorithm does not match the DSA specification
	 */
	private void decodeDSA(byte[] bytes, int[] pos) throws NoSuchAlgorithmException, InvalidKeySpecException {
		BigInteger p = decodeBigInt(bytes, pos);
		BigInteger q = decodeBigInt(bytes, pos);
		BigInteger g = decodeBigInt(bytes, pos);
		BigInteger y = decodeBigInt(bytes, pos);
		DSAPublicKeySpec spec = new DSAPublicKeySpec(y, p, q, g);
		KeyFactory.getInstance("DSA").generatePublic(spec);
	}

	/**
	 * Checks whether is the key in the correct ecdsa format.
	 *
	 * @param bytes Data of SSH key encoded in Base64
	 * @param pos   Position in the 'bytes'
	 * @throws NoSuchAlgorithmException Should never occur since hardcoded value is used
	 * @throws InvalidKeySpecException  Thrown if the used algorithm does not match the RSA specification
	 */
	private void decodeRSA(byte[] bytes, int[] pos) throws NoSuchAlgorithmException, InvalidKeySpecException {
		BigInteger exponent = decodeBigInt(bytes, pos);
		BigInteger modulus = decodeBigInt(bytes, pos);
		RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
		KeyFactory.getInstance("RSA").generatePublic(spec);
	}

	/**
	 * Provides means to get from a parsed Q value to the X and Y point values.
	 * that can be used to create and ECPoint compatible with ECPublicKeySpec.
	 *
	 * @param q          According to RFC 5656:
	 *                   "Q is the public key encoded from an elliptic curve point into an octet string"
	 * @param identifier According to RFC 5656:
	 *                   "The string [identifier] is the identifier of the elliptic curve domain parameters."
	 * @return An ECPoint suitable for creating a JCE ECPublicKeySpec.
	 */
	private ECPoint getECPoint(BigInteger q, String identifier) {
		String name = identifier.replace("nist", "sec") + "r1";
		ECNamedCurveParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec(name);
		org.bouncycastle.math.ec.ECPoint point = ecSpec.getCurve().decodePoint(q.toByteArray());
		BigInteger x = point.getAffineXCoord().toBigInteger();
		BigInteger y = point.getAffineYCoord().toBigInteger();
		return new ECPoint(x, y);
	}

	/**
	 * Gets the curve parameters for the given key type identifier.
	 *
	 * @param identifier According to RFC 5656:
	 *                   "The string [identifier] is the identifier of the elliptic curve domain parameters."
	 * @return An ECParameterSpec suitable for creating a JCE ECPublicKeySpec.
	 */
	private ECParameterSpec getECParameterSpec(String identifier) {
		try {
			// http://www.bouncycastle.org/wiki/pages/viewpage.action?pageId=362269#SupportedCurves(ECDSAandECGOST)-NIST(aliasesforSECcurves)
			String name = identifier.replace("nist", "sec") + "r1";
			AlgorithmParameters parameters = AlgorithmParameters.getInstance("EC");
			parameters.init(new ECGenParameterSpec(name));
			return parameters.getParameterSpec(ECParameterSpec.class);
		} catch (InvalidParameterSpecException | NoSuchAlgorithmException e) {
			throw new IllegalArgumentException("Unable to parse curve parameters: ", e);
		}
	}

	/**
	 * Decodes type of SSh key encoded in provided data
	 *
	 * @param bytes Data of SSH key encoded in Base64
	 * @param pos   Position in the 'bytes'
	 * @return Type of SSH key
	 */
	private String decodeType(byte[] bytes, int[] pos) {
		int len = decodeInt(bytes, pos);
		String type = new String(bytes, pos[0], len);
		pos[0] += len;
		return type;
	}

	/**
	 * Decodes integer part encoded in provided data. Usually used when validating concrete SSH algorithm.
	 *
	 * @param bytes Data of SSH key encoded in Base64
	 * @param pos   Position in the 'bytes'
	 * @return Integer part of SSH key
	 */
	private int decodeInt(byte[] bytes, int[] pos) {
		return ((bytes[pos[0]++] & 0xFF) << 24) | ((bytes[pos[0]++] & 0xFF) << 16)
			| ((bytes[pos[0]++] & 0xFF) << 8) | (bytes[pos[0]++] & 0xFF);
	}

	/**
	 * Decodes big integer part encoded in provided data. Usually used when validating concrete SSH algorithm.
	 *
	 * @param bytes Data of SSH key encoded in Base64
	 * @param pos   Position in the 'bytes'
	 * @return Big integer part of SSH key
	 */
	private BigInteger decodeBigInt(byte[] bytes, int[] pos) {
		int len = decodeInt(bytes, pos);
		byte[] bigIntBytes = new byte[len];
		System.arraycopy(bytes, pos[0], bigIntBytes, 0, len);
		pos[0] += len;
		return new BigInteger(bigIntBytes);
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attr.setFriendlyName("sshPublicKey");
		attr.setDisplayName("Public ssh key");
		attr.setType(ArrayList.class.getName());
		attr.setDescription("User's SSH public keys.");
		return attr;
	}
}
