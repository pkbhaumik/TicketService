package pb.ticketing.service.controller;

import java.util.Optional;
import java.util.UUID;

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

import pb.ticketing.service.Settings;
import pb.ticketing.service.model.SeatHold;
import pb.ticketing.service.repository.TicketServiceRepositoryImpl;

@CrossOrigin
@RestController
@RequestMapping(value="/ticketservice")
public class TicketServiceController {
	
	@Value("${db.server.name}")
	private String databaseServer;
	
	@Value("${db.database.name}")
	private String databaseName;
	
	@Value("${db.user.name}")
	private String userName;
	
	@Value("${db.user.password}")
	private String password;
	
	public TicketServiceController() {}
	
	@GetMapping(value = "numSeatAvailable", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Integer> get(
			@RequestParam(value = "venueLevel", required = false) Integer venueLevel) {
		
		TicketServiceRepositoryImpl repo = new TicketServiceRepositoryImpl(Settings.getSqlServerConnectionString(databaseServer, databaseName, userName, password));
		
		Optional<Integer> optionalVenueLevel = Optional.ofNullable(venueLevel); 
		
		return new ResponseEntity<>(repo.numSeatsAvailable(optionalVenueLevel), HttpStatus.OK);
	}
	
	@PostMapping(value = "findAndHoldSeats", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<SeatHold> post(
			@RequestParam(value = "numSeats", required = true) Integer numSeats,
			@RequestParam(value = "minLevel", required = false) Integer minLevel,
			@RequestParam(value = "maxLevel", required = false) Integer maxLevel,
			@RequestParam(value = "customerEmail", required = true) String customerEmail) {

		return new ResponseEntity<>(new SeatHold(1, numSeats), HttpStatus.OK);

	}
	
	@PostMapping(value = "reserveSeats", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> post(
			@RequestParam(value = "SeatHoldId", required = true) Integer seatHoldId,
			@RequestParam(value = "customerEmail", required = true) String customerEmail)
	{
		return new ResponseEntity<>(UUID.randomUUID().toString(), HttpStatus.OK);
	}

}
