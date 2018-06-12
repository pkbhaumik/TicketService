package pb.ticketing.service.repository;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Optional;

import org.apache.log4j.Logger;

import com.google.common.base.Strings;

import pb.ticketing.service.Settings;
import pb.ticketing.service.model.SeatHold;
import pb.ticketing.service.model.SeatInformation;
import pb.ticketing.service.model.TicketService;

public class TicketServiceRepositoryImpl implements TicketService {
	private static Logger LOGGER = Logger.getLogger(TicketServiceRepositoryImpl.class);
	private static String SEAT_HOLD_COLUMN = "SeatHoldId";
	private static String SEAT_ID_COLUMN = "SeatId";
	private static String LEVEL_ID_COLUMN = "LevelId";
	private static String ROW_NUMBER_COLUMN = "RowNumber";
	private static String SEAT_NUMBER_COLUMN = "SeatNumber";

	private final String connectionString;

	public TicketServiceRepositoryImpl(String connectionString) {
		this.connectionString = connectionString;
	}

	@Override
	public int numSeatsAvailable(Optional<Integer> venueLevel) {
		try (Connection connection = Settings.getSQLServerConnection(this.connectionString)) {
			CallableStatement stmt = null;

			if (venueLevel.isPresent()) {
				stmt = connection.prepareCall("{call GetFreeSeatCountForLevel(?)}");
				stmt.setInt(1, venueLevel.get());
			} else {
				stmt = connection.prepareCall("{call GetFreeSeatCount()}");
			}

			try {
				try (ResultSet rs = stmt.executeQuery()) {

					if (rs != null && rs.next()) {
						return rs.getInt(1);
					}
				}
			} finally {
				stmt.close();
			}

		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		return 0;
	}

	@Override
	public SeatHold findAndHoldSeats(int numSeats, Optional<Integer> minLevel, Optional<Integer> maxLevel,
			String customerEmail) throws SQLException {
		if (numSeats <= 0) {
			throw new IllegalArgumentException("Atleast 1 seat should be requested.");
		}

		if (Strings.isNullOrEmpty(customerEmail)) {
			throw new IllegalArgumentException("Provide a valid Email address.");
		}

		if (!minLevel.isPresent()) {
			minLevel = Optional.of(1);
		}

		if (!maxLevel.isPresent()) {
			maxLevel = Optional.of(4);
		}

		try (Connection connection = Settings.getSQLServerConnection(this.connectionString)) {
			try (CallableStatement stmt = connection.prepareCall("{call HoldSeats(?,?,?,?)}")) {
				stmt.setInt(1, numSeats);
				stmt.setInt(2, minLevel.get());
				stmt.setInt(3, maxLevel.get());
				stmt.setString(4, customerEmail);

				try (ResultSet rs = stmt.executeQuery()) {

					if (rs != null && rs.next()) {
						// Get the first record
						SeatHold seatHold = new SeatHold(rs.getInt(SEAT_HOLD_COLUMN), numSeats);

						do {
							SeatInformation seatInfo = new SeatInformation(rs.getInt(SEAT_ID_COLUMN),
									rs.getInt(LEVEL_ID_COLUMN), rs.getInt(ROW_NUMBER_COLUMN),
									rs.getInt(SEAT_NUMBER_COLUMN));
							seatHold.addSeatHold(seatInfo);
						} while (rs.next());

						return seatHold;
					}
				}
			}
		}
		return null;
	}

	@Override
	public String reserveSeats(int seatHoldId, String customerEmail) throws SQLException {
		if (seatHoldId <= 0) {
			throw new IllegalArgumentException("Invalid seatHoldId.");
		}

		if (Strings.isNullOrEmpty(customerEmail)) {
			throw new IllegalArgumentException("Provide a valid Email address.");
		}

		// Create confirmationId
		Calendar dtNow = Calendar.getInstance();

		String confirmationId = String.format("%04d%02d%02d%02d%02d%02d%d", 
				dtNow.get(Calendar.YEAR),
				dtNow.get(Calendar.MONTH),
				dtNow.get(Calendar.DAY_OF_MONTH), 
				dtNow.get(Calendar.HOUR_OF_DAY),
				dtNow.get(Calendar.MINUTE), 
				dtNow.get(Calendar.SECOND), 
				seatHoldId);

		try (Connection connection = Settings.getSQLServerConnection(this.connectionString)) {
			try (CallableStatement stmt = connection.prepareCall("{call ReserveSeats(?,?,?,?)}")) {

				stmt.setInt(1, seatHoldId);
				stmt.setString(2, customerEmail);
				stmt.setString(3, confirmationId);
				stmt.setFloat(4, 500.00f);
				
				try (ResultSet rs = stmt.executeQuery()) {
					if(rs != null && rs.next()) {
						int reservationId = rs.getInt(1);
						
						if(reservationId > 0) {
							return confirmationId;
						}
					}
				}
			}
		}
		
		return null;
	}

}
