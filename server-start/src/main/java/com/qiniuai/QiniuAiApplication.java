package com.qiniuai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author jiping-zeng
 * @since 2025/9/27 1:34
 */
@EnableAsync
@EnableCaching
@SpringBootApplication
@EnableScheduling
public class QiniuAiApplication {
  public static void main(String[] args) {
    SpringApplication.run(QiniuAiApplication.class, args);
  }

}
