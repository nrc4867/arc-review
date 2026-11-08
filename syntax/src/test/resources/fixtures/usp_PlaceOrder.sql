/*
    Places an order for a customer from a table-valued parameter of lines.
    Returns the new order id, or raises when the customer is over their credit limit.
*/
CREATE OR ALTER PROCEDURE [sales].[usp_PlaceOrder]
    @CustomerRef NVARCHAR(64),
    @Lines       sales.OrderLineTable READONLY,
    @TaxRate     DECIMAL(5, 4) = 0.0825,
    @OrderId     BIGINT OUTPUT
WITH EXECUTE AS OWNER
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    DECLARE @CustomerId INT,
            @Credit     DECIMAL(18, 2),
            @Subtotal   DECIMAL(18, 2) = 0,
            @Msg        NVARCHAR(2048);

    SELECT @CustomerId = c.CustomerId, @Credit = c.CreditLimit
    FROM [sales].[Customer] AS c WITH (UPDLOCK, HOLDLOCK)
    WHERE c.ExternalRef = @CustomerRef;

    IF @CustomerId IS NULL
    BEGIN
        SET @Msg = CONCAT(N'Unknown customer ', QUOTENAME(@CustomerRef, N''''));
        THROW 50001, @Msg, 1;
    END;

    SELECT @Subtotal = SUM(l.Quantity * l.UnitPrice) FROM @Lines AS l;

    ;WITH OpenOrders AS
    (
        SELECT o.CustomerId, SUM(o.Subtotal + o.Tax) AS Outstanding
        FROM sales.[Order] o
        WHERE o.CustomerId = @CustomerId
          AND o.[Status] IN (0, 1, 2)
        GROUP BY o.CustomerId
    )
    SELECT @Credit = @Credit - ISNULL(oo.Outstanding, 0)
    FROM OpenOrders oo;

    IF @Subtotal * (1 + @TaxRate) > @Credit
    BEGIN
        RAISERROR (N'Credit limit exceeded for %s (available %s)', 16, 1, @CustomerRef, @Credit);
        RETURN -1;
    END

    BEGIN TRY
        BEGIN TRANSACTION;

        INSERT INTO sales.[Order] (CustomerId, [Status], PlacedAt, Subtotal, Tax)
        VALUES (@CustomerId, 0, SYSUTCDATETIME(), @Subtotal, ROUND(@Subtotal * @TaxRate, 2));

        SET @OrderId = SCOPE_IDENTITY();

        INSERT INTO sales.OrderLine (OrderId, Sku, Quantity, UnitPrice)
        SELECT @OrderId, l.Sku, l.Quantity, l.UnitPrice
        FROM @Lines l;

        EXEC sales.usp_WriteAudit @Entity = N'Order', @EntityId = @OrderId, @Action = N'PLACE';

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;

        DECLARE @ErrNo INT = ERROR_NUMBER(), @ErrMsg NVARCHAR(4000) = ERROR_MESSAGE();
        EXEC sales.usp_WriteAudit @Entity = N'Order', @EntityId = NULL, @Action = N'FAIL', @Detail = @ErrMsg;
        ;THROW;
    END CATCH
END
GO

GRANT EXECUTE ON OBJECT::[sales].[usp_PlaceOrder] TO [app_orders];
GO
