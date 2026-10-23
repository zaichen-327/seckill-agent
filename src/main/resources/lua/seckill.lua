local stock = redis.call('get', KEYS[1])
if stock == false then
    return -1
end
if tonumber(stock) <= 0 then
    return -2
end
return redis.call('decr', KEYS[1])