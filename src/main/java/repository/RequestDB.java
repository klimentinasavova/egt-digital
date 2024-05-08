package repository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

@Component
public class RequestDB {
    @Value("${spring.redis.host:localhost}")
    private String host;

    @Value("${spring.redis.port:6379}")
    private int port;

    protected Jedis jedis;

    public RequestDB() {
        jedis = new Jedis(host, port);
    }

    public boolean addRequestId(String requestId) {
        long status = jedis.setnx(requestId, "");
        //if (status == 1) -> RequestId does not exist. Added to Redis.
        //if (status != 1) -> RequestId already exists in Redis.
        return  (status == 1);
    }

    public void close() {
        jedis.close();
    }

}
