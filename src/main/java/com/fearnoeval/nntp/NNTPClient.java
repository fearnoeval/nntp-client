package com.fearnoeval.nntp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A small NNTP client. Commands are sent as byte arrays and responses are
 * returned as byte arrays. Commands must be provided with their terminating
 * characters (CRLF). Responses are returned with their status code and all
 * terminating characters.
 */
public final class NNTPClient {

  /**
   * Writes data to the socket and returns a single- or multi-line response from
   * the socket as a byte array based on the response code received from the
   * socket. If the response code is known to return a multi-line response, a
   * multi-line response is returned. All other responses are returned as a
   * single-line response. Officially supports all response codes defined in
   * RFC 3977 and RFC 2980.
   *
   * @param  socket      the socket with an active connection to an NNTP server
   * @param  dataToWrite the data to write as bytes
   * @return             the response from the server as bytes
   */
  public static byte[] writeAndRead(final Socket socket, final byte[] dataToWrite) throws IOException {
    return writeAndRead(socket, dataToWrite, 0, dataToWrite.length);
  }

  /**
   * Writes <code>length</code> bytes from the passed byte array starting at
   * <code>offset</code> to the socket and returns a single- or multi-line
   * response from the socket as a byte array based on the response code
   * received from the socket. If the response code is known to return a
   * multi-line response, a multi-line response is returned. All other responses
   * are returned as a single-line response. Officially supports all response
   * codes defined in RFC 3977 and RFC 2980.
   *
   * @param  socket      the socket with an active connection to an NNTP server
   * @param  dataToWrite the data to write as bytes
   * @param  offset      start offset into the data to write
   * @param  length      the number of bytes to write
   * @return             the response from the server as bytes
   */
  public static byte[] writeAndRead(final Socket socket, final byte[] dataToWrite, final int offset, final int length) throws IOException {
    return writeAndRead(socket, dataToWrite, offset, length, defaultMultiLineResponseCodes);
  }

  /**
   * Returns a single-line response from the socket as a byte array.
   *
   * @param  socket the socket with an active connection to an NNTP server
   * @return        the response from the server as bytes
   */
  public static byte[] readSingleLine(final Socket socket) throws IOException {
    return readSingleLine(socket, new ByteArrayOutputStream());
  }

  /**
   * Returns a multi-line response from the socket as a byte array.
   *
   * @param  socket the socket with an active connection to an NNTP server
   * @return        the response from the server as bytes
   */
  public static byte[] readMultiLine(final Socket socket) throws IOException {
    return readMultiLine(socket, new ByteArrayOutputStream());
  }

  /**
   * Writes data to the socket.
   *
   * @param socket      the socket with an active connection to an NNTP server
   * @param dataToWrite the data to write as bytes
   */
  public static void write(final Socket socket, final byte[] dataToWrite) throws IOException {
    write(socket, dataToWrite, 0, dataToWrite.length);
  }

  /**
   * Writes <code>length</code> bytes from the passed byte array starting at
   * <code>offset</code> to the socket.
   *
   * @param socket      the socket with an active connection to an NNTP server
   * @param dataToWrite the data to write as bytes
   * @param offset      start offset into the data to write
   * @param length      the number of bytes to write
   */
  public static void write(final Socket socket, final byte[] dataToWrite, final int offset, final int length) throws IOException {
    socket.getOutputStream().write(dataToWrite, offset, length);
  }

  private NNTPClient() {}

  /**
   * Unmodifiable set of strings of the default multi-line response codes.
   */
  public static final Set<String> defaultMultiLineResponseCodes;
  static {
    final Set<String> s = new HashSet<>(10, 1.0f);
    s.add("100");
    s.add("101");
    s.add("215");
    s.add("220");
    s.add("221");
    s.add("222");
    s.add("224");
    s.add("225");
    s.add("230");
    s.add("231");
    defaultMultiLineResponseCodes = Collections.unmodifiableSet(s);
  }

  private static byte[] writeAndRead(final Socket socket, final byte[] dataToWrite, final int offset, final int length, final Set<String> multiLineResponseCodes) throws IOException {
    write(socket, dataToWrite, offset, length);

    final byte[] responseCode = new byte[3];
    socket.getInputStream().read(responseCode);
    final String responseCodeString = new String(responseCode, StandardCharsets.UTF_8);

    if (responseCodeString.equals(_211)) {
      return read211(socket, dataToWrite);
    }
    if (multiLineResponseCodes.contains(responseCodeString)) {
      return readMultiLine(socket, responseCode);
    }
    return readSingleLine(socket, responseCode);
  }

  private static final String _211      = "211";
  private static final byte[] _211Array = _211.getBytes(StandardCharsets.UTF_8);
  private static final String listgroup = "LISTGROUP";

  private static boolean isListgroup(final byte[] maybeCommand) {
    return (maybeCommand.length >= listgroup.length()) && new String(maybeCommand, 0, listgroup.length(), StandardCharsets.UTF_8).toUpperCase().equals(listgroup);
  }

  private static byte[] read211(final Socket socket, final byte[] maybeCommand) throws IOException {
    return (isListgroup(maybeCommand)) ? readMultiLine(socket, _211Array) : readSingleLine(socket, _211Array);
  }

  private static final int cr  = '\r';
  private static final int lf  = '\n';
  private static final int dot = '.';

  private static byte[] readSingleLine(final Socket socket, final byte[] responseCode) throws IOException {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    baos.write(responseCode);
    return readSingleLine(socket, baos);
  }

  private static byte[] readSingleLine(final Socket socket, final ByteArrayOutputStream baos) throws IOException {
    final InputStream is = socket.getInputStream();

    int b;

    for (;;) {
      b = is.read();
      baos.write(b);
      if (b == cr) {
        b = is.read();
        baos.write(b);
        if (b == lf) {
          return baos.toByteArray();
        }
      }
    }
  }

  private static byte[] readMultiLine(final Socket socket, final byte[] responseCode) throws IOException {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    baos.write(responseCode);
    return readMultiLine(socket, baos);
  }

  private static byte[] readMultiLine(final Socket socket, final ByteArrayOutputStream baos) throws IOException {
    final InputStream is = socket.getInputStream();

    int b;

    for (;;) {
      b = is.read();
      baos.write(b);
      if (b == cr) {
        b = is.read();
        baos.write(b);
        if (b == lf) {
          b = is.read();
          baos.write(b);
          if (b == dot) {
            b = is.read();
            baos.write(b);
            if (b == cr) {
              b = is.read();
              baos.write(b);
              if (b == lf) {
                return baos.toByteArray();
              }
            }
          }
        }
      }
    }
  }
}
