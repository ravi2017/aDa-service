package fk.sp.aDa.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * Created by ravi.gupta on 21/6/16.
 */
@Getter
@Setter
public class WhereCondition {

    private String whereConditionColumn;

    private String whereConditionOperator;

    private String tableName;

    private String whereConditionValue;

    private String connectorOperator;

    private String leftParenthesis;

    private String rightParenthesis;

    private String betweenRightValue;

    private String betweenLeftValue;

    private String databaseName;

    private List<String> inList;

}
