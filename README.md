# NNTPClient

A small [Network News Transfer Protocol][rfc3977] client library for Java

## Public API

- `static byte[] readSingleLine(final InputStream inputStream) throws IOException`
- `static byte[] readMultiLine(final InputStream inputStream) throws IOException`
- `static byte[] writeAndRead(final OutputStream outputStream, final InputStream inputStream, final byte[] dataToWrite) throws IOException`
- `static final Set<Integer> defaultMultiLineResponseCodes`

## Installation

- Coming soon

## Example usage

    import com.fearnoeval.nntp.NNTPClient;
    import java.io.IOException;
    import java.io.InputStream;
    import java.io.OutputStream;
    import java.net.Socket;
    import java.nio.charset.Charset;
    import java.nio.charset.StandardCharsets;
    import javax.net.ssl.SSLSocket;
    import javax.net.ssl.SSLSocketFactory;

    public class NaiveNNTPClientDemo {

      private static final Charset utf8 = StandardCharsets.UTF_8;

      public static void main(String[] args) throws IOException {
        final String host = args[0];
        final int    port = Integer.parseInt(args[1]);

        final Socket       socket       = SSLSocketFactory.getDefault().createSocket(host, port);
        final InputStream  inputStream  = socket.getInputStream();
        final OutputStream outputStream = socket.getOutputStream();

        final byte[] greeting   = NNTPClient.readSingleLine(inputStream);
        final byte[] modeReader = NNTPClient.writeAndRead(outputStream, inputStream, "MODE READER\r\n".getBytes(utf8));

        outputStream.write("CAPABILITIES\r\n".getBytes(utf8));
        final byte[] help = NNTPClient.readMultiLine(inputStream);

        System.out.println(new String(greeting,   utf8));
        System.out.println(new String(modeReader, utf8));
        System.out.println(new String(help,       utf8));
        socket.close();
      }
    }

Example output of `java NaiveNNTPClientDemo news.giganews.com 443` (Note: this
is not an endoresement):

    200 News.GigaNews.Com
    200 reading enabled
    101 capability list
    VERSION 2
    READER
    AUTHINFO USER TRANSIENT-SSL
    205 goodbye

## FAQ

- Is this stable?
  - It's currently a work-in-progress
  - Once it gets some use to shake out any bugs, a proper release will be made
  - Once properly released, there will be no breaking changes
- Why is it so small?
  - I was trying to make a prettier/higher-level API, but ran into many
  subjective choices
  - Instead of making those choices and forcing them on users, I stepped back
  and decided that in this instance, exposing a lower-level API provided the
  best tradeoff in terms of usability and flexibility
- Why not use `org.apache.commons.net.nntp`?
  - There's no mention of bytes in their `NNTP` or `NNTPClient` classes, which
  is required for many use cases
  - There's only mention of [RFC 977 (February 1986)][rfc977] in the source,
  whereas I wanted support for [RFC 3977 (October 2006)][rfc3977]
  - It was easier to write a ~200-line library that did the essentials than try
  to understand and update their ~5000-line NNTP package

## Caveat emptor

This is a tl;dr. For more details, see [CAVEATS.md](CAVEATS.md)

- Because of the ambiguity of response code 211, beware of sending LISTGROUP
commands with `writeAndRead`

## License

- © 2017 [Tim Walter](https://www.fearnoeval.com/)
- Licensed under the [Apache License, Version 2.0](LICENSE)

[rfc977]: https://tools.ietf.org/html/rfc977
[rfc3977]: https://tools.ietf.org/html/rfc3977
