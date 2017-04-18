# NNTPClient

A small [Network News Transfer Protocol][rfc3977] client library for Java

## Public API

- `public static byte[] readMultiLine(final Socket socket) throws IOException`
  - Returns a multi-line response from the socket as a byte array
- `public static byte[] readSingleLine(final Socket socket) throws IOException`
  - Returns a single-line response from the socket as a byte array
- `public static void write(final Socket socket, byte[] command) throws IOException`
  - Writes a command to a socket
- `public static byte[] writeAndRead(final Socket socket, final byte[] command) throws IOException`
  - Returns a single- or multi-line response from the socket as a byte array
  based on the response code received from the socket

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

## Example usage

Naive example:

    import com.fearnoeval.nntp.NNTPClient;
    import java.io.IOException;
    import java.net.Socket;
    import java.nio.charset.StandardCharsets;
    import javax.net.ssl.SSLSocket;
    import javax.net.ssl.SSLSocketFactory;

    public class NaiveNNTPClientDemo {

      private static byte[] stob(final String s) {
        return s.getBytes(StandardCharsets.UTF_8);
      }

      private static String btos(final byte[] bs) {
        return new String(bs, StandardCharsets.UTF_8);
      }

      public static void main(String[] args) {
        final String host = args[0];
        final int    port = Integer.parseInt(args[1]);

        try {
          final Socket s = SSLSocketFactory.getDefault().createSocket(host, port);

          final byte[] greeting        = NNTPClient.readSingleLine(s);
          final byte[] capabilitiesRes = NNTPClient.writeAndRead(s, stob("CAPABILITIES\r\n"));
          final byte[] helpRes         = NNTPClient.writeAndRead(s, stob("HELP\r\n"));

          System.out.print(btos(greeting));
          System.out.print(btos(capabilitiesRes));
          System.out.print(btos(helpRes));
          System.out.print(btos(NNTPClient.writeAndRead(s, stob("QUIT\r\n"))));

          s.close();
        }
        catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

Example output of `java NaiveNNTPClientDemo news.giganews.com 443` (Note: this
is not an endoresement):

    200 News.GigaNews.Com
    101 capability list
    VERSION 2
    READER
    AUTHINFO USER TRANSIENT-SSL
    .
    480 authentication required
    205 goodbye

## License

- © 2017 [Tim Walter](https://www.fearnoeval.com/)
- Licensed under the [Apache License, Version 2.0](LICENSE)

[rfc977]: https://tools.ietf.org/html/rfc977
[rfc3977]: https://tools.ietf.org/html/rfc3977
