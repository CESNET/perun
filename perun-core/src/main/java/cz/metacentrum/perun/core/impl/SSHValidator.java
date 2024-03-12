package cz.metacentrum.perun.core.impl;

import com.google.api.client.util.Base64;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.SSHKeyNotValidException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.AlgorithmParameters;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.DSAPublicKeySpec;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Arrays;
import java.util.List;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validates SSH keys using predefined rules and the `ssh-keygen` tool as a final check
 *
 * @author David Flor <davidflor@seznam.cz>
 */
public class SSHValidator {

  private static final Logger LOG = LoggerFactory.getLogger(SSHValidator.class);

  private static final String SSH_RSA = "ssh-rsa";
  private static final String SSH_DSS = "ssh-dss";
  private static final String ECDSA_SHA2_NISTP256 = "ecdsa-sha2-nistp256";
  private static final String ECDSA_SHA2_NISTP384 = "ecdsa-sha2-nistp384";
  private static final String ECDSA_SHA2_NISTP521 = "ecdsa-sha2-nistp521";
  private static final String SSH_ED25519 = "ssh-ed25519";
  private static final String SSH_ED25519_CERT = "ssh-ed25519-cert-v01@openssh.com";
  private static final String SK_SSH_ED25519 = "sk-ssh-ed25519@openssh.com";
  private static final String SK_SSH_ED25519_CERT = "sk-ssh-ed25519-cert-v01@openssh.com";
  private static final String SK_ECDSA_SHA2_NISTP256 = "sk-ecdsa-sha2-nistp256@openssh.com";
  private static final String SSH_RSA_CERT = "ssh-rsa-cert-v01@openssh.com";
  private static final String SSH_DSS_CERT = "ssh-dss-cert-v01@openssh.com";
  private static final String ECDSA_SHA2_NISTP256_CERT = "ecdsa-sha2-nistp256-cert-v01@openssh.com";
  private static final String ECDSA_SHA2_NISTP384_CERT = "ecdsa-sha2-nistp384-cert-v01@openssh.com";
  private static final String ECDSA_SHA2_NISTP521_CERT = "ecdsa-sha2-nistp521-cert-v01@openssh.com";
  private static final String SK_ECDSA_SHA2_NISTP256_CERT = "sk-ecdsa-sha2-nistp256-cert-v01@openssh.com";

  private static final List<String> ALLOWED_SSH_TYPES =
      List.of(SSH_RSA, SSH_DSS, ECDSA_SHA2_NISTP256, ECDSA_SHA2_NISTP384, ECDSA_SHA2_NISTP521, SSH_ED25519,
          SSH_ED25519_CERT, SK_SSH_ED25519, SK_SSH_ED25519_CERT, SK_ECDSA_SHA2_NISTP256, SSH_RSA_CERT, SSH_DSS_CERT,
          ECDSA_SHA2_NISTP256_CERT, ECDSA_SHA2_NISTP384_CERT, ECDSA_SHA2_NISTP521_CERT, SK_ECDSA_SHA2_NISTP256_CERT);

  // for now without cert variant
  private static final List<String> RSA_SSH_TYPES = List.of(SSH_RSA);

  // for now without cert variant
  private static final List<String> ECDSA_SSH_TYPES =
      List.of(ECDSA_SHA2_NISTP256, ECDSA_SHA2_NISTP384, ECDSA_SHA2_NISTP521, SK_ECDSA_SHA2_NISTP256);

  // for now without cert variant
  private static final List<String> DSA_SSH_TYPES = List.of(SSH_DSS);

  private SSHValidator() {
  }


