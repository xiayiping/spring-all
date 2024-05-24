--keys: Arrays.asList(getRawName(), threadsQueueName, timeoutSetName),
--args: unit.toMillis(leaseTime), getLockName(threadId), wait, currentTime
--threadsQueueName = prefixName("redisson_lock_queue", name); redisson_lock_queue:{xypLock1}
--timeoutSetName = prefixName("redisson_lock_timeout", name); redisson_lock_timeout:{xypLock1}
while true do
    local firstThreadId2 = redis.call('lindex', KEYS[2], 0);  -- LINDEX key index | Returns the element at index index in the list stored at key.
    if firstThreadId2 == false then
        break ;
    end ;

    local timeout = tonumber(redis.call('zscore', KEYS[3], firstThreadId2)); -- ZSCORE key member | Returns the score of member in the sorted set at key.
    if timeout <= tonumber(ARGV[4]) then
        -- remove the item from the queue and timeout set
        -- NOTE we do not alter any other timeout
        redis.call('zrem', KEYS[3], firstThreadId2);
        redis.call('lpop', KEYS[2]);
    else
        break ;
    end ;
end ;

-- check if the lock can be acquired now
if (redis.call('exists', KEYS[1]) == 0)
        and ((redis.call('exists', KEYS[2]) == 0)
        or (redis.call('lindex', KEYS[2], 0) == ARGV[2])) then

    -- remove this thread from the queue and timeout set
    redis.call('lpop', KEYS[2]);
    redis.call('zrem', KEYS[3], ARGV[2]);

    -- decrease timeouts for all waiting in the queue
    local keys = redis.call('zrange', KEYS[3], 0, -1);
    for i = 1, #keys, 1 do
        redis.call('zincrby', KEYS[3], -tonumber(ARGV[3]), keys[i]);
    end ;

    -- acquire the lock and set the TTL for the lease
    redis.call('hset', KEYS[1], ARGV[2], 1); -- HSET key field value [field value ...] | Sets the specified fields to their respective values in the hash stored at key.
    redis.call('pexpire', KEYS[1], ARGV[1]);
    return nil;
end ;

-- check if the lock is already held, and this is a re-entry
if redis.call('hexists', KEYS[1], ARGV[2]) == 1 then
    redis.call('hincrby', KEYS[1], ARGV[2], 1);
    redis.call('pexpire', KEYS[1], ARGV[1]);
    return nil;
end ;

-- the lock cannot be acquired
-- check if the thread is already in the queue
local timeout = redis.call('zscore', KEYS[3], ARGV[2]);
if timeout ~= false then
    -- the real timeout is the timeout of the prior thread
    -- in the queue, but this is approximately correct, and
    -- avoids having to traverse the queue
    return timeout - tonumber(ARGV[3]) - tonumber(ARGV[4]);
end ;

-- add the thread to the queue at the end, and set its timeout in the timeout set to the timeout of
-- the prior thread in the queue (or the timeout of the lock if the queue is empty) plus the
-- threadWaitTime
local lastThreadId = redis.call('lindex', KEYS[2], -1);
local ttl;
if lastThreadId ~= false and lastThreadId ~= ARGV[2] then
    ttl = tonumber(redis.call('zscore', KEYS[3], lastThreadId)) - tonumber(ARGV[4]);
else
    ttl = redis.call('pttl', KEYS[1]);
end ;
local timeout = ttl + tonumber(ARGV[3]) + tonumber(ARGV[4]);
if redis.call('zadd', KEYS[3], timeout, ARGV[2]) == 1 then
    redis.call('rpush', KEYS[2], ARGV[2]); -- RPUSH key element [element ...] | Insert all the specified values at the tail of the list stored at key.
end ;
return ttl;