package fk.sp.aDa.request;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by ravi.gupta on 16/6/16.
 */
@Getter
@Setter
public class Having {

    private String aggregateFunction;

    private String tableName;

    private String columnName;

    private String operatorType;

    private String betweenRightValue;

    private String betweenLeftValue;

    private String connectorType;

    private String operatorValue;

    private String leftParenthesis;

    private String rightParenthesis;

}
