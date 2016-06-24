package fk.sp.aDa.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * Created by ravi.gupta on 14/6/16.
 */
@Getter
@Setter
public class SelectedColumns {

    private List<String> selectedColumns;

    private String selectedTables;

}
