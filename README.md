# NNTPClient

A small [Network News Transfer Protocol][rfc3977] client library for Java

## Public API

- Coming soon

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

- Coming soon

## Caveat emptor

This is a tl;dr. For more details, see [CAVEATS.md](CAVEATS.md)

- Because of the ambiguity of response code 211, beware of sending LISTGROUP
commands with `writeAndRead`

## License

- © 2017 [Tim Walter](https://www.fearnoeval.com/)
- Licensed under the [Apache License, Version 2.0](LICENSE)

[rfc977]: https://tools.ietf.org/html/rfc977
[rfc3977]: https://tools.ietf.org/html/rfc3977
