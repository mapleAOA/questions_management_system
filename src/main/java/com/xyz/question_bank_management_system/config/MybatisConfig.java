package com.xyz.question_bank_management_system.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.xyz.question_bank_management_system.mapper")
public class MybatisConfig {
}
