package unidue.ub.services.getter;

import org.springframework.jdbc.core.JdbcTemplate;
import unidue.ub.services.getter.model.RawJournalprices;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Retrieves the prices for journals or journal collections from the Aleph database.
 * 
 * @author Jutta Kleinfeld, Eike Spielberg
 * @version 1
 */
public class JournalPriceGetter {

    private JdbcTemplate jdbcTemplate;

    private static final Pattern yearPattern = Pattern.compile("((19|20)\\d\\d)");

    private final String getPriceCollection = "select z68_rec_key, z68_vendor_code , z68_order_number, z75_i_total_amount, z75_i_date_from, z75_i_date_to, z75_i_note from edu50.z68, edu50.z75 where z68_rec_key = z75_rec_key and z68_order_number =?";

    private final String getPriceISSN = "select substr(z103_rec_key,6,9),  z75_i_total_amount, z75_i_date_from, z75_i_date_to, z75_i_note from edu50.z103, edu01.z13, edu50.z75 where substr(z103_rec_key_1,6,9) = z13_rec_key and substr(z103_rec_key_1,1,5) = 'EDU01' and  substr(z75_rec_key,1,9) = substr(z103_rec_key,6,9) and z13_isbn_issn = ?";

    public JournalPriceGetter(JdbcTemplate jdbcTemplate) throws SQLException {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Hashtable<Integer, Double> getJournalcollectionprices(String ordernumber) throws SQLException {
        List<RawJournalprices> rawJournalprices = new ArrayList<>();
        rawJournalprices
                .addAll(jdbcTemplate.query(getPriceCollection, new Object[]{ordernumber},
                        (rs, rowNum) -> new RawJournalprices(rs.getString("z36h_rec_key"), rs.getString("z68_vendor_code"),
                                rs.getString("z68_order_number"),
                                rs.getString("z75_i_total_amount"), rs.getString("z75_i_date_from"),
                                rs.getString("z75_i_date_to"), rs.getString("z75_i_note"))));
        Hashtable<Integer, Double> prices = new Hashtable<Integer, Double>();
        return prices;
        }

    /**
     * when queried with a single journal title, the prices from the Aleph database are retrieved and a list of journal titles for each year is returned. The price is set within each journal title.
     * 
     * @param issn
     *            the ISSN of an journal title
     * @return a list of journal title, each containing the price for a specific year
     * @exception SQLException exception querying the Aleph database 
     */
    public Hashtable<Integer, Double> getJournalPrice(String issn) throws SQLException {
        List<RawJournalprices> rawJournalprices = new ArrayList<>();
        rawJournalprices
                .addAll(jdbcTemplate.query(getPriceISSN, new Object[]{issn},
                        (rs, rowNum) -> new RawJournalprices(rs.getString("z36h_rec_key"), rs.getString("z68_vendor_code"),
                                rs.getString("z68_order_number"),
                                rs.getString("z75_i_total_amount"), rs.getString("z75_i_date_from"),
                                rs.getString("z75_i_date_to"), rs.getString("z75_i_note"))));
        Hashtable<Integer, Double> prices = new Hashtable<Integer, Double>();
        return prices;
    }
}
