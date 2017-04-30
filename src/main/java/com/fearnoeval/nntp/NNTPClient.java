package com.fearnoeval.nntp;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import java.io.IOException;

public final class NNTPClient {

  public static byte[] writeAndRead(final OutputStream outputStream, final InputStream inputStream, final byte[] dataToWrite) throws IOException {
    return writeAndRead(outputStream, inputStream, dataToWrite, defaultMultiLineResponseCodes);
  }

  public static byte[] readSingleLine(final InputStream inputStream) throws IOException {
    return readSingleLine(inputStream, new ByteArrayOutputStream());
  }

  public static byte[] readMultiLine(final InputStream inputStream) throws IOException {
    return readMultiLine(inputStream, new ByteArrayOutputStream());
  }

  private NNTPClient() {}

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

  private static byte[] writeAndRead(final OutputStream outputStream, final InputStream inputStream, final byte[] dataToWrite, final Set<String> multiLineResponseCodes) throws IOException {
    outputStream.write(dataToWrite);
    outputStream.flush();

    final byte[] responseCode = new byte[3];
    inputStream.read(responseCode);
    final String responseCodeString = new String(responseCode, StandardCharsets.UTF_8);

    if (responseCodeString.equals(_211)) {
      return read211(inputStream, dataToWrite);
    }
    if (multiLineResponseCodes.contains(responseCodeString)) {
      return readMultiLine(inputStream, responseCode);
    }
    return readSingleLine(inputStream, responseCode);
  }

  private static final String _211      = "211";
  private static final byte[] _211Array = _211.getBytes(StandardCharsets.UTF_8);
  private static final String listgroup = "LISTGROUP";

  private static boolean isListgroup(final byte[] maybeCommand) {
    return (maybeCommand.length >= listgroup.length()) && new String(maybeCommand, 0, listgroup.length(), StandardCharsets.UTF_8).toUpperCase().equals(listgroup);
  }

  private static byte[] read211(final InputStream inputStream, final byte[] maybeCommand) throws IOException {
    return (isListgroup(maybeCommand)) ? readMultiLine(inputStream, _211Array) : readSingleLine(inputStream, _211Array);
  }

  private static final int cr  = 13;
  private static final int lf  = 10;
  private static final int dot = 46;

  private static byte[] readSingleLine(final InputStream inputStream, final byte[] responseCode) throws IOException {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    baos.write(responseCode);
    return readSingleLine(inputStream, baos);
  }

  private static byte[] readSingleLine(final InputStream is, final ByteArrayOutputStream baos) throws IOException {
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

  private static byte[] readMultiLine(final InputStream inputStream, final byte[] responseCode) throws IOException {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    baos.write(responseCode);
    return readMultiLine(inputStream, baos);
  }

  private static byte[] readMultiLine(final InputStream is, final ByteArrayOutputStream baos) throws IOException {
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
