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

    private List<JoinRequest> joinRequests;

    private List<JoinRequestCondition> joinRequestConditions;

    private JoinRequestTables joinRequestTables;

    private EmployeeRequest employeeRequest;

    private String pageNumber;

    private String pageSize;

    private String maxRows;

    private DatabaseName databaseName;

}
