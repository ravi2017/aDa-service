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
public class EmployeeRequest {

    private List<String> selectedColumns;

    private Map<String, Object> whereConditionColumns;

    private List<String> whereConditionOperators;

    private List<String> selectedTableName;

    private List<String> connectorOperator;

    private List<String> leftParenthesis;

    private List<String> rightParenthesis;

    private List<WhereCondition> whereConditionList;

    private List<GroupBy> groupByList;

    private List<Having> havingList;

    private List<OrderBy> orderByList;

    private  DatabaseName databaseName;

}
