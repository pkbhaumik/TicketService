CREATE DATABASE TicketService;
GO

USE TicketService;
GO

CREATE SCHEMA TS;
GO


CREATE TABLE [TS].[Stage] 
(
	LevelId tinyint NOT NULL,
	LevelName VARCHAR(30) NOT NULL,
	TotalTows smallint NOT NULL,
	SeatsInRow smallint NOT NULL,
	Price decimal(5,2) NOT NULL,
	CONSTRAINT PK_Stage_LevelId PRIMARY KEY CLUSTERED (LevelId)
);

GO

INSERT INTO [TS].[Stage] (LevelId, LevelName, TotalTows, SeatsInRow, Price) VALUES(1, 'Orchestra', 25, 50, 100.00);
INSERT INTO [TS].[Stage] (LevelId, LevelName, TotalTows, SeatsInRow, Price) VALUES(2, 'Main', 20, 100, 75.00);
INSERT INTO [TS].[Stage] (LevelId, LevelName, TotalTows, SeatsInRow, Price) VALUES(3, 'Balcony 1', 15, 100, 50.00);
INSERT INTO [TS].[Stage] (LevelId, LevelName, TotalTows, SeatsInRow, Price) VALUES(4, 'Balcony 2', 15, 100, 40.00);
GO

CREATE TABLE [TS].[SeatHold] 
(
	SeatHoldId int IDENTITY (1,1) NOT NULL,
	CustomerEmail VARCHAR(128) NOT NULL,
	ExpiringAt DateTime2 NOT NULL,
	Deleted Bit NOT NULL DEFAULT(0)
);
GO

CREATE NONCLUSTERED INDEX IDX_SeatHold_SeatHoldId 
	ON [TS].[SeatHold] (SeatHoldId)
	INCLUDE(CustomerEmail, ExpiringAt);
GO

CREATE NONCLUSTERED INDEX IDX_SeatHold_CustomerEmail 
	ON [TS].[SeatHold] (CustomerEmail, Deleted)
	INCLUDE (SeatHoldId, ExpiringAt);
GO

CREATE TABLE [TS].[Reservation] 
(
	ReservationId INT IDENTITY (1,1) NOT NULL,
	ConfirmationId VARCHAR(30) NOT NULL,
	CustomerEmail varchar(128) NOT NULL,
	TotalPaid decimal(5,2) NOT NULL,
	ReservedTime DateTime2 NOT NULL,
	Cancelled BIT NOT NULL DEFAULT(0),
	CancelledTime DateTime2,
	CONSTRAINT PK_Reservation_ReservationId PRIMARY KEY CLUSTERED (ReservationId)
);

GO



CREATE TABLE [TS].[SeatMap] 
(
	SeatId int IDENTITY (1,1) NOT NULL,
	LevelId tinyint NOT NULL,
	RowNumber smallint NOT NULL,
	SeatNumber smallint NOT NULL,
	Status tinyint NOT NULL DEFAULT(0),
	SeatHoldId int,
	ReservationId int,
	CONSTRAINT PK_SeatMap_SeatId PRIMARY KEY CLUSTERED (SeatId),
	CONSTRAINT FK_SeatMap_Stage_LevelId FOREIGN KEY (LevelId) REFERENCES [TS].[Stage] (LevelId),
	CONSTRAINT FK_SeatMap_Reservation_ReservationId FOREIGN KEY (ReservationId) REFERENCES [TS].[Reservation] (ReservationId)
);
GO

CREATE NONCLUSTERED INDEX IDX_SeatMap_Status 
	ON [TS].[SeatMap] (Status, LevelId, RowNumber, SeatNumber) 
	INCLUDE (SeatId,  SeatHoldId, ReservationId);

GO


/*
* Procedures
*
*
*/

CREATE PROCEDURE GetFreeSeatCount 
AS 
BEGIN
	SELECT COUNT(*) AS Seats
	FROM [TS].[SeatMap]
	WHERE Status = 0;
END
GO

CREATE PROCEDURE GetFreeSeatCountForLevel @level int
AS 
BEGIN
	SELECT COUNT(*) AS Seats
	FROM [TS].[SeatMap]
	WHERE Status = 0
		AND LevelId = @level;
END
GO









	 

