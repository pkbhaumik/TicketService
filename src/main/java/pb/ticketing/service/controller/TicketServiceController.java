package pb.ticketing.service.controller;

import java.sql.SQLException;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.Strings;

import pb.ticketing.service.Settings;
import pb.ticketing.service.model.SeatHold;
import pb.ticketing.service.repository.TicketServiceRepositoryImpl;

@CrossOrigin
@RestController
@RequestMapping(value = "/ticketservice")
public class TicketServiceController {
	private static Logger LOGGER = Logger.getLogger(TicketServiceController.class);

	@Value("${db.server.name}")
	private String databaseServer;

	@Value("${db.database.name}")
	private String databaseName;

	@Value("${db.user.name}")
	private String userName;

	@Value("${db.user.password}")
	private String password;

	public TicketServiceController() {
	}

	@GetMapping(value = "numSeatAvailable", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Integer> get(
			@RequestParam(value = "venueLevel", required = false) Integer venueLevel) {

		Optional<Integer> optionalVenueLevel = Optional.ofNullable(venueLevel);

		TicketServiceRepositoryImpl ticketServiceRepo = new TicketServiceRepositoryImpl(
				Settings.getSqlServerConnectionString(databaseServer, databaseName, userName, password));

		return new ResponseEntity<>(ticketServiceRepo.numSeatsAvailable(optionalVenueLevel), HttpStatus.OK);
	}

	@PostMapping(value = "findAndHoldSeats", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<SeatHold> post(
			@RequestParam(value = "numSeats", required = true) Integer numSeats,
			@RequestParam(value = "minLevel", required = false) Integer minLevel,
			@RequestParam(value = "maxLevel", required = false) Integer maxLevel,
			@RequestParam(value = "customerEmail", required = true) String customerEmail) {

		Optional<Integer> optionalMinLevel = Optional.ofNullable(minLevel);
		Optional<Integer> optionalMaxLevel = Optional.ofNullable(maxLevel);

		try {
			TicketServiceRepositoryImpl ticketServiceRepo = new TicketServiceRepositoryImpl(
					Settings.getSqlServerConnectionString(databaseServer, databaseName, userName, password));
			SeatHold seatHold = ticketServiceRepo.findAndHoldSeats(numSeats, optionalMinLevel, optionalMaxLevel, customerEmail);

			if (seatHold != null) {
				return new ResponseEntity<>(seatHold, HttpStatus.OK);
			}

		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}

		return new ResponseEntity<>(null, HttpStatus.EXPECTATION_FAILED);

	}

	@PostMapping(value = "reserveSeats", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> post(
			@RequestParam(value = "SeatHoldId", required = true) Integer seatHoldId,
			@RequestParam(value = "customerEmail", required = true) String customerEmail) {
		try {
			TicketServiceRepositoryImpl ticketServiceRepo = new TicketServiceRepositoryImpl(
					Settings.getSqlServerConnectionString(databaseServer, databaseName, userName, password));
			String confirmationNumber = ticketServiceRepo.reserveSeats(seatHoldId, customerEmail);
			if (!Strings.isNullOrEmpty(confirmationNumber)) {
				return new ResponseEntity<>(confirmationNumber, HttpStatus.OK);
			} else {
				return new ResponseEntity<>("", HttpStatus.BAD_REQUEST);
			}
		} catch (SQLException e) {
			LOGGER.error(e.getMessage());
			return new ResponseEntity<>(e.getMessage(), HttpStatus.EXPECTATION_FAILED);
		}
	}
}
