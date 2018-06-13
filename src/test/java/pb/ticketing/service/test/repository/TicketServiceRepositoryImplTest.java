package pb.ticketing.service.test.repository;

import static org.junit.Assert.*;

import java.util.Optional;

import org.junit.Test;

import com.google.common.base.Strings;

import pb.ticketing.service.model.SeatHold;
import pb.ticketing.service.model.TicketService;
import pb.ticketing.service.repository.TicketServiceRepositoryImpl;

public class TicketServiceRepositoryImplTest {
	private static String connectionString = "jdbc:sqlserver://127.0.0.1\\SQLEXPRESS;databaseName=TicketService;user=ticketservice;password=Ticketservice123;Max Pool Size=50";

	@Test
	public void testNumSeatsAvailable() {
		TicketService ts = new TicketServiceRepositoryImpl(connectionString);
		Optional<Integer> venueLevel = Optional.of(1);
		int numSeat1 = ts.numSeatsAvailable(venueLevel);
		int numSeat2 = ts.numSeatsAvailable(venueLevel);
		
		assertEquals("numSeatsAvailable - Level 1", numSeat1, numSeat2);
	}
	
	@Test
	public void testNumSeatsAvailableAll() {
		TicketService ts = new TicketServiceRepositoryImpl(connectionString);
		Optional<Integer> venueLevel = Optional.empty();
		int numSeat1 = ts.numSeatsAvailable(venueLevel);
		int numSeat2 = ts.numSeatsAvailable(venueLevel);
		
		assertEquals("numSeatsAvailable - ALL Level", numSeat1, numSeat2);
	}

	@Test
	public void testFindAndHoldSeats() throws Exception {
		TicketService ts = new TicketServiceRepositoryImpl(connectionString);

		SeatHold seatHold = ts.findAndHoldSeats(1, Optional.empty(), Optional.empty(), "test@test.com");
		assertEquals("findAndHoldSeats", seatHold.getNumSeats(), 1);
	}

	@Test
	public void testReserveSeats() throws Exception {
		TicketService ts = new TicketServiceRepositoryImpl(connectionString);

		SeatHold seatHold = ts.findAndHoldSeats(1, Optional.empty(), Optional.empty(), "test@test.com");
		String confirmationId = ts.reserveSeats(seatHold.getSeatHoldId(), "test@test.com");
		assertTrue("reserveSeats", Strings.isNullOrEmpty(confirmationId) == false);
	}

}
