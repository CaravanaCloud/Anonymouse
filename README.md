# Anonymouse

Prevent data leaks from test environments by erasing all Personally Identifiable Information, but still retaining relationships and semantics for bug reproduction and forensics.

# Current Status

Initial proof of concept.
Uses JDBC Metadata API to iterate on the database (full scan) and replace all values classified as PII.
Help more than welcome!





