package unidue.ub.services.getter;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import unidue.ub.media.monographs.Event;
import unidue.ub.media.monographs.Item;
import unidue.ub.media.monographs.Manifestation;

@Controller
@RefreshScope
@RequestMapping("/getter")
@CrossOrigin(origins="http://localhost")
public class GetterControler {
	
	@Autowired
    JdbcTemplate jdbcTemplate;

	private final String select = "select distinct substr(z30_rec_key,1,9) as docNumber from edu50.z30 where ( z30_call_no = ?) ";
	
	private final String like = "or ( z30_call_no like ?) ";
	
	private final String orderBy = "order by docNumber";

	private final String getNotation = "select distinct substr(z30_rec_key,1,9) as docNumber from edu50.z30 where ( z30_call_no like ?)";
	
	private final String getEtat = "select distinct substr(z75_rec_key,1,9) as docNumber from edu50.z601, edu50.z75 where z601_rec_key_2 = z75_rec_key_2 and z601_rec_key like ? and z601_type = 'INV'";

	private final String getCurrentItems = "select z30_call_no, z30_rec_key, z30_price, z30_collection, z30_material, z30_sub_library, z30_item_status, z30_item_process_status, z30_update_date, z30_inventory_number_date from edu50.z30 where z30_rec_key like ?";
	
	private final String getDeletedItems = "select z30h_call_no, z30h_rec_key, z30h_price, z30h_collection, z30h_material, z30h_sub_library, z30h_item_status, z30h_item_process_status, z30h_inventory_number_date, z30h_h_date, z30h_update_date, z30h_h_reason_type from edu50.z30h where z30h_h_reason_type = 'DELETE' and z30h_rec_key like ?";
	
	private final String getBudget  = "select z601_rec_key from edu50.z601,edu50.z75 where z601_rec_key_2  = z75_rec_key_2 and z75_rec_key like ? and z601_type = 'INV'";
	
	private final String getItemsByBarCode = "select z30_call_no, z30_rec_key, z30_price, z30_collection, z30_material, z30_sub_library, z30_item_status, z30_item_process_status, z30_update_date, z30_inventory_number_date from edu50.z30 where z30_barcode = ?";
	
	private final String getClosedLoans = "select z36h_rec_key, z36h_material, z36h_status, z36h_bor_status, z36h_loan_date, z36h_loan_hour, z36h_returned_date, z36h_returned_hour, z36h_sub_library from edu50.z36h where z36h_rec_key like ? and z36h_loan_date > '20000000' order by z36h_loan_date, z36h_loan_hour, z36h_number";
	
	private final String getOpenLoans = "select z36_rec_key, z36_material, z36_status, z36_bor_status, z36_loan_date, z36_loan_hour, z36_sub_library from edu50.z36 where z36_rec_key like ? and z36_loan_date > '20000000' order by z36_loan_date, z36_loan_hour, z36_number";
	
	private final String getClosedRequests = "select z37h_rec_key, z37h_open_date, z37h_open_hour, z37h_hold_date, z37h_pickup_location from edu50.z37h where z37h_rec_key like ? and z37h_open_date > '20000000' order by z37h_open_date, z37h_open_hour, z37h_rec_key";
	
	private final String getOpenRequests = "select z37_rec_key, z37_open_date, z37_open_hour, z37_pickup_location from edu50.z37 where z37_rec_key like ? and z37_open_date > '20000000' order by z37_open_date, z37_open_hour, z37_rec_key";
	
	private final static String SUB_D = "_d";
	
	@RequestMapping("/manifestation")
	public ResponseEntity<?> get(
			@RequestParam("identifier") String identifier,
			@RequestParam("exact") String exact,
			@RequestParam("mode") String mode) {
		List<Manifestation> manifests = new ArrayList<>();
		Boolean exactBoolean = "true".equals(exact);
		String query = "";
		switch (mode) {
		case "shelfmark":
			String suffix = "";
			if (identifier.endsWith(SUB_D)) {
				identifier = identifier.substring(0, identifier.length() - SUB_D.length());
				suffix = SUB_D;
			}
			identifier = identifier.toUpperCase();
			query = exactBoolean ? (select + like + orderBy + suffix) : (select + like + like + like + orderBy + suffix);
		case "etat":
			query = getEtat + orderBy;
		case "notation":
			query = getNotation + orderBy;
		}
		manifests.addAll(jdbcTemplate.query(query, new Object[]{identifier},(rs, rowNum) -> new Manifestation(rs.getString("docNumber"))));
		return ResponseEntity.ok(manifests);
	}
	
	@RequestMapping("/item")
	public ResponseEntity<?> getItems(
			@RequestParam("identifier") String identifier,
			@RequestParam("mode") String mode) {
		List<Item> items = new ArrayList<>();
		switch (mode) {
		case "docNumber":
			items.addAll(jdbcTemplate.query(getCurrentItems, new Object[]{identifier + "%"},(rs, rowNum) -> 
			new Item(rs.getString("z30_collection"), 
					rs.getString("z30_call_no"), 
					rs.getString("z30_sub_library"),
					Integer.parseInt(rs.getString("z30_rec_key").substring(9)),
					rs.getString("z30_material"),
					rs.getString("z30_item_status"),
					rs.getString("z30_item_process_status"),
					rs.getString("z30_inventory_number_date"),
					rs.getString("z30_update_date"),
					rs.getString("z30_price")
					)));
			items.addAll(jdbcTemplate.query(getDeletedItems, new Object[]{identifier + "%"},(rs, rowNum) -> 
			new Item(rs.getString("z30_collection"), 
					rs.getString("z30_call_no"), 
					rs.getString("z30_sub_library"),
					Integer.parseInt(rs.getString("z30_rec_key").substring(9)),
					rs.getString("z30_material"),
					rs.getString("z30_item_status"),
					rs.getString("z30_item_process_status"),
					rs.getString("z30_inventory_number_date"),
					rs.getString("z30_update_date"),
					rs.getString("z30_price")
					)));
		
		case "barcode":
			items.addAll(jdbcTemplate.query(getItemsByBarCode, new Object[]{identifier + "%"},(rs, rowNum) -> 
			new Item(rs.getString("z30_collection"), 
					rs.getString("z30_call_no"), 
					rs.getString("z30_sub_library"),
					Integer.parseInt(rs.getString("z30_rec_key").substring(9)),
					rs.getString("z30_material"),
					rs.getString("z30_item_status"),
					rs.getString("z30_item_process_status"),
					rs.getString("z30_inventory_number_date"),
					rs.getString("z30_update_date"),
					rs.getString("z30_price")
					)));
		
		}
		return ResponseEntity.ok(items);
	}

	@RequestMapping(value="/event",
			method=RequestMethod.GET,
			consumes=MediaType.APPLICATION_JSON_VALUE,
			produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> getEvents(@RequestBody Manifestation manifestation) {
		List<RawEvent> rawEvents = new ArrayList<>();
		rawEvents.addAll(jdbcTemplate.query(getOpenLoans, new Object[]{manifestation.getDocNumber() + "%"},(rs, rowNum) ->
		new RawEvent(rs.getString("z36_rec_key"),
				rs.getString("z36_sub_library"),
				rs.getString("z36_bor_status"),
				rs.getString("z36_loan_date"),
				rs.getString("z36_loan_hour")
				)));
		List<Event> events = new ArrayList<>();
		for (RawEvent rawEvent : rawEvents) {
			
		}
		return ResponseEntity.ok(events);
	}
}
