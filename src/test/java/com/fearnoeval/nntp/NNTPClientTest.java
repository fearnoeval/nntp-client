package com.fearnoeval.nntp;

import org.junit.Test;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.CoreMatchers.instanceOf;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import java.io.EOFException;
import java.io.IOException;

public class NNTPClientTest {

  // readSingleLine tests

  @Test
  public void readSingleLine() {

    final String reply    = "111 19990623135624\r\n";
    final String expected = "111 19990623135624";

    try {
      assertThat(readSingleLineStringToString(reply), is(expected));
    }
    catch (Exception e) {
      fail(shouldNotHaveThrown);
    }
  }

  @Test
  public void readSingleLineEOFException() {

    final String reply = "";

    try {
      readSingleLineStringToString(reply);
      fail(shouldHaveThrown);
    }
    catch (Exception e) {
      assertThat(e, instanceOf(EOFException.class));
    }
  }

  // readMultiLine tests

  @Test
  public void readMultiLine() {

    final String reply    = "100 Help text follows\r\n"
                          + "This is some help text.  There is no specific\r\n"
                          + "formatting requirement for this test, though\r\n"
                          + "it is customary for it to list the valid commands\r\n"
                          + "and give a brief definition of what they do.\r\n"
                          + ".\r\n";
    final String expected = "100 Help text follows\r\n"
                          + "This is some help text.  There is no specific\r\n"
                          + "formatting requirement for this test, though\r\n"
                          + "it is customary for it to list the valid commands\r\n"
                          + "and give a brief definition of what they do.";

    try {
      assertThat(readMultiLineStringToString(reply), is(expected));
    }
    catch (Exception e) {
      fail(shouldNotHaveThrown);
    }
  }

  @Test
  public void readMultiLineEOFException() {

    final String reply = "";

    try {
      readMultiLineStringToString(reply);
      fail(shouldHaveThrown);
    }
    catch (Exception e) {
      assertThat(e, instanceOf(EOFException.class));
    }
  }

  // writeAndRead tests

  @Test
  public void writeAndReadSingleLine() {

    final String command  = "DATE\r\n";
    final String reply    = "111 19990623135624\r\n";
    final String expected = "111 19990623135624";

    try {
      assertThat(writeAndReadStringToString(command, reply), is(expected));
    }
    catch (Exception e) {
      fail(shouldNotHaveThrown);
    }
  }

  @Test
  public void writeAndReadMultiLine() {

    final String command  = "HELP\r\n";
    final String reply    = "100 Help text follows\r\n"
                          + "This is some help text.  There is no specific\r\n"
                          + "formatting requirement for this test, though\r\n"
                          + "it is customary for it to list the valid commands\r\n"
                          + "and give a brief definition of what they do.\r\n"
                          + ".\r\n";
    final String expected = "100 Help text follows\r\n"
                          + "This is some help text.  There is no specific\r\n"
                          + "formatting requirement for this test, though\r\n"
                          + "it is customary for it to list the valid commands\r\n"
                          + "and give a brief definition of what they do.";

    try {
      assertThat(writeAndReadStringToString(command, reply), is(expected));
    }
    catch (Exception e) {
      fail(shouldNotHaveThrown);
    }
  }

  @Test
  public void writeAndReadSingleLine211() {

    final String command  = "GROUP misc.test\r\n";
    final String reply    = "211 1234 3000234 3002322 misc.test\r\n";
    final String expected = "211 1234 3000234 3002322 misc.test";

    try {
      assertThat(writeAndReadStringToString(command, reply), is(expected));
    }
    catch (Exception e) {
      fail(shouldNotHaveThrown);
    }
  }

  @Test
  public void writeAndReadMultiLine211() {

    final String command  = "LISTGROUP misc.test\r\n";
    final String reply    = "211 2000 3000234 3002322 misc.test list follows\r\n"
                          + "3000234\r\n"
                          + "3000237\r\n"
                          + "3000238\r\n"
                          + "3000239\r\n"
                          + "3002322\r\n"
                          + ".\r\n";
    final String expected = "211 2000 3000234 3002322 misc.test list follows\r\n"
                          + "3000234\r\n"
                          + "3000237\r\n"
                          + "3000238\r\n"
                          + "3000239\r\n"
                          + "3002322";

    try {
      assertThat(writeAndReadStringToString(command, reply), is(expected));
    }
    catch (Exception e) {
      fail(shouldNotHaveThrown);
    }
  }

  @Test
  public void writeAndReadMultiLineEOFException() {

    final String command = "";
    final String reply   = "";

    try {
      writeAndReadStringToString(command, reply);
      fail(shouldHaveThrown);
    }
    catch (Exception e) {
      assertThat(e, instanceOf(EOFException.class));
    }
  }

  // Helpers

  private static final String       shouldHaveThrown    = "Exception should have been thrown";
  private static final String       shouldNotHaveThrown = "Exception shouldn't have been thrown";
  private static final OutputStream voidOutputStream    = new VoidOutputStream();
  private static final Charset      utf8                = StandardCharsets.UTF_8;

  private static String readSingleLineStringToString(final String reply) throws IOException {
    return new String(NNTPClient.readSingleLine(new ByteArrayInputStream(reply.getBytes(utf8))), utf8);
  }
  private static String readMultiLineStringToString(final String reply) throws IOException {
    return new String(NNTPClient.readMultiLine(new ByteArrayInputStream(reply.getBytes(utf8))), utf8);
  }
  private static String writeAndReadStringToString(final String command, final String reply) throws IOException {
    return new String(NNTPClient.writeAndRead(voidOutputStream, new ByteArrayInputStream(reply.getBytes(utf8)), command.getBytes(utf8)), utf8);
  }

  private static class VoidOutputStream extends OutputStream {
    public VoidOutputStream() {}
    public void close() {}
    public void flush() {}
    public void write(byte[] b) {}
    public void write(byte[] b, int off, int len) {}
    public void write(int b) {}
  }
}
