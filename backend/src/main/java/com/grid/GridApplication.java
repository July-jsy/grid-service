package com.grid;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.grid.mapper")
public class GridApplication {
    public static void main(String[] args) {
        SpringApplication.run(GridApplication.class, args);
    }
}
