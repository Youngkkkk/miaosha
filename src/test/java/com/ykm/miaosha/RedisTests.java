package com.ykm.miaosha;

import com.ykm.miaosha.dataobject.UserDO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashSet;
import java.util.Set;

/**
 * @Author ykm
 * @Date 2019/6/14 10:50
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class RedisTests {

    @Autowired
    private RedisTemplate redisTemplate;

    public static final String SCORE_RANK = "score_rank";

    @Test
    public void redisTest() {
        // redis存储数据
        String key = "name";
        redisTemplate.opsForValue().set(key, "yukong");
        // 获取数据
        String value = (String) redisTemplate.opsForValue().get(key);
        System.out.println("获取缓存中key为" + key + "的值为：" + value);

        UserDO userDO = new UserDO();
        String userKey = "yukong";
        redisTemplate.opsForValue().set(userKey, userDO);
        UserDO newFans = (UserDO) redisTemplate.opsForValue().get(userKey);
        System.out.println("获取缓存中key为" + userKey + "的值为：" + newFans);

    }

    /**
     * 批量新增
     * zset
     */
    @Test
    public void batchAdd() {
        Set<ZSetOperations.TypedTuple<String>> tuples = new HashSet<>();
        long start = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            DefaultTypedTuple<String> tuple = new DefaultTypedTuple<>("张三" + i, 1D + i);
            tuples.add(tuple);
        }
        System.err.println("循环时间：" + (System.currentTimeMillis() - start));
        Long num = redisTemplate.opsForZSet().add(SCORE_RANK, tuples);
        System.out.println("批量新增时间:" + (System.currentTimeMillis() - start));
        System.out.println("受影响行数：" + num);
    }

    /**
     * 获取排行列表
     */
    @Test
    public void list() {
        Set<String> range = redisTemplate.opsForZSet().reverseRange(SCORE_RANK, 0, 10);
        System.out.println("前十名：");
        range.forEach(System.out::println);
        Set<ZSetOperations.TypedTuple<String>> sets = redisTemplate.opsForZSet().reverseRangeWithScores(SCORE_RANK, 0, 10);
        sets.forEach((k) -> System.err.println(k.getScore() + ":" + k.getValue()));
    }

    /**
     * 单个新增
     */
    @Test
    public void add() {
        redisTemplate.opsForZSet().add(SCORE_RANK, "李四", 8889);
    }

    /**
     * 获取单人的排行
     */
    @Test
    public void find() {
        Long rankNum = redisTemplate.opsForZSet().reverseRank(SCORE_RANK, "李四");
        System.err.println("李四的排行：" + rankNum);
        Double score = redisTemplate.opsForZSet().score(SCORE_RANK, "李四");
        System.err.println("李四的分数：" + score);
    }

    /**
     * 统计两个分数之间的人数
     */
    @Test
    public void count() {
        Long count = redisTemplate.opsForZSet().count(SCORE_RANK, 8001, 9000);
        System.out.println("统计8001-9000之间的人数:" + count);
    }

    /**
     * 获取整个集合的基数(数量大小)
     */
    @Test
    public void zCard() {
        Long aLong = redisTemplate.opsForZSet().zCard(SCORE_RANK);
        System.out.println("集合的基数为：" + aLong);
    }

    /**
     * 使用加法操作分数
     */
    @Test
    public void incrementScore() {
        Double score = redisTemplate.opsForZSet().incrementScore(SCORE_RANK, "李四", 1000);
        System.out.println("李四分数+1000后：" + score);
    }


    /* example: https://mp.weixin.qq.com/s/tLajYRchaOgocA-H1ozgyg
    //单个新增or更新
    Boolean add(K key, V value, double score);
    //批量新增or更新
    Long add(K key, Set<TypedTuple<V>> tuples);
    //使用加法操作分数
    Double incrementScore(K key, V value, double delta);

    //通过key/value删除
    Long remove(K key, Object... values);

    //通过排名区间删除
    Long removeRange(K key, long start, long end);

    //通过分数区间删除
    Long removeRangeByScore(K key, double min, double max);

    //通过排名区间获取列表值集合
    Set<V> range(K key, long start, long end);

    //通过排名区间获取列表值和分数集合
    Set<TypedTuple<V>> rangeWithScores(K key, long start, long end);

    //通过分数区间获取列表值集合
    Set<V> rangeByScore(K key, double min, double max);

    //通过分数区间获取列表值和分数集合
    Set<TypedTuple<V>> rangeByScoreWithScores(K key, double min, double max);

    //通过Range对象删选再获取集合排行
    Set<V> rangeByLex(K key, Range range);

    //通过Range对象删选再获取limit数量的集合排行
    Set<V> rangeByLex(K key, Range range, Limit limit);

    //获取个人排行
    Long rank(K key, Object o);

    //获取个人分数
    Double score(K key, Object o);

    //统计分数区间的人数
    Long count(K key, double min, double max);

    //统计集合基数
    Long zCard(K key);
    */
}