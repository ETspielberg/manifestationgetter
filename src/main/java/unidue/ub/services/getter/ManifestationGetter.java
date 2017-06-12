package unidue.ub.services.getter;

import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;

import unidue.ub.media.monographs.Manifestation;

public class ManifestationGetter {
	
	private final String select = "select distinct substr(z30_rec_key,1,9) as docNumber from edu50.z30 where ( z30_call_no = ?) ";
	
	private final String like = "or ( z30_call_no like ?) ";
	
	private final String orderBy = "order by docNumber";
	
	private final String getEtat = "select distinct substr(z75_rec_key,1,9) as docNumber from edu50.z601, edu50.z75 where z601_rec_key_2 = z75_rec_key_2 and z601_rec_key like ? and z601_type = 'INV'";
	
	private final String getByNotation = "select distinct substr(z30_rec_key,1,9) as docNumber from edu50.z30 where ( z30_call_no like ?)";
	
	private JdbcTemplate jdbcTemplate;
	
	private final static String SUB_D = "_d";
	
	public ManifestationGetter(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	
	public List<Manifestation> getDocumentsByShelfmark(String identifier, boolean exact) {
		List<Manifestation> manifestations = new ArrayList<>();
		String suffix = "";
		if (identifier.endsWith(SUB_D)) {
			identifier = identifier.substring(0, identifier.length() - SUB_D.length());
			suffix = SUB_D;
		}
		identifier = identifier.toUpperCase();
		String query = exact ? (select + like + orderBy) : (select + like + like + like + orderBy);
		if (exact) 
			manifestations.addAll(jdbcTemplate.query(query, new Object[]{identifier + suffix,identifier + "+%" + suffix},(rs, rowNum) -> new Manifestation(rs.getString("docNumber"))));
		else
			manifestations.addAll(jdbcTemplate.query(query, new Object[]{identifier + suffix,identifier + "+%" + suffix,identifier + "-%" + suffix,identifier + "(%" + suffix},(rs, rowNum) -> new Manifestation(rs.getString("docNumber"))));
		return manifestations;
	}
	
	public List<Manifestation> getDocumentsByEtat(String identifier, boolean exact) {
		String query = getEtat + orderBy;
		List<Manifestation> manifestations = new ArrayList<>();
		manifestations.addAll(jdbcTemplate.query(query, new Object[]{identifier},(rs, rowNum) -> new Manifestation(rs.getString("docNumber"))));
		return manifestations;
	}
		
	public List<Manifestation> getDocumentsByNotation(String identifier) {
		List<Manifestation> manifestations = new ArrayList<>();
		manifestations.addAll(jdbcTemplate.query(getByNotation, new Object[]{identifier},(rs, rowNum) -> new Manifestation(rs.getString("docNumber"))));
		return manifestations;
	}


}
