package pb.ticketing.service.controller;

import java.net.InetAddress;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.Future;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import pb.ticketing.service.Settings;
import pb.ticketing.service.model.SeatHold;
import pb.ticketing.service.repository.TicketServiceRepositoryImpl;

@CrossOrigin
@RestController
@RequestMapping(value = "/ticketservice")
public class TicketServiceController {
	private static Logger LOGGER = Logger.getLogger(TicketServiceController.class);
	private static String RESERVATION_HOLD_ID = "_reservation_hold_id_";
	private static String KAFKA_TOPIC_NAME = "ticket-service";

	@Value("${db.server.name}")
	private String databaseServer;

	@Value("${db.database.name}")
	private String databaseName;

	@Value("${db.user.name}")
	private String userName;

	@Value("${db.user.password}")
	private String password;
	
	@Value("${kafka.brokers}")
	private String kafkaBrokers;

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
				try {
					Producer<String, String> publisher = getKafkaProducer();
					
					if(publisher != null) {
						Map<String, String> message = new HashMap<String, String>();
						message.put("_seat_hold_id_", Integer.toString(seatHold.getSeatHoldId()));
						message.put("_expiring_at_", Long.toString(seatHold.getExpiringAt()));
						
						final Gson GSON = new GsonBuilder().serializeSpecialFloatingPointValues().create();
						
						Future<RecordMetadata> meta = publisher.send(new ProducerRecord<String, String>(KAFKA_TOPIC_NAME, GSON.toJson(message)));
						meta.get();
						
						publisher.close();
					}
				}
				catch(Exception e) {
					LOGGER.error(e.getMessage());
				}
				
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
				try {
					Producer<String, String> publisher = getKafkaProducer();
					
					if(publisher != null) {
						Map<String, String> message = new HashMap<String, String>();
						message.put(RESERVATION_HOLD_ID, Integer.toString(seatHoldId));
						
						final Gson GSON = new GsonBuilder().serializeSpecialFloatingPointValues().create();
						
						Future<RecordMetadata> meta = publisher.send(new ProducerRecord<String, String>(KAFKA_TOPIC_NAME, GSON.toJson(message)));
						meta.get();
						
						publisher.close();
					}
				}
				catch(Exception e) {
					LOGGER.error(e.getMessage());
				}
				
				return new ResponseEntity<>(confirmationNumber, HttpStatus.OK);
			} else {
				return new ResponseEntity<>("", HttpStatus.BAD_REQUEST);
			}
		} catch (SQLException e) {
			LOGGER.error(e.getMessage());
			return new ResponseEntity<>(e.getMessage(), HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	private Producer<String, String> getKafkaProducer() {
		// create the KAFKA storm pipe publisher
		Properties props = new Properties();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBrokers);
		props.put(ProducerConfig.ACKS_CONFIG, "all");
		props.put(ProducerConfig.RETRIES_CONFIG, 1);
		props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
		props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
		props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
		props.put(ProducerConfig.CLIENT_ID_CONFIG, getHostName() + "-ts-producer");
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
				"org.apache.kafka.common.serialization.StringSerializer");
		props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);
		props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 15000);
		props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 1000);

		return new KafkaProducer<String, String>(props);
	}

	private static String getHostName() {
		try {
			InetAddress myHost = InetAddress.getLocalHost();
			return myHost.getHostName();
		} catch (Exception e) {

		}

		return "Unknown";
	}
}
