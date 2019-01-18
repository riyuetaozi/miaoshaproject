package com.miaoshaproject;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


//@SpringBootApplication 作用就是将App类被Spring托管并且此类是主启动类
//@MapperScan 作用就是将dao进行注入到@MapperScan中
@SpringBootApplication
@MapperScan("com.miaoshaproject.dao")
public class App {

    public static void main(String[] args){
        SpringApplication.run(App.class, args);
    }

}
