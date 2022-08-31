## The Commons-IO Module

Common code for issue - io. Different from `commons` as it has a `gson` dependency for streaming json parsing and writing.

This is different from simple gson databinding in its use of streaming, and therefore suitable for processing the large amounts of data that are sometimes collected
during instrumentation, where databinding requires large amounts of memory that may not be available. 