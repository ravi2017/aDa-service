package fk.sp.aDa.request;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by ravi.gupta on 16/6/16.
 */
@Getter
@Setter
public class OrderBy {

    private String aggregateFunction;

    private String tableName;

    private String columnName;

    private String sortType;
}
