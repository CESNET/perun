package cz.metacentrum.perun.core.impl;

//Copyright 2003-2010 Christian d'Heureuse, Inventec Informatik AG, Zurich, Switzerland
//www.source-code.biz, www.inventec.ch/chdh
//
//This module is multi-licensed and may be used under the terms
//of any of the following licenses:
//
//EPL, Eclipse Public License, V1.0 or later, http://www.eclipse.org/legal
//LGPL, GNU Lesser General Public License, V2.1 or later, http://www.gnu.org/licenses/lgpl.html
//GPL, GNU General Public License, V2 or later, http://www.gnu.org/licenses/gpl.html
//AL, Apache License, V2.0 or later, http://www.apache.org/licenses
//BSD, BSD License, http://www.opensource.org/licenses/bsd-license.php
//
//Please contact the author if you need another license.
//This module is provided "as is", without warranties of any kind.

/**
 * A Base64 encoder/decoder.
 *
 * <p>
 * This class is used to encode and decode data in Base64 format as described in RFC 1521.
 *
 * <p>
 * Project home page: <a href="http://www.source-code.biz/base64coder/java/">www.source-code.biz/base64coder/java</a>
 * <br>
 * Author: Christian d'Heureuse, Inventec Informatik AG, Zurich, Switzerland<br> Multi-licensed: EPL / LGPL / GPL / AL /
 * BSD.
 */
public class Base64Coder {

  //The line separator string of the operating system.
  private static final String SYSTEM_LINE_SEPARATOR = System.getProperty("line.separator");

  //Mapping table from 6-bit nibbles to Base64 characters.
  private static final char[] MAP_1 = new char[64];
  //Mapping table from Base64 characters to 6-bit nibbles.
  private static final byte[] MAP_2 = new byte[128];

  static {
    int i = 0;
    for (char c = 'A'; c <= 'Z'; c++) {
      MAP_1[i++] = c;
    }
    for (char c = 'a'; c <= 'z'; c++) {
      MAP_1[i++] = c;
    }
    for (char c = '0'; c <= '9'; c++) {
      MAP_1[i++] = c;
    }
    MAP_1[i++] = '+';
    MAP_1[i] = '/';
  }

  static {
    for (int i = 0; i < MAP_2.length; i++) {
      MAP_2[i] = -1;
    }
    for (int i = 0; i < 64; i++) {
      MAP_2[MAP_1[i]] = (byte) i;
    }
  }

  //Dummy constructor.
  private Base64Coder() {
  }

  /**
   * Decodes a byte array from Base64 format. No blanks or line breaks are allowed within the Base64 encoded input
   * data.
   *
   * @param s A Base64 String to be decoded.
   * @return An array containing the decoded data bytes.
   * @throws IllegalArgumentException If the input is not valid Base64 encoded data.
   */
  public static byte[] decode(String s) {
    return decode(s.toCharArray());
  }

  /**
   * Decodes a byte array from Base64 format. No blanks or line breaks are allowed within the Base64 encoded input
   * data.
   *
   * @param in A character array containing the Base64 encoded data.
   * @return An array containing the decoded data bytes.
   * @throws IllegalArgumentException If the input is not valid Base64 encoded data.
   */
  public static byte[] decode(char[] in) {
    return decode(in, 0, in.length);
  }

  /**
   * Decodes a byte array from Base64 format. No blanks or line breaks are allowed within the Base64 encoded input
   * data.
   *
   * @param in   A character array containing the Base64 encoded data.
   * @param inputOffset Offset of the first character in <code>in</code> to be processed.
   * @param inputLength Number of characters to process in <code>in</code>, starting at <code>inputOffset</code>.
   * @return An array containing the decoded data bytes.
   * @throws IllegalArgumentException If the input is not valid Base64 encoded data.
   */
  public static byte[] decode(char[] in, int inputOffset, int inputLength) {
    if (inputLength % 4 != 0) {
      throw new IllegalArgumentException("Length of Base64 encoded input string is not a multiple of 4.");
    }
    while (inputLength > 0 && in[inputOffset + inputLength - 1] == '=') {
      inputLength--;
    }
    int outputLength = (inputLength * 3) / 4;
    byte[] out = new byte[outputLength];
    int ip = inputOffset;
    int inputEnd = inputOffset + inputLength;
    int op = 0;
    while (ip < inputEnd) {
      int i0 = in[ip++];
      int i1 = in[ip++];
      int i2 = ip < inputEnd ? in[ip++] : 'A';
      int i3 = ip < inputEnd ? in[ip++] : 'A';
      if (i0 > 127 || i1 > 127 || i2 > 127 || i3 > 127) {
        throw new IllegalArgumentException("Illegal character in Base64 encoded data.");
      }
      int b0 = MAP_2[i0];
      int b1 = MAP_2[i1];
      int b2 = MAP_2[i2];
      int b3 = MAP_2[i3];
      if (b0 < 0 || b1 < 0 || b2 < 0 || b3 < 0) {
        throw new IllegalArgumentException("Illegal character in Base64 encoded data.");
      }
      int o0 = (b0 << 2) | (b1 >>> 4);
      int o1 = ((b1 & 0xf) << 4) | (b2 >>> 2);
      int o2 = ((b2 & 3) << 6) | b3;
      out[op++] = (byte) o0;
      if (op < outputLength) {
        out[op++] = (byte) o1;
      }
      if (op < outputLength) {
        out[op++] = (byte) o2;
      }
    }
    return out;
  }

