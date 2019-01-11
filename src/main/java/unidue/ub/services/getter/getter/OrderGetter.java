package unidue.ub.services.getter.getter;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import unidue.ub.services.getter.model.RawOrder;

import java.util.ArrayList;
import java.util.List;

@Component
public class OrderGetter {

    private JdbcTemplate jdbcTemplate;

    private final String getSerialsInvoices = "select z68_rec_key, z68_order_number, z68_method_of_aquisition, z68_library_note, z68_vendor_code, z68_subscription_date_from, z68_sub_library , z68_e_currency , z68_unit_price from edu50.z68 where z68_order_status = 'SV' and z68_order_type = 'S'";

    private final String getSerialsInvoicesByIssn = "select z68_rec_key, z68_order_number, z68_method_of_aquisition, z68_library_note, z68_vendor_code, z68_subscription_date_from, z68_sub_library , z68_e_currency , z68_unit_price from edu50.z68, edu01.z13, edu50.z103 where z68_order_status = 'SV' and substr(z68_rec_key,1,9) = substr(z103_rec_key,6,9) and substr(z103_rec_key_1,6,9) = z13_rec_key and z13_isbn_issn = ?";

    public OrderGetter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<RawOrder> getAllOrders() {
        return new ArrayList<>(jdbcTemplate.query(getSerialsInvoices, new Object[]{},
                (rs, rowNum) -> new RawOrder(rs.getString("z68_rec_key"), rs.getString("z68_order_number"),
                        rs.getString("z68_method_of_aquisition"), rs.getString("z68_library_note"), rs.getString("z68_vendor_code"),
                        String.valueOf(rs.getInt("z68_subscription_date_from")), rs.getString("z68_sub_library"),
                        rs.getString("z68_e_currency"), rs.getString("z68_unit_price"))));
    }


    public List<RawOrder> findOrdersByOrderNumber(String orderNumber) {
        String byOrderNumber = " and z68_order_number like ?";
        return new ArrayList<>(jdbcTemplate.query(getSerialsInvoices + byOrderNumber, new Object[]{orderNumber + "%"},
                (rs, rowNum) -> new RawOrder(rs.getString("z68_rec_key"), rs.getString("z68_order_number").trim(),
                        rs.getString("z68_method_of_aquisition").trim(), rs.getString("z68_library_note").trim(), rs.getString("z68_vendor_code").trim(),
                        String.valueOf(rs.getInt("z68_subscription_date_from")), rs.getString("z68_sub_library").trim(),
                        rs.getString("z68_e_currency").trim(), rs.getString("z68_unit_price").trim())));
    }

    public List<RawOrder> findOrdersByIssn(String issn) {
        return new ArrayList<>(jdbcTemplate.query(getSerialsInvoicesByIssn, new Object[]{issn},
                (rs, rowNum) -> new RawOrder(rs.getString("z68_rec_key"), rs.getString("z68_order_number").trim(),
                        rs.getString("z68_method_of_aquisition").trim(), rs.getString("z68_library_note").trim(), rs.getString("z68_vendor_code").trim(),
                        String.valueOf(rs.getInt("z68_subscription_date_from")), rs.getString("z68_sub_library").trim(),
                        rs.getString("z68_e_currency"), rs.getString("z68_unit_price"))));
    }
}