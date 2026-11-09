-- KEYS: daily ledger, in-flight lease, cooldown. All keys share the user hash tag.
-- ARGV: owner, lease seconds, cooldown seconds, day TTL, call limit, token limit,
--       reserved tokens, persisted successful calls, persisted successful tokens.
if redis.call('EXISTS', KEYS[2]) == 1 then return 1 end
if redis.call('EXISTS', KEYS[3]) == 1 then return 2 end
local calls = math.max(tonumber(redis.call('HGET', KEYS[1], 'calls') or '0'), tonumber(ARGV[8]))
local tokens = math.max(tonumber(redis.call('HGET', KEYS[1], 'tokens') or '0'), tonumber(ARGV[9]))
if calls >= tonumber(ARGV[5]) then return 3 end
if tokens + tonumber(ARGV[7]) > tonumber(ARGV[6]) then return 4 end
redis.call('HSET', KEYS[1], 'calls', calls + 1, 'tokens', tokens + tonumber(ARGV[7]))
redis.call('EXPIRE', KEYS[1], ARGV[4])
redis.call('SET', KEYS[2], ARGV[1], 'EX', ARGV[2])
redis.call('SET', KEYS[3], ARGV[1], 'EX', ARGV[3])
return 0