  /**
   * Checks whether is the SSH key in the correct format.
   *
   * @param sshKey SSH key to be checked
   * @throws SSHKeyNotValidException that is thrown whenever SSH key is not in correct format
   */
  public static void validateSSH(String sshKey) throws SSHKeyNotValidException {
    if (sshKey == null || sshKey.isEmpty()) {
      throw new SSHKeyNotValidException("SSH key has to cannot be empty or null");
    }
    try {
      sshKey = removeSSHKeyCommandPrefix(sshKey);
      int[] pos = {0};
      byte[] sshBase64KeyBytes;

      String[] sshKeyParts = sshKey.split(" ");
      if (sshKeyParts.length < 2) {
        throw new SSHKeyNotValidException(
            "SSH public key has to consists at least from the key type and the Base64 encoded public key.");
      }

      String sshKeyType = sshKeyParts[0];
      if (!ALLOWED_SSH_TYPES.contains(sshKeyType)) {
        throw new SSHKeyNotValidException(
            "The " + sshKeyType + " key type is not allowed. Allowed types are: " + ALLOWED_SSH_TYPES + ".");
      }

      try {
        sshBase64KeyBytes = Base64.decodeBase64(sshKeyParts[1]);
      } catch (Exception exception) {
        throw new SSHKeyNotValidException("Provided Base64 encoded public key is not valid.");
      }

      String sshBase64KeyType = decodeType(sshBase64KeyBytes, pos);
      if (!sshBase64KeyType.equals(sshKeyType)) {
        throw new SSHKeyNotValidException("SSH types are not same. Type defined before the Base64 is: " + sshKeyType +
                                          " and type inside the Base64 is: " + sshBase64KeyType + ".");
      }

      try {
        if (RSA_SSH_TYPES.contains(sshKeyType)) {
          decodeRSA(sshBase64KeyBytes, pos);
        } else if (DSA_SSH_TYPES.contains(sshKeyType)) {
          decodeDSA(sshBase64KeyBytes, pos);
        } else if (ECDSA_SSH_TYPES.contains(sshKeyType)) {
          decodeEcdsa(sshBase64KeyBytes, pos);
        }
      } catch (Exception ex) {
        throw new SSHKeyNotValidException("Provided Base64 encoded public key is not valid.");
      }
    } catch (Exception e) {
      throw new SSHKeyNotValidException("Invalid SSH key format:  " + e.getMessage());
    }
    // check one more time with ssh-keygen for edge cases
    runSshKeygen(sshKey);
  }

  /**
   * Validates ssh public key using the ssh-keygen tool
   *
   * @param sshKey ssh public key to verify
   * @throws SSHKeyNotValidException when validation fails
   */
  private static void runSshKeygen(String sshKey) throws SSHKeyNotValidException {
    File tempSSH = null;
    try {
      tempSSH = File.createTempFile("perunSSHAttr", ".txt");

      try (BufferedWriter bw = new BufferedWriter(new FileWriter(tempSSH))) {
        bw.write(sshKey);
      }
      ProcessBuilder pb = new ProcessBuilder("ssh-keygen", "-l", "-f", tempSSH.getAbsolutePath());
      Process process = pb.start();

      // String result = new String(process.getInputStream().readAllBytes());
      String error = new String(process.getErrorStream().readAllBytes());

      int returnCode = process.waitFor();
      if (returnCode != 0) {
        LOG.error("SSH validation error: " + error + " for key: " + sshKey + " with error code: " + returnCode);
        if (returnCode == 255) {
          throw new SSHKeyNotValidException("Provided SSH key is not valid");
        }
        throw new SSHKeyNotValidException("SSH validation failed with: " + error);
      }
    } catch (IOException | InterruptedException ex) {
      throw new InternalErrorException("File error while verifying ssh key");
    } finally {
      try {
        if (tempSSH != null) {
          Files.deleteIfExists(tempSSH.toPath());
        }
      } catch (IOException ex) {
        LOG.error("Failed to remove file: " + tempSSH.getAbsolutePath() + " after verifying ssh key");
        throw new InternalErrorException("Couldn't delete files after verifying ssh key");
      }
    }
  }

