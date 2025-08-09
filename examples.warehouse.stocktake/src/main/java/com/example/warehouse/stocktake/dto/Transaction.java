package com.example.warehouse.stocktake.dto;

import java.sql.Timestamp;

public record Transaction(int id, int change, Timestamp created) {
}
