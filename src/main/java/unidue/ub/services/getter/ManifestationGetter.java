package unidue.ub.services.getter;

import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;

import unidue.ub.media.monographs.Manifestation;

public class ManifestationGetter {
	
	private final String select = "select distinct substr(z30_rec_key,1,9) as titleId from edu50.z30 where ( z30_call_no = ?) ";
	
	private final String like = "or ( z30_call_no like ?) ";
	
	private final String orderBy = "order by titleId";
	
	private final String getEtat = "select distinct substr(z75_rec_key,1,9) as titleId from edu50.z601, edu50.z75 where z601_rec_key_2 = z75_rec_key_2 and z601_rec_key like ? and z601_type = 'INV'";
	
	private final String getByNotation = "select distinct substr(z30_rec_key,1,9) as titleId from edu50.z30 where ( z30_call_no like ?)";

	private final String getByOpenRequests = "select distinct substr(z37_rec_key,1,9) as titleId from edu50.z37 where z37_pickup_location != 'ILLDT' and (z37_end_request_date >(select to_char(sysdate, 'YYYYMMDD') from dual))";

	private final String getByBarcode = "select substr(z30_rec_key,1,9) as titleId from edu50.z30 where z30_barcode = ?";
	
	private JdbcTemplate jdbcTemplate;
	
	private final static String SUB_D = "_d";
	
	ManifestationGetter(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	
	List<Manifestation> getDocumentsByShelfmark(String identifier, boolean exact) {
		List<Manifestation> manifestations = new ArrayList<>();
		boolean hasSuffix = identifier.endsWith(SUB_D);
		String suffix = "";
		if (hasSuffix) {
			identifier = identifier.substring(0, identifier.length() - SUB_D.length());
			suffix = SUB_D;
		}
		identifier = identifier.toUpperCase();
		String query = exact ? (select + like + orderBy) : (select + like + like + like + orderBy);
		if (exact) 
			manifestations.addAll(jdbcTemplate.query(query, new Object[]{identifier + suffix,identifier + "+%" + suffix},(rs, rowNum) -> new Manifestation(rs.getString("titleId"))));
		else
			manifestations.addAll(jdbcTemplate.query(query, new Object[]{identifier + suffix,identifier + "+%" + suffix,identifier + "-%" + suffix,identifier + "(%" + suffix},(rs, rowNum) -> new Manifestation(rs.getString("titleId"))));
		return manifestations;
	}

	List<Manifestation> getDocumentsByOpenRequests() {
		List<Manifestation> manifestations = new ArrayList<>();
		manifestations.addAll(jdbcTemplate.query(getByOpenRequests, new Object[]{},(rs, rowNum) -> new Manifestation(rs.getString("titleId"))));
		return manifestations;
	}

	List<Manifestation> getManifestationsByBarcode(String barcode) {
		List<Manifestation> manifestations = new ArrayList<>();
		manifestations.addAll(jdbcTemplate.query(getByBarcode, new Object[]{barcode.toUpperCase().trim()},(rs, rowNum) -> new Manifestation(rs.getString("titleId"))));
		return manifestations;
	}


	List<Manifestation> getDocumentsByEtat(String identifier) {
		String query = getEtat + orderBy;
		List<Manifestation> manifestations = new ArrayList<>();
		manifestations.addAll(jdbcTemplate.query(query, new Object[]{identifier + "%"},(rs, rowNum) -> new Manifestation(rs.getString("titleId"))));
		return manifestations;
	}
		
	List<Manifestation> getDocumentsByNotation(String identifier) {
		List<Manifestation> manifestations = new ArrayList<>();
		manifestations.addAll(jdbcTemplate.query(getByNotation, new Object[]{identifier + "%"},(rs, rowNum) -> new Manifestation(rs.getString("titleId"))));
		return manifestations;
	}


}
