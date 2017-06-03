package unidue.ub.services.manifestgetter;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import unidue.ub.media.monographs.Manifestation;

@Controller
@RefreshScope
@RequestMapping("/manifestgetter")
@CrossOrigin(origins="http://localhost")
public class ManifestGetterControler {
	
	@Autowired
    JdbcTemplate jdbcTemplate;

	private final String select = "select distinct substr(z30_rec_key,1,9) as docNumber from edu50.z30 where ( z30_call_no = ?) ";
	
	private final String like = "or ( z30_call_no like ?) ";
	
	private final String orderBy = "order by docNumber";

	private final String selectNotation = "select distinct substr(z30_rec_key,1,9) as docNumber from edu50.z30 where ( z30_call_no like ?)";
	
	private final String selectEtat = "select distinct substr(z75_rec_key,1,9) as docNumber from edu50.z601, edu50.z75 where z601_rec_key_2 = z75_rec_key_2 and z601_rec_key like ? and z601_type = 'INV'";

	private final static String SUB_D = "_d";
	
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
			query = selectEtat + orderBy;
		case "notation":
			query = selectNotation + orderBy;
		}
		jdbcTemplate.query(query, new Object[]{identifier},(rs, rowNum) -> new Manifestation(rs.getString("docNumber")));
		return ResponseEntity.ok(manifests);
	}

}
