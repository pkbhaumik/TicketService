package pb.ticketing.service.repository;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Optional;

import org.apache.log4j.Logger;
import pb.ticketing.service.Settings;
import pb.ticketing.service.model.SeatHold;
import pb.ticketing.service.model.TicketService;

public class TicketServiceRepositoryImpl implements TicketService {
	private static Logger LOGGER = Logger.getLogger(TicketServiceRepositoryImpl.class);
	
	private final String connectionString;
	
	public TicketServiceRepositoryImpl(String connectionString) {
		this.connectionString = connectionString;
	}
	
	@Override
	public int numSeatsAvailable(Optional<Integer> venueLevel) {
		try (Connection connection = Settings.getSQLServerConnection(this.connectionString)) {
			CallableStatement stmt = null;
			
			if(venueLevel.isPresent()) {
				stmt = connection.prepareCall("{call GetFreeSeatCountForLevel(?)}");
				stmt.setInt(1, venueLevel.get());
			}
			else {
				stmt = connection.prepareCall("{call GetFreeSeatCount()}");
			}
			
			try {
				try (ResultSet rs = stmt.executeQuery()) {
					
					if(rs != null && rs.next()) {
						return rs.getInt(1);
					}
				}
			}
			finally {
				stmt.close();
			}

		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		return 0;
	}

	@Override
	public SeatHold findAndHoldSeats(int numSeats, Optional<Integer> minLevel, Optional<Integer> maxLevel, String customerEmail) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String reserveSeats(int seatHoldId, String customerEmail) {
		// TODO Auto-generated method stub
		return null;
	}

}
