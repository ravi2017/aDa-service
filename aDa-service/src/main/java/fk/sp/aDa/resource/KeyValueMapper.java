package fk.sp.aDa.resource;

/**
 * Created by ravi.gupta on 14/6/16.
 */

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

public class KeyValueMapper implements ResultSetMapper<Object> {
    public final Map<String, String> map = new HashMap<String, String>();

    @Override
    public Object map(int index, ResultSet rs, StatementContext ctx) throws SQLException {
        map.put(rs.getString(1), rs.getString(2));
        return null;
    }
}