  /**
   * Removes any potential command prefix before the ssh key
   *
   * @param sshKey raw ssh key value from the attribute
   * @return SSH key without the command prefix
   */
  private static String removeSSHKeyCommandPrefix(String sshKey) {
    // entries in authorized_keys are of this format (from man page):
    // "Public keys consist of the following space-separated fields: options, keytype, base64-encoded key, comment.
    // The options field is optional."

    // split on spaces outside of quotes
    String[] sshKeyParts = sshKey.split(" (?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

    // check whether key has options, cut them if so
    if (!ALLOWED_SSH_TYPES.contains(sshKeyParts[0])) {
      String[] keyPartsWithoutPrefix = Arrays.copyOfRange(sshKeyParts, 1, sshKeyParts.length);
      return String.join(" ", keyPartsWithoutPrefix);
    }
    return sshKey;

  }

  /**
   * Checks whether is the key in the correct ecdsa format.
   *
   * @param bytes Data of SSH key encoded in Base64
   * @param pos   Position in the 'bytes'
   * @throws NoSuchAlgorithmException Should never occur since hardcoded value is used
   * @throws InvalidKeySpecException  Thrown if the used algorithm does not match the EC specification
   */
  private static void decodeEcdsa(byte[] bytes, int[] pos) throws NoSuchAlgorithmException, InvalidKeySpecException {
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
  private static void decodeDSA(byte[] bytes, int[] pos) throws NoSuchAlgorithmException, InvalidKeySpecException {
    BigInteger p = decodeBigInt(bytes, pos);
    BigInteger q = decodeBigInt(bytes, pos);
    BigInteger g = decodeBigInt(bytes, pos);
    BigInteger y = decodeBigInt(bytes, pos);
    DSAPublicKeySpec spec = new DSAPublicKeySpec(y, p, q, g);
    KeyFactory.getInstance("DSA").generatePublic(spec);
  }

  /**
   * Checks whether is the key in the correct rsa format.
   *
   * @param bytes Data of SSH key encoded in Base64
   * @param pos   Position in the 'bytes'
   * @throws NoSuchAlgorithmException Should never occur since hardcoded value is used
   * @throws InvalidKeySpecException  Thrown if the used algorithm does not match the RSA specification
   */
  private static void decodeRSA(byte[] bytes, int[] pos) throws NoSuchAlgorithmException, InvalidKeySpecException {
    BigInteger exponent = decodeBigInt(bytes, pos);
    BigInteger modulus = decodeBigInt(bytes, pos);
    RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
    KeyFactory.getInstance("RSA").generatePublic(spec);
  }

  /**
   * Provides means to get from a parsed Q value to the X and Y point values. that can be used to create and ECPoint
   * compatible with ECPublicKeySpec.
   *
   * @param q          According to RFC 5656: "Q is the public key encoded from an elliptic curve point into an octet
   *                   string"
   * @param identifier According to RFC 5656: "The string [identifier] is the identifier of the elliptic curve domain
   *                   parameters."
   * @return An ECPoint suitable for creating a JCE ECPublicKeySpec.
   */
  private static ECPoint getECPoint(BigInteger q, String identifier) {
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
   * @param identifier According to RFC 5656: "The string [identifier] is the identifier of the elliptic curve domain
   *                   parameters."
   * @return An ECParameterSpec suitable for creating a JCE ECPublicKeySpec.
   */
  private static ECParameterSpec getECParameterSpec(String identifier) {
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
  private static String decodeType(byte[] bytes, int[] pos) {
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
  private static int decodeInt(byte[] bytes, int[] pos) {
    return ((bytes[pos[0]++] & 0xFF) << 24) | ((bytes[pos[0]++] & 0xFF) << 16) | ((bytes[pos[0]++] & 0xFF) << 8) |
           (bytes[pos[0]++] & 0xFF);
  }

  /**
   * Decodes big integer part encoded in provided data. Usually used when validating concrete SSH algorithm.
   *
   * @param bytes Data of SSH key encoded in Base64
   * @param pos   Position in the 'bytes'
   * @return Big integer part of SSH key
   */
  private static BigInteger decodeBigInt(byte[] bytes, int[] pos) {
    int len = decodeInt(bytes, pos);
    byte[] bigIntBytes = new byte[len];
    System.arraycopy(bytes, pos[0], bigIntBytes, 0, len);
    pos[0] += len;
    return new BigInteger(bigIntBytes);
  }

}
