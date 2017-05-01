package com.fearnoeval.nntp;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import java.io.EOFException;
import java.io.IOException;
import java.lang.NumberFormatException;

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

  public static final Set<Integer> defaultMultiLineResponseCodes;
  static {
    final Set<Integer> s = new HashSet<>(10, 1.0f);
    s.add(100);
    s.add(101);
    s.add(215);
    s.add(220);
    s.add(221);
    s.add(222);
    s.add(224);
    s.add(225);
    s.add(230);
    s.add(231);
    defaultMultiLineResponseCodes = Collections.unmodifiableSet(s);
  }

  private static byte[] writeAndRead(final OutputStream outputStream, final InputStream inputStream, final byte[] dataToWrite, final Set<Integer> multiLineResponseCodes) throws IOException {
    outputStream.write(dataToWrite);
    outputStream.flush();

    final byte[] statusCodeBytes = new byte[3];
    final int    bytesRead       = inputStream.read(statusCodeBytes);

    if (bytesRead == 3) {
      try {
        final int statusCodeInt = Integer.parseInt(new String(statusCodeBytes, StandardCharsets.UTF_8));

        if (statusCodeInt == 211) {
          return read211(inputStream, statusCodeBytes, dataToWrite);
        }
        if (multiLineResponseCodes.contains(statusCodeInt)) {
          return readMultiLine(inputStream, statusCodeBytes);
        }
        return readSingleLine(inputStream, statusCodeBytes);
      }
      catch (NumberFormatException e) { throw new RuntimeException("Invalid response", e); }
    }

    throw new EOFException();
  }

  private static final String listgroup = "LISTGROUP";

  private static boolean isListgroup(final byte[] maybeCommand) {
    return (maybeCommand.length >= listgroup.length()) && new String(maybeCommand, 0, listgroup.length(), StandardCharsets.UTF_8).toUpperCase().equals(listgroup);
  }

  private static byte[] read211(final InputStream inputStream, final byte[] statusCode, final byte[] maybeCommand) throws IOException {
    return isListgroup(maybeCommand) ? readMultiLine(inputStream, statusCode) : readSingleLine(inputStream, statusCode);
  }

  private static byte[] readSingleLine(final InputStream inputStream, final byte[] statusCode) throws IOException {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    baos.write(statusCode);
    return readSingleLine(inputStream, baos);
  }

  private static byte[] readMultiLine(final InputStream inputStream, final byte[] statusCode) throws IOException {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    baos.write(statusCode);
    return readMultiLine(inputStream, baos);
  }

  private static final int cr  = 13;
  private static final int lf  = 10;
  private static final int dot = 46;

  private static final int[] singleLineEnding = new int[] {cr, lf};
  private static final int[] multiLineEnding  = new int[] {cr, lf, dot, cr, lf};

  private static byte[] readSingleLine(final InputStream is, final ByteArrayOutputStream baos) throws IOException {
    return readUntil(is, baos, singleLineEnding);
  }

  private static byte[] readMultiLine(final InputStream is, final ByteArrayOutputStream baos) throws IOException {
    return readUntil(is, baos, multiLineEnding);
  }

  private static byte[] readUntil(final InputStream is, final ByteArrayOutputStream baos, final int[] a) throws IOException {
    for (int i = 0, b; i < a.length; i = (b == a[i]) ? i + 1 : 0) {
      b = is.read();
      if (b == -1) { throw new EOFException(); }
      baos.write(b);
    }
    return baos.toByteArray();
  }
}
