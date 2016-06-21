package fk.sp.aDa.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Created by ravi.gupta on 15/6/16.
 */
@Getter
@Setter
public class JoinRequestTables {

    private List<String> tableNames;
}
