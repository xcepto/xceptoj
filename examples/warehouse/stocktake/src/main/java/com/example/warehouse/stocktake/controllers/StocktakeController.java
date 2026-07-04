package com.example.warehouse.stocktake.controllers;

import com.example.warehouse.stocktake.dto.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Controller
public class StocktakeController {

  @Autowired
  private JdbcTemplate jdbc;

  @GetMapping("/")
  public String GetStocktake(Model model){

    List<Transaction> transactions = jdbc.query("SELECT * FROM inventory;", (rs, rowNum) -> new Transaction(
        rs.getInt("id"),
        rs.getInt("change"),
        rs.getTimestamp("created")
    ));

    Integer total = transactions.stream()
        .map(Transaction::change)
        .mapToInt(x -> x)
        .sum();

    model.addAttribute("total", total);
    model.addAttribute("transactions", transactions);
    System.out.println(transactions.stream().count());

    return "stocktake";
  }
}
