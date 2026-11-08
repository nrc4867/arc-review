package com.example.orders

import javax.sql.DataSource

@JvmInline
value class CustomerRef(val value: String)

class CustomerSync(private val dataSource: DataSource) {
    fun sync(batchSize: Int = 5_000, dryRun: Boolean = false): Map<String, Int> =
        dataSource.connection.use { connection ->
            connection.prepareCall("{call sales.usp_SyncCustomers(?, ?)}").use { call ->
                call.setInt(1, batchSize)
                call.setBoolean(2, dryRun)
                call.executeQuery().use { rows ->
                    buildMap { while (rows.next()) put(rows.getString(1), rows.getInt(2)) }
                }
            }
        }

    fun summary(ref: CustomerRef): String? = dataSource.connection.use { connection ->
        val sql = """
            SELECT s.DisplayName, s.Orders, s.Spent
            FROM sales.vw_CustomerSummary AS s
            JOIN sales.Customer AS c ON c.CustomerId = s.CustomerId
            WHERE c.ExternalRef = ?
        """.trimIndent()
        connection.prepareStatement(sql).use { statement ->
            statement.setString(1, ref.value)
            statement.executeQuery().use { rows -> if (rows.next()) rows.getString(1) else null }
        }
    }

    fun placeOrder(repository: OrderRepository, ref: CustomerRef, lines: List<OrderLine>): Long =
        repository.placeOrder(ref.value, lines)
}
