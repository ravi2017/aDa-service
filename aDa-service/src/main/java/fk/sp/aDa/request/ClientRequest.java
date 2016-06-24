package fk.sp.aDa.request;

import javafx.util.Pair;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * Created by ravi.gupta on 30/5/16.
 */
@Getter
@Setter
public class ClientRequest {

    private JoinCompleteRequest joinCompleteRequest;

    private List<WhereCondition> whereConditionList;

    private List<GroupBy> groupByList;

    private List<Having> havingList;

    private List<OrderBy> orderByList;

    private String pageNumber;

    private String pageSize;

    private String maxRows;

    private  String databaseName;

}
