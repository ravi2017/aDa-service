package fk.sp.aDa.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Created by ravi.gupta on 15/6/16.
 */
@Getter
@Setter
public class JoinRequestCondition {

    private String tableOneName;

    private String tableTwoName;

    private String tableOneColumnName;

    private String tableTwoColumnName;

}
