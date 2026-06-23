package com.zzyl;

import org.jetbrains.annotations.TestOnly;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * @Author: ziyi
 * @Date: 2026/5/21 20:33
 * @Version: v1.0.0
 * @Description: TODO
 **/
@SpringBootTest
public class RedisTest {
    @Autowired
    RedisTemplate<String,String> redisTemplate;
    @Test
    public void stringTest(){
        redisTemplate.opsForValue().set("name","张三");
        System.out.println(redisTemplate.opsForValue().get("name"));
        //setnx
        redisTemplate.opsForValue().setIfAbsent("name1","hxm",20, TimeUnit.SECONDS);
        //setex
        redisTemplate.opsForValue().set("token","123",10,TimeUnit.SECONDS);
        redisTemplate.delete("name");

    }
    @Test
    public void listTest(){
        redisTemplate.opsForList().leftPush("list","2");
        redisTemplate.opsForList().leftPushAll("list","2","32","32");
        System.out.println(redisTemplate.opsForList().range("list", 0, -1));
        System.out.println(redisTemplate.opsForList().leftPop("list"));
        System.out.println(redisTemplate.opsForList().size("list"));

    }
    @Test
    public void hashTest(){
        //类似hashmap
        //hput
        redisTemplate.opsForHash().put("hash","name","ziyi");
        redisTemplate.opsForHash().put("hash","age","18");
        //hget
        System.out.println(redisTemplate.opsForHash().get("hash", "name"));
        //hvals
        System.out.println(redisTemplate.opsForHash().values("hash"));
        //hkeys
        System.out.println(redisTemplate.opsForHash().keys("hash"));
        //hdel
        redisTemplate.opsForHash().delete("hash","age");
    }
    @Test
    public void setTest(){
        //类似linkedlist
        //sadd
        redisTemplate.opsForSet().add("set1","1","2","3");
        redisTemplate.opsForSet().add("set2","5","4","3");
        //smembers
        System.out.println(redisTemplate.opsForSet().members("set1"));
        //sunion取并集
        redisTemplate.opsForSet().union("set1","set2");
        //sinter 取交集
        redisTemplate.opsForSet().intersect("set1","set2");
        //scard
        redisTemplate.opsForSet().size("set1");
    }
    @Test
    public void zsetTest(){
        //zadd
        redisTemplate.opsForZSet().add("zset","hxm",100);
        redisTemplate.opsForZSet().add("zset","ziyi",99);
        //zrang
        redisTemplate.opsForZSet().range("zset",0,-1);
        //zrem
        redisTemplate.opsForZSet().remove("zset","ziyi");
        //zincrby

        redisTemplate.opsForZSet().incrementScore("zset","hxm",100);

    }
    @Test
    public void commonTest(){
        //del
        redisTemplate.delete("zset");
        //keys*
        redisTemplate.keys("*");
        //exists
        redisTemplate.hasKey("name");
        //type
        redisTemplate.type("zset");
    }
}
