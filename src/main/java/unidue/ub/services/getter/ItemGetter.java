package unidue.ub.services.getter;

import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;

import unidue.ub.media.monographs.Item;
import unidue.ub.services.getter.queryresults.RawDeletedItem;

class ItemGetter {
	
	private JdbcTemplate jdbcTemplate;
	
	private final String getCurrentItems = "select z30_call_no, z30_rec_key, z30_price, z30_collection, z30_material, z30_sub_library, z30_item_status, z30_item_process_status, z30_update_date, z30_inventory_number_date from edu50.z30 where z30_rec_key like ?";

	private final String getDeletedItems = "select z30h_call_no, z30h_rec_key, z30h_price, z30h_collection, z30h_material, z30h_sub_library, z30h_item_status, z30h_item_process_status, z30h_inventory_number_date, z30h_h_date, z30h_update_date, z30h_h_reason_type from edu50.z30h where z30h_h_reason_type = 'DELETE' and z30h_rec_key like ?";

	private final String getDeletedItem = "select z30h_call_no, z30h_rec_key, z30h_price, z30h_collection, z30h_material, z30h_sub_library, z30h_item_status, z30h_item_process_status, z30h_inventory_number_date, z30h_h_date, z30h_update_date, z30h_h_reason_type from edu50.z30h where z30h_rec_key like ?";

	private final String getItemsByBarCode = "select z30_call_no, z30_rec_key, z30_price, z30_collection, z30_material, z30_sub_library, z30_item_status, z30_item_process_status, z30_update_date, z30_inventory_number_date from edu50.z30 where z30_barcode = ?";
	
	ItemGetter(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	private List<Item> items;

	List<Item> getItemsByDocNumber(String identifier) {
		items = new ArrayList<>();
		items.addAll(jdbcTemplate.query(getCurrentItems, new Object[]{identifier + "%"},(rs, rowNum) ->
		new Item(rs.getString("z30_rec_key"),
				rs.getString("z30_collection"), 
				rs.getString("z30_call_no"), 
				rs.getString("z30_sub_library"),
				rs.getString("z30_material"),
				rs.getString("z30_item_status"),
				rs.getString("z30_item_process_status"),
				rs.getString("z30_inventory_number_date"),
				rs.getString("z30_update_date"),
				rs.getString("z30_price")
				)));
		for (Item item : items) {
			if (!(item.getItemStatus().equals("89") || item.getItemStatus().equals("90") || item.getItemStatus().equals("xx"))) {
				item.setDeletionDate("");
			} else {
				item.setShelfmark("???");
			}
		}
		List<RawDeletedItem> rawDeletedItems = new ArrayList<>();
		rawDeletedItems.addAll(jdbcTemplate.query(getDeletedItems, new Object[]{identifier + "%"},(rs, rowNum) ->
		new RawDeletedItem(rs.getString("z30h_rec_key"),
				rs.getString("z30h_collection"), 
				rs.getString("z30h_call_no"), 
				rs.getString("z30h_sub_library"),
				rs.getString("z30h_material"),
				rs.getString("z30h_item_status"),
				rs.getString("z30h_item_process_status"),
				rs.getString("z30h_inventory_number_date"),
				rs.getString("z30h_update_date"),
				rs.getString("z30h_h_date"),
				rs.getString("z30h_price")
				)));
		if (rawDeletedItems.size() > 0) {
			for (RawDeletedItem rawDeletedItem : rawDeletedItems) {
				try {
					Item item = rawDeletedItem.getItem();
					String itemStatus = rawDeletedItem.getItemStatus();
					if ((itemStatus.equals("89") || itemStatus.equals("90") || itemStatus.equals("xx"))) {
						item.setDeletionDate(rawDeletedItem.getUpdateDate());
					}
					item.setShelfmark("???");
					items.add(item);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		cleanUpFields();
		return items;
	}

	Item getItemByItemId(String ItemId) {
		List<RawDeletedItem> rawDeletedItems = jdbcTemplate.query(getDeletedItem, new Object[]{ItemId + "%"},(rs, rowNum) ->
				new RawDeletedItem(rs.getString("z30h_rec_key"),
						rs.getString("z30h_collection"),
						rs.getString("z30h_call_no"),
						rs.getString("z30h_sub_library"),
						rs.getString("z30h_material"),
						rs.getString("z30h_item_status"),
						rs.getString("z30h_item_process_status"),
						rs.getString("z30h_inventory_number_date"),
						rs.getString("z30h_update_date"),
						rs.getString("z30h_h_date"),
						rs.getString("z30h_price")
				));
		return extractItem(rawDeletedItems);
	}

	private Item extractItem(List<RawDeletedItem> rawDeletedItems) {
		Item item = new Item();
		if (rawDeletedItems.size() > 0) {
			/* TODO: find an example for events with item 'xx' and see the result in the query, build the corresponding analysis here.*/
		}
		return item;
	}

	private void cleanUpFields() {
		for (Item item : items) {
			if (item.getItemId().length() > 15)
				item.setItemId(item.getItemId().substring(0,15));
			if (item.getSubLibrary().length() > 5 || item.getSubLibrary().equals("") || item.getSubLibrary() == null || item.getSubLibrary().length() < 3)
				item.setSubLibrary("???");
			if (item.getItemStatus() == null)
				item.setItemStatus("???");
			if (item.getProcessStatus() == null)
				item.setProcessStatus("???");
		}
	}

}
