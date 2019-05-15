package unidue.ub.services.getter.getter;

import org.springframework.jdbc.core.JdbcTemplate;
import unidue.ub.services.getter.model.RawJournalInfo;

import java.util.ArrayList;
import java.util.List;

public class JournalInfoGetter {

    private JdbcTemplate jdbcTemplate;

    private final String getJournalInfo = "select z13_title, z13_author, z13_isbn_issn, z13_imprint from edu50.z103, edu01.z13 where  z103_lkr_library = 'EDU50' and substr(?,1,9) = substr(z103_rec_key,6,9) and substr(z103_rec_key_1,6,9) = z13_rec_key";

    public JournalInfoGetter(JdbcTemplate jdbcTemplate) {this.jdbcTemplate = jdbcTemplate;}

    public RawJournalInfo getJournalInfoForInternalId(String internalId) {
        List<RawJournalInfo> rawJournalInfo = new ArrayList<>(jdbcTemplate.query(getJournalInfo, new Object[]{internalId},
                (rs, rowNum) -> new RawJournalInfo(rs.getString("z13_isbn_issn"), rs.getString("z13_title"),
                        rs.getString("z13_imprint"), rs.getString("z13_author"))));
        return rawJournalInfo.get(0);
    }
}
