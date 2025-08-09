package com.example.warehouse.inventory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class PostgresConnectionFactory {
  private String url;
  private String username;
  private String password;

  public void setUrl(String url){
    this.url = url;
  }

  public void setUsername(String username){
    this.username = username;
  }

  public void setPassword(String password){
    this.password = password;
  }

  public Connection Build() throws SQLException {
    return DriverManager.getConnection(url, username, password);
  }
}
