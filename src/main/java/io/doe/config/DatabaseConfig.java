package io.doe.config;

import io.doe.common.Constants;
import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see org.springframework.boot.autoconfigure
 * started on 2017-05-?? ~
 */

@Configuration
@MapperScan(basePackages=Constants.BASE_PACKAGE + ".persistence.mapper", annotationClass=Mapper.class)
public class DatabaseConfig { /* Spring Configuration Class */ }
