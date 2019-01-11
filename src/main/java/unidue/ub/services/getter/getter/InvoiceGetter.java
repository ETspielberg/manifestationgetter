package unidue.ub.services.getter.getter;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import unidue.ub.services.getter.model.RawInvoice;

import java.util.ArrayList;
import java.util.List;

@Component
public class InvoiceGetter {

    private JdbcTemplate jdbcTemplate;

    public InvoiceGetter(JdbcTemplate jdbcTemplate) {this.jdbcTemplate = jdbcTemplate; }

    private final String getSerialsInvoicesForInternalId = "select z75_rec_key, z75_rec_key_2, z75_i_credit_debit, z75_i_total_amount, z75_i_note, z75_i_date_from , z75_i_date_to, z77_i_date, z77_i_currency, z77_p_date, z77_p_amount from edu50.z75, edu50.z77 where z77_rec_key = substr(z75_rec_key_2,1,35) and z75_rec_key = ?";

    public List<RawInvoice> findInvoicesByInternalId(String internalId) {
        List<RawInvoice> rawInvoices = new ArrayList<>(jdbcTemplate.query(getSerialsInvoicesForInternalId, new Object[]{internalId},
                (rs, rowNum) -> new RawInvoice(rs.getString("z75_rec_key"), rs.getString("z77_i_date"),
                        rs.getString("z75_i_credit_debit"), rs.getString("z75_i_total_amount"), rs.getString("z75_i_note"),
                        rs.getString("z75_i_date_from"), rs.getString("z75_i_date_to"), rs.getString("z77_i_currency"), rs.getString("z77_i_date"), rs.getString("z77_p_amount"))));
        return rawInvoices;
    }
}
