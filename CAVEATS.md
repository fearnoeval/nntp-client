# Caveats

## Response code 211

- Response code 211 is ambiguous \[[1][rfc3977-section-3.2]\]\[[2][rfc3977-appendix-c]\]
  - A 211 response after a GROUP command is a single-line response
  - A 211 response after a LISTGROUP command is a mutli-line response
- If `writeAndRead` receives a 211 response, it looks at the data that it was
passed to write and checks if it starts with LISTGROUP (case insensitive)
  - If true, it will return a multi-line response
  - Otherwise, it will return a single-line response
- If you *really* must split it, understand that the rest of the rest of the
response is waiting to be read, and that you should call `readMultiLine` to get
the rest of the response

[rfc3977-section-3.2]: https://tools.ietf.org/html/rfc3977#section-3.2
[rfc3977-appendix-c]: https://tools.ietf.org/html/rfc3977#appendix-C
