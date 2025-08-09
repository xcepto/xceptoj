package com.example.warehouse.stocktake;

import com.example.warehouse.common.RabbitMqRetryConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.resilience4j.retry.Retry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootApplication
public class StocktakeApplication {

  public static void main(String[] args) {
    SpringApplication.run(StocktakeApplication.class, args);
  }

  @Autowired
  private Environment env;

  @Bean
  public DataSource dataSource() throws Exception {
    HikariDataSource dataSource = new HikariDataSource();
    dataSource.setJdbcUrl(env.getProperty("spring.datasource.url"));
    dataSource.setUsername(env.getProperty("spring.datasource.username"));
    dataSource.setPassword(env.getProperty("spring.datasource.password"));
    dataSource.setDriverClassName(env.getProperty("spring.datasource.driver-class-name"));


    Retry retry = Retry.of("postgres", RabbitMqRetryConfig.getRetryConfig());
    AtomicInteger retries = new AtomicInteger();
    Callable<Void> callable = () -> {
      System.out.println("Retry count: " + retries.get());
      retries.addAndGet(1);
      try (var connection = dataSource.getConnection()) {
        if (!connection.isValid(5))
          throw new Exception("timeout");
      } catch (Exception e) {
        System.out.printf("Failed to connect postgres: %s, message: %s%n", e.getMessage(), e.getCause());
        throw new RuntimeException(e);
      }
      return null;
    };
    Callable<Void> retriedStart = retry.decorateCallable(callable);
    retriedStart.call();
    return dataSource;
  }

}