  /**
   * Decodes a byte array from Base64 format and ignores line separators, tabs and blanks. CR, LF, Tab and Space
   * characters are ignored in the input data. This method is compatible with
   * <code>sun.misc.BASE64Decoder.decodeBuffer(String)</code>.
   *
   * @param s A Base64 String to be decoded.
   * @return An array containing the decoded data bytes.
   * @throws IllegalArgumentException If the input is not valid Base64 encoded data.
   */
  public static byte[] decodeLines(String s) {
    char[] buf = new char[s.length()];
    int p = 0;
    for (int ip = 0; ip < s.length(); ip++) {
      char c = s.charAt(ip);
      if (c != ' ' && c != '\r' && c != '\n' && c != '\t') {
        buf[p++] = c;
      }
    }
    return decode(buf, 0, p);
  }

  /**
   * Decodes a string from Base64 format. No blanks or line breaks are allowed within the Base64 encoded input data.
   *
   * @param s A Base64 String to be decoded.
   * @return A String containing the decoded data.
   * @throws IllegalArgumentException If the input is not valid Base64 encoded data.
   */
  public static String decodeString(String s) {
    return new String(decode(s));
  }

  /**
   * Encodes a byte array into Base64 format. No blanks or line breaks are inserted in the output.
   *
   * @param in An array containing the data bytes to be encoded.
   * @return A character array containing the Base64 encoded data.
   */
  public static char[] encode(byte[] in) {
    return encode(in, 0, in.length);
  }

  /**
   * Encodes a byte array into Base64 format. No blanks or line breaks are inserted in the output.
   *
   * @param in          An array containing the data bytes to be encoded.
   * @param inputOffset Offset of the first byte in <code>in</code> to be processed.
   * @param inputLength Number of bytes to process in <code>in</code>, starting at <code>inputOffset</code>.
   * @return A character array containing the Base64 encoded data.
   */
  public static char[] encode(byte[] in, int inputOffset, int inputLength) {
    int outputDataLength = (inputLength * 4 + 2) / 3;       // output length without padding
    int outputLength = ((inputLength + 2) / 3) * 4;         // output length including padding
    char[] out = new char[outputLength];
    int ip = inputOffset;
    int inputEnd = inputOffset + inputLength;
    int op = 0;
    while (ip < inputEnd) {
      int i0 = in[ip++] & 0xff;
      int i1 = ip < inputEnd ? in[ip++] & 0xff : 0;
      int i2 = ip < inputEnd ? in[ip++] & 0xff : 0;
      int o0 = i0 >>> 2;
      int o1 = ((i0 & 3) << 4) | (i1 >>> 4);
      int o2 = ((i1 & 0xf) << 2) | (i2 >>> 6);
      int o3 = i2 & 0x3F;
      out[op++] = MAP_1[o0];
      out[op++] = MAP_1[o1];
      out[op] = op < outputDataLength ? MAP_1[o2] : '=';
      op++;
      out[op] = op < outputDataLength ? MAP_1[o3] : '=';
      op++;
    }
    return out;
  }

  /**
   * Encodes a byte array into Base64 format. No blanks or line breaks are inserted in the output.
   *
   * @param in   An array containing the data bytes to be encoded.
   * @param inputLength Number of bytes to process in <code>in</code>.
   * @return A character array containing the Base64 encoded data.
   */
  public static char[] encode(byte[] in, int inputLength) {
    return encode(in, 0, inputLength);
  }

  /**
   * Encodes a byte array into Base 64 format and breaks the output into lines of 76 characters. This method is
   * compatible with <code>sun.misc.BASE64Encoder.encodeBuffer(byte[])</code>.
   *
   * @param in An array containing the data bytes to be encoded.
   * @return A String containing the Base64 encoded data, broken into lines.
   */
  public static String encodeLines(byte[] in) {
    return encodeLines(in, 0, in.length, 76, SYSTEM_LINE_SEPARATOR);
  }

  /**
   * Encodes a byte array into Base 64 format and breaks the output into lines.
   *
   * @param in            An array containing the data bytes to be encoded.
   * @param inputOffset   Offset of the first byte in <code>in</code> to be processed.
   * @param inputLength   Number of bytes to be processed in <code>in</code>, starting at <code>inputOffset</code>.
   * @param lineLen       Line length for the output data. Should be a multiple of 4.
   * @param lineSeparator The line separator to be used to separate the output lines.
   * @return A String containing the Base64 encoded data, broken into lines.
   */
  public static String encodeLines(byte[] in, int inputOffset, int inputLength, int lineLen, String lineSeparator) {
    int blockLen = (lineLen * 3) / 4;
    if (blockLen <= 0) {
      throw new IllegalArgumentException();
    }
    int lines = (inputLength + blockLen - 1) / blockLen;
    int bufLen = ((inputLength + 2) / 3) * 4 + lines * lineSeparator.length();
    StringBuilder buf = new StringBuilder(bufLen);
    int ip = 0;
    while (ip < inputLength) {
      int l = Math.min(inputLength - ip, blockLen);
      buf.append(encode(in, inputOffset + ip, l));
      buf.append(lineSeparator);
      ip += l;
    }
    return buf.toString();
  }

  /**
   * Encodes a string into Base64 format. No blanks or line breaks are inserted.
   *
   * @param s A String to be encoded.
   * @return A String containing the Base64 encoded data.
   */
  public static String encodeString(String s) {
    return new String(encode(s.getBytes()));
  }

} // end class Base64Coder
