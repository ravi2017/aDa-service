package fk.sp.aDa.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Created by ravi.gupta on 16/6/16.
 */
@Getter
@Setter
public class GroupBy {

    private String groupByColumnName;

    private String groupByTableName;
}
