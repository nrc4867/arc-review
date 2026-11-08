SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

IF NOT EXISTS (SELECT 1 FROM sys.schemas WHERE name = N'sales')
    EXEC (N'CREATE SCHEMA [sales] AUTHORIZATION [dbo]');
GO

CREATE TABLE [sales].[Customer]
(
    [CustomerId]    INT IDENTITY(1, 1) NOT NULL,
    [ExternalRef]   NVARCHAR(64)       NOT NULL,
    [DisplayName]   NVARCHAR(200)      NOT NULL,
    [CreditLimit]   DECIMAL(18, 2)     NOT NULL CONSTRAINT [DF_Customer_CreditLimit] DEFAULT (0),
    [CreatedAt]     DATETIME2(3)       NOT NULL CONSTRAINT [DF_Customer_CreatedAt] DEFAULT (SYSUTCDATETIME()),
    [RowVersion]    ROWVERSION         NOT NULL,
    CONSTRAINT [PK_Customer] PRIMARY KEY CLUSTERED ([CustomerId] ASC),
    CONSTRAINT [UQ_Customer_ExternalRef] UNIQUE NONCLUSTERED ([ExternalRef])
);
GO

CREATE TABLE [sales].[Order]
(
    [OrderId]       BIGINT IDENTITY(1, 1) NOT NULL,
    [CustomerId]    INT                NOT NULL,
    [Status]        TINYINT            NOT NULL CONSTRAINT [DF_Order_Status] DEFAULT (0),
    [PlacedAt]      DATETIME2(3)       NOT NULL,
    [Total]         AS (CONVERT(DECIMAL(18, 2), [Subtotal] + [Tax])) PERSISTED,
    [Subtotal]      DECIMAL(18, 2)     NOT NULL,
    [Tax]           DECIMAL(18, 2)     NOT NULL,
    CONSTRAINT [PK_Order] PRIMARY KEY CLUSTERED ([OrderId]),
    CONSTRAINT [FK_Order_Customer] FOREIGN KEY ([CustomerId]) REFERENCES [sales].[Customer] ([CustomerId]),
    CONSTRAINT [CK_Order_Status] CHECK ([Status] BETWEEN 0 AND 4)
);
GO

CREATE NONCLUSTERED INDEX [IX_Order_Customer_PlacedAt]
    ON [sales].[Order] ([CustomerId], [PlacedAt] DESC)
    INCLUDE ([Status], [Subtotal], [Tax])
    WITH (ONLINE = ON, DATA_COMPRESSION = PAGE);
GO

CREATE TABLE sales.OrderLine
(
    OrderLineId BIGINT IDENTITY NOT NULL PRIMARY KEY,
    OrderId     BIGINT          NOT NULL REFERENCES sales.[Order] (OrderId) ON DELETE CASCADE,
    Sku         VARCHAR(32)     NOT NULL,
    Quantity    INT             NOT NULL,
    UnitPrice   MONEY           NOT NULL
);
GO

CREATE TYPE sales.OrderLineTable AS TABLE
(
    Sku       VARCHAR(32) NOT NULL,
    Quantity  INT         NOT NULL,
    UnitPrice MONEY       NOT NULL,
    INDEX IX_Sku NONCLUSTERED (Sku)
);
GO
