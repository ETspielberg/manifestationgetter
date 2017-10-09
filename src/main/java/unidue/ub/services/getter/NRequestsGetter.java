package unidue.ub.services.getter;

import org.springframework.jdbc.core.JdbcTemplate;
import unidue.ub.media.analysis.Nrequests;

import java.util.ArrayList;
import java.util.List;

/**
 * Retrieves the number of requests from the Aleph database.
 * 
 * @author Eike Spielberg
 * @version 1
 */
public class NRequestsGetter {

	private final String psGetNRequestsSingle  = "select z30reckey, z30_call_no, anzahl_vor, anzahl_ex, anzahl_aus, anz_auslstatus, quotient from edu50.z30, edu50.vortab_1a, edu50.vortab_2,edu50.vortab_3, edu50.vortab_4, edu50.vortab_5 where z37reckey = z30reckey and z30reckey = z36reckey and z37reckey = z30reckey2 and z37reckey = z30reckey3 and z30reckey = substr(z30_rec_key,1,9) and substr(z30_rec_key,10,6) = '000010' and anzahl_vor > 1 and z30_call_no like ?";

	private final String  psGetNRequestsMulti  = "select z30reckey, z30_call_no, anzahl_vor, anzahl_ex, anzahl_aus, anz_auslstatus, quotient from edu50.z30, edu50.vortab_1a, edu50.vortab_2,edu50.vortab_3, edu50.vortab_4, edu50.vortab_5 where z37reckey = z30reckey and z30reckey = z36reckey and z37reckey = z30reckey2 and z37reckey = z30reckey3 and z30reckey = substr(z30_rec_key,1,9) and substr(z30_rec_key,10,6) = '000010' and anzahl_vor > 1 and z30_call_no between ? and ? order by z30_call_no";;

	private final String  psGetNRequestsAll = "select z30reckey, z30_call_no, anzahl_vor, anzahl_ex, anzahl_aus, anz_auslstatus, quotient from edu50.z30, edu50.vortab_1a, edu50.vortab_2,edu50.vortab_3, edu50.vortab_4, edu50.vortab_5 where z37reckey = z30reckey and z30reckey = z36reckey and z37reckey = z30reckey2 and z37reckey = z30reckey3 and z30reckey = substr(z30_rec_key,1,9) and substr(z30_rec_key,10,6) = '000010'".toUpperCase();
    
    private JdbcTemplate jdbcTemplate;

	private List<Nrequests> nrequests;

	public NRequestsGetter(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	/**
	 * retrieves the number of Requests for the documents in the region defined
	 * by the notations
	 * 
	 * @param notations
	 *            a region of notations separated by '-'
	 * @return nRequests the list of <code>NRequest</code>
	 */
	public List<Nrequests> getNrequestsForRange(String notations)  {
		nrequests = new ArrayList<>();
		nrequests.addAll(jdbcTemplate.query(psGetNRequestsMulti, new Object[]{notations.substring(0, notations.indexOf("-")).trim() + "%" ,notations.substring(0, notations.indexOf("-")).trim()+ "%"},
				(rs, rowNum) -> new Nrequests(rs.getString("z30reckey"), rs.getString("z30_call_no"),
						rs.getDouble("quotient"), rs.getInt("anzahl_ex"), rs.getInt("anz_auslstatus"),
						rs.getInt("anzahl_vor"), rs.getInt("anzahl_aus"))));
		return nrequests;
	}

	/**
	 * retrieves the number of Requests for the documents with the defined
	 * notations
	 * 
	 * @param notation
	 *            a single notation
	 * @return nRequest the list of <code>NRequest</code>
	 */
	public List<Nrequests> getNrequestsForNotation(String notation) {
		nrequests = new ArrayList<>();
		nrequests.addAll(jdbcTemplate.query(psGetNRequestsSingle, new Object[]{notation + "%"},
				(rs, rowNum) -> new Nrequests(rs.getString("z30reckey"), rs.getString("z30_call_no"),
						rs.getDouble("quotient"), rs.getInt("anzahl_ex"), rs.getInt("anz_auslstatus"),
						rs.getInt("anzahl_vor"), rs.getInt("anzahl_aus"))));
		return nrequests;
	}
	
	/**
	 * retrieves all requests
	 * 
	 * @return the list of <code>NRequest</code>
	 */
	public List<Nrequests> getAllNrequests() {
		nrequests = new ArrayList<>();
		nrequests.addAll(jdbcTemplate.query(psGetNRequestsAll, new Object[]{},
				(rs, rowNum) -> new Nrequests(rs.getString("z30reckey"), rs.getString("z30_call_no"),
						rs.getDouble("quotient"), rs.getInt("anzahl_ex"), rs.getInt("anz_auslstatus"),
						rs.getInt("anzahl_vor"), rs.getInt("anzahl_aus"))));
		return nrequests;
    }

}
