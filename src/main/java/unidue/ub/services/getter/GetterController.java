package unidue.ub.services.getter;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import unidue.ub.media.monographs.Event;
import unidue.ub.media.monographs.Item;
import unidue.ub.media.monographs.Manifestation;

@Controller
@RefreshScope
@RequestMapping("/getter")
@CrossOrigin(origins = "http://localhost")
public class GetterController {

	@Autowired
	JdbcTemplate jdbcTemplate;

	private static final Logger LOGGER = LoggerFactory.getLogger(GetterController.class);

	@RequestMapping("/manifestations")
	public ResponseEntity<?> get(@RequestParam("identifier") String identifier, @RequestParam("exact") String exact,
			@RequestParam("mode") String mode) {

		ManifestationGetter manifestationgetter = new ManifestationGetter(jdbcTemplate);
		List<Manifestation> manifestations = new ArrayList<>();
		Boolean exactBoolean = "true".equals(exact);
		switch (mode) {
		case "shelfmark":
			manifestations = manifestationgetter.getDocumentsByShelfmark(identifier, exactBoolean);
		case "etat":
			manifestations = manifestationgetter.getDocumentsByEtat(identifier, exactBoolean);
		case "notation":
			manifestations = manifestationgetter.getDocumentsByNotation(identifier);
		}
		return ResponseEntity.ok(manifestations);
	}

	@RequestMapping("/items")
	public ResponseEntity<?> getItems(@RequestParam("identifier") String identifier,
			@RequestParam("mode") String mode) {
		List<Item> items = new ArrayList<>();
		ItemGetter itemGetter = new ItemGetter(jdbcTemplate);
		switch (mode) {
		case "docNumber":
			items = itemGetter.getItemsByDocNumber(identifier);
		case "barcode":

		}
		return ResponseEntity.ok(items);
	}

	@RequestMapping("/loans")
	public ResponseEntity<?> getLoans(@RequestParam("identifier") String identifier,
			@RequestParam("mode") String mode) {
		EventGetter eventGetter = new EventGetter(jdbcTemplate);
		List<Event> events = eventGetter.getLoansByDocNumber(identifier);
		return ResponseEntity.ok(events);
	}

	@RequestMapping("/requests")
	public ResponseEntity<?> getEvents(@RequestParam("identifier") String identifier,
			@RequestParam("mode") String mode) {
		List<Event> events = new ArrayList<>();
		EventGetter eventGetter = new EventGetter(jdbcTemplate);
		switch (mode) {
		case "docNumber":
			events = eventGetter.getRequestsByDocNumber(identifier);
		case "latestRequests":

		}
		return ResponseEntity.ok(events);
	}

	@RequestMapping("/fullManifestation")
	public ResponseEntity<?> getFullManifestation(@RequestParam("identifier") String identifier,
			@RequestParam("collection") String collection, @RequestParam("material") String material) {
		ItemGetter itemGetter = new ItemGetter(jdbcTemplate);
		Manifestation manifestation = new Manifestation(identifier);
		List<Item> items = itemGetter.getItemsByDocNumber(identifier);
		return ResponseEntity.ok(manifestation);
	}
}
