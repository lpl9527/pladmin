package com.lpl.utils;

import com.lpl.PlAdminApp;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)    //设置启动器
@SpringBootTest(classes = PlAdminApp.class) //指定启动类
public class RedisUtilsTest {

    @Autowired
    private RedisUtils redisUtils;

    @Test
    public void setTest(){
        redisUtils.set("key1", "222222");
    }
    @Test
    public void test() {
        System.out.println("测试中执行！");
    }

    @Before
    public void beforeTest() {
        System.out.println("测试之前执行！");
    }
    @After
    public void afterTest() {
        System.out.println("测试之后执行！");
    }
}
