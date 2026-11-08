package com.example.orders;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

public final class OrderRepository {
    private static final String PLACE_ORDER = "{call sales.usp_PlaceOrder(?, ?, ?, ?)}";
    private static final String SEARCH = "EXEC [sales].[usp_SearchOrders] @CustomerId = ?, @Status = ?";
    private static final String BALANCE = "SELECT sales.ufn_CustomerBalance(?)";

    private final DataSource dataSource;

    public OrderRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public long placeOrder(String customerRef, List<OrderLine> lines) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             CallableStatement call = connection.prepareCall(PLACE_ORDER)) {
            call.setString(1, customerRef);
            call.setObject(2, OrderLine.toTable(lines));
            call.setBigDecimal(3, new BigDecimal("0.0825"));
            call.registerOutParameter(4, Types.BIGINT);
            call.execute();
            return call.getLong(4);
        }
    }

    public List<OrderSummary> recentOrders(int customerId) throws SQLException {
        String sql = "SELECT OrderId, PlacedAt, Total FROM sales.itvf_RecentOrders(?, 30) WHERE rn <= 10";
        List<OrderSummary> result = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, customerId);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    result.add(new OrderSummary(rows.getLong(1), rows.getTimestamp(2).toInstant(), rows.getBigDecimal(3)));
                }
            }
        }
        return result;
    }

    public BigDecimal balance(int customerId) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(BALANCE)) {
            statement.setInt(1, customerId);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? rows.getBigDecimal(1) : BigDecimal.ZERO;
            }
        }
    }

    public int countCustomers() throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("select count(*) from sales.Customer")) {
            try (ResultSet rows = statement.executeQuery()) {
                rows.next();
                return rows.getInt(1);
            }
        }
    }
}
