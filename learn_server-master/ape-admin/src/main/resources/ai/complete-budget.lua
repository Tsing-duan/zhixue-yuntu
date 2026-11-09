-- A lost/expired lease never releases a newer request or refunds an unknown charge.
if redis.call('GET', KEYS[2]) ~= ARGV[1] then return 0 end
if redis.call('EXISTS', KEYS[1]) == 1 then
  if ARGV[4] == 'not-configured' then
    redis.call('HINCRBY', KEYS[1], 'calls', -1)
    redis.call('HINCRBY', KEYS[1], 'tokens', -tonumber(ARGV[2]))
  elseif ARGV[4] == 'success' then
    redis.call('HINCRBY', KEYS[1], 'tokens', tonumber(ARGV[3]) - tonumber(ARGV[2]))
  end
end
redis.call('DEL', KEYS[2])
return 1
