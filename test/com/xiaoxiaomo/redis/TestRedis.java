package com.xiaoxiaomo.redis;

import com.xiaoxiaomo.redis.util.JedisUtil;
import org.junit.Test;
import redis.clients.jedis.*;
import sun.plugin2.gluegen.runtime.CPU;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by xiaoxiaomo on 2016/4/27.
 */
public class TestRedis {

    /**
     * 不使用管道初始化1W条数据
     * 耗时：3079毫秒
     * @throws Exception
     */
    @Test
    public void NOTUsePipeline() throws Exception {
        Jedis jedis = JedisUtil.getJedis();
        long start_time = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            jedis.set("aa_"+i, i+"");
        }
        System.out.println(System.currentTimeMillis()-start_time);
    }

    /**
     * 使用管道初始化1W条数据
     * 耗时：255毫秒
     * @throws Exception
     */
    @Test
    public void usePipeline() throws Exception {
        Jedis jedis = JedisUtil.getJedis();

        long start_time = System.currentTimeMillis();
        Pipeline pipelined = jedis.pipelined();
        for (int i = 0; i < 10000; i++) {
            pipelined.set("cc_"+i, i+"");
        }
        pipelined.sync();//执行管道中的命令
        System.out.println(System.currentTimeMillis()-start_time);
    }

    @Test
    public void TestRedis(){
        Jedis jedis = new Jedis( "192.168.3.56" , 6379 ) ;
        jedis.set("user:info:xxo","23");
        System.out.println(jedis.get("user:info:xxo"));
    }


    @Test
    public void TestRedisPool(){

        //1. 连接池配置
        JedisPoolConfig config = new JedisPoolConfig() ;
        config.setMaxIdle(50);         //最大空闲连接
        config.setMaxTotal(100);       //最大连接数
        config.setMaxWaitMillis(2000); //最长连接时间
        config.setTestOnBorrow(true);  //校验

        //2. 通过连接池获取Jedis
        JedisPool pool = new JedisPool(config, "192.168.3.56", 6379);
        Jedis jedis = pool.getResource(); //获取Jedis

        //3. 通过Jedis操作数据
        jedis.set("user:info:xxo:age", String.valueOf(23)) ;
        System.out.println(jedis.get("user:info:xxo:age"));

    }



    @Test
    public void TestMyJedisPool(){
        Jedis jedis = JedisUtil.getJedis() ;
        jedis.set("aaa" , "bbbbb");

        JedisUtil.returnRource(jedis);
    }


    @Test
    public void userVisit(){
        for (int i = 0; i < 15; i++) {
            System.out.println(checkIP("192.168.3.59"));
        }
    }

    private boolean checkIP(String ip) {

        //1. 创建一个对象
        Jedis jedis = new Jedis("192.168.3.56", 6379) ;

        //2. 查看ip,如果不存在
        String key = jedis.get(ip);
        if( key == null ){
            jedis.set( ip , "1" );
            jedis.expire( ip , 5 ) ; //有效时间
            //jedis.expireAt()
        }

        //3.是否在5s之类超过限制，这里限制为10次
        else{
            int count = Integer.valueOf( key ) ;
            if( count >= 10 ){
                return false;
            }
            else{
                //jedis.set( ip , (++count)+"" ) ;
                jedis.incr(ip) ;
                jedis.incrBy(ip,1);
            }
        }
        return true ;

    }


    /**
     * 使用sentinel操作主从架构中的数据
     * @throws Exception
     */
    @Test
    public void test7() throws Exception {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        //TODO--
        String masterName = "mymaster";
        Set<String> sentinels = new HashSet<String>();;
        sentinels.add("192.168.33.88:26379");
        sentinels.add("192.168.33.89:26379");
        JedisSentinelPool jsp = new JedisSentinelPool(masterName , sentinels, poolConfig );

        HostAndPort currentHostMaster = jsp.getCurrentHostMaster();
        System.out.println(currentHostMaster.getHost()+"--"+currentHostMaster.getPort());

        Jedis jedis = jsp.getResource();//获取Jedis
        String value = jedis.set("xiaoxiao","momo");
        System.out.println(value);
        jedis.close();
    }


    /**
     * 集群操作
     * @throws Exception
     */
    @Test
    public void cluster() throws Exception {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        //TODO--
        Set<HostAndPort> nodes = new HashSet<HostAndPort>();
        nodes.add(new HostAndPort("192.168.33.88", 7000));
        nodes.add(new HostAndPort("192.168.33.88", 7001));
        nodes.add(new HostAndPort("192.168.33.88", 7002));
        nodes.add(new HostAndPort("192.168.33.88", 7003));
        nodes.add(new HostAndPort("192.168.33.88", 7004));
        nodes.add(new HostAndPort("192.168.33.88", 7005));

        JedisCluster jedisCluster = new JedisCluster(nodes , poolConfig );
		/*String string = jedisCluster.get("a");
		System.out.println(string);*/
        jedisCluster.set("blog", "blog.xiaoxiaomo.com");
        System.out.println( jedisCluster.get("blog") );
        //这个close表示把jedisCluster给关闭了
        //jedisCluster.close();
    }
}
