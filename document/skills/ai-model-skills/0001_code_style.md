# Log

Log is part of code.

Log should be printed on beginning and end of each method.

## Condition Branch

Must print log on every condition branches to indicate the code route. \
The log MUST contain the variable that participants the branch decision.

# Pools

Should create pool for any reusable resources. Including:

1. http client
2. db connection pool
3. threads (now in java virtual thread is preferred)

Any pool framework, should be able to set up and monitor (or equivalent):

1. max size
2. idle expire
3. max idle
4. min size
5. queue max capacity
6. current queue size

# Json

for a long value not having business meaning, use `@JsonFormat(shape = JsonFormat.Shape.STRING)` to avoid javascript number overflow.



