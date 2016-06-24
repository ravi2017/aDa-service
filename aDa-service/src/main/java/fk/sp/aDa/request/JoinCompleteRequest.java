package fk.sp.aDa.request;

import io.dropwizard.jackson.JsonSnakeCase;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Created by ravi.gupta on 15/6/16.
 */
@Getter
@Setter
@JsonSnakeCase
public class JoinCompleteRequest {

    private List<SelectedColumns> selectedColumnsList;

    private List<JoinRequestCondition> joinRequestConditionList;

    private JoinRequestTables joinRequestTables;

}
