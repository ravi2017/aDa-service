package fk.sp.aDa.resource;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import fk.sp.aDa.configuration.AdaConfiguration;
import fk.sp.aDa.db.repository.EmployeeRepository;
import fk.sp.aDa.request.*;
import org.omg.CORBA.Any;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.Query;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.sql.*;
import java.util.*;


import static org.glassfish.hk2.utilities.ServiceLocatorUtilities.bind;

/**
 * Created by ravi.gupta on 25/5/16.
 */
@Path("/database")
@Produces(MediaType.APPLICATION_JSON)
public class EmployeeResource {

    private EmployeeRepository employeeRepository;

    private AdaConfiguration adaConfiguration;

    private Handle handle;

    @Inject
    public EmployeeResource(EmployeeRepository employeeRepository,
                            AdaConfiguration adaConfiguration,
                            Handle handle) {
        this.adaConfiguration = adaConfiguration;
        this.employeeRepository = employeeRepository;
        this.handle = handle;
    }

    @GET
    @Path("/getTables")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public List<String> databaseDetails() throws Exception {
        Connection connection = null;
        List<String> tableNames = new ArrayList<String>();
        try {
            connection = handle.getConnection();
            // Gets the metadata of the database
            DatabaseMetaData dbmd = connection.getMetaData();
            String[] types = {"TABLE"};

            ResultSet rs = dbmd.getTables(null, null, "%", types);
            while (rs.next()) {
                String tableCatalog = rs.getString(1);
                String tableSchema = rs.getString(2);
                String tableName = rs.getString(3);
                //System.out.printf("%s - %s - %s%n", tableCatalog, tableSchema, tableName);
                tableNames.add(tableName);
            }
        } catch (SQLException e) {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        }
        return tableNames;
    }

    @POST
    @Path("/describeTableName")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    public List<String> tableDetails(String tableName, int code) throws Exception{
        Connection connection = null;
        Statement statement = null;
        List<String> columnNames = new ArrayList<String>();
        List<String> indexColumnNames = new ArrayList<String>();
        try {
            connection = handle.getConnection();
            DatabaseMetaData dbmd = connection.getMetaData();
            statement = connection.createStatement();
            String sql;
            sql = "select * from " + tableName;
            ResultSet rs = statement.executeQuery(sql);
            ResultSetMetaData rsmd = rs.getMetaData();
            ResultSet rs1 = dbmd.getIndexInfo(null, null, tableName, true, false);
            while(rs1.next()) {
                indexColumnNames.add(rs1.getString("COLUMN_NAME"));
            }
            for(int i=1; i<=rsmd.getColumnCount(); i++) {
                columnNames.add(rsmd.getColumnName(i));
            }
        } catch (SQLException e) {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        }
        if(code==0)
            return columnNames;
        else
            return indexColumnNames;
    }

    @POST
    @Path("/query")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public <Any> Any queryDetails(EmployeeRequest employeeRequest) {

        String columns = "";
        String whereCondition = "";
        int counter = 0;
        for (String a : employeeRequest.getSelectedColumns()) {
            if(counter == 0)
                columns = columns + a;
            else
                columns = columns + ", " + a;
            counter++;
        }
        counter = 0;
        for(String a: employeeRequest.getWhereConditionColumns().keySet()) {
            if(counter == 0)
                whereCondition = whereCondition + " where " + employeeRequest.getLeftParenthesis().get(counter) + a + " " + employeeRequest.getWhereConditionOperators().get(counter) + ":" +
                        a + employeeRequest.getRightParenthesis().get(counter);
            else
                whereCondition = whereCondition + " " + employeeRequest.getConnectorOperator().get(counter-1)+ " " + employeeRequest.getLeftParenthesis().get(counter) + a + " " + employeeRequest.getWhereConditionOperators().get(counter) + ":" + a + employeeRequest.getRightParenthesis().get(counter);
            counter++;
        }
        //System.out.println("select " + columns  + " From " + employeeRequest.getSelectedTableName() + whereCondition);
        String groupBy = "";
        if(employeeRequest.getGroupByList().size()>0) {
            for(int i=0;i<employeeRequest.getGroupByList().size();i++) {
                if(i==0) {
                    groupBy = groupBy + " Group By " + employeeRequest.getGroupByList().get(i).getGroupByTableName() + "." + employeeRequest.getGroupByList().get(i).getGroupByColumnName();
                }
                else {
                    groupBy = groupBy + "," + employeeRequest.getGroupByList().get(i).getGroupByTableName() + "." + employeeRequest.getGroupByList().get(i).getGroupByColumnName();
                }
            }
        }
        String having = "";
        if(employeeRequest.getHavingList().size()>0) {
            for(int i=0;i<employeeRequest.getHavingList().size();i++) {
                if(i==0) {
                    having  = having + " Having " + employeeRequest.getHavingList().get(i).getLeftParenthesis() + employeeRequest.getHavingList().get(i).getAggregateFunction() + "(" + employeeRequest.getHavingList().get(i).getTableName() + "." + employeeRequest.getHavingList().get(i).getColumnName() + ")" + " ";
                    if(employeeRequest.getHavingList().get(i).getOperatorType().equals("BETWEEN")) {
                        having = having + "BETWEEN " + employeeRequest.getHavingList().get(i).getBetweenLeftValue() + " AND " + employeeRequest.getHavingList().get(i).getBetweenRightValue() + employeeRequest.getHavingList().get(i).getRightParenthesis();
                    }
                    else  {
                        having = having + employeeRequest.getHavingList().get(i).getOperatorType() + employeeRequest.getHavingList().get(i).getOperatorValue() + employeeRequest.getHavingList().get(i).getRightParenthesis();
                    }
                }
                else {
                    having  = having + " " + employeeRequest.getHavingList().get(i).getConnectorType() + " " + employeeRequest.getHavingList().get(i).getLeftParenthesis() + employeeRequest.getHavingList().get(i).getAggregateFunction() + "(" + employeeRequest.getHavingList().get(i).getTableName() + "." + employeeRequest.getHavingList().get(i).getColumnName() + ")" + " ";
                    if(employeeRequest.getHavingList().get(i).getOperatorType().equals("BETWEEN")) {
                        having = having + "BETWEEN " + employeeRequest.getHavingList().get(i).getBetweenLeftValue() + " AND " + employeeRequest.getHavingList().get(i).getBetweenRightValue() + employeeRequest.getHavingList().get(i).getRightParenthesis();
                    }
                    else  {
                        having = having + employeeRequest.getHavingList().get(i).getOperatorType() + employeeRequest.getHavingList().get(i).getOperatorValue() + employeeRequest.getHavingList().get(i).getRightParenthesis();
                    }
                }
            }
        }
        String orderBy = "";
        if(employeeRequest.getOrderByList().size()>0) {
            for(int i=0;i<employeeRequest.getOrderByList().size();i++) {
                if(i==0) {
                    orderBy = orderBy + " Order By " + employeeRequest.getOrderByList().get(i).getAggregateFunction() + "(" + employeeRequest.getOrderByList().get(i).getTableName() + "." + employeeRequest.getOrderByList().get(i).getColumnName() + ")" + " " + employeeRequest.getOrderByList().get(i).getSortType();
                }
                else {
                    orderBy = orderBy + "," + employeeRequest.getOrderByList().get(i).getAggregateFunction() + "(" + employeeRequest.getOrderByList().get(i).getTableName() + "." + employeeRequest.getOrderByList().get(i).getColumnName() + ")" + " " +employeeRequest.getOrderByList().get(i).getSortType();
                }
            }
        }

        System.out.println("select " + columns + " From " + employeeRequest.getSelectedTableName() + whereCondition + groupBy + having + orderBy);
        Query<Map<String, Object>> name = handle.createQuery("select " + columns + " From " + employeeRequest.getSelectedTableName() + whereCondition + groupBy + having + orderBy);
        for(String a: employeeRequest.getWhereConditionColumns().keySet()) {
            name.bind(a, employeeRequest.getWhereConditionColumns().get(a));
        }
        return (Any) name.list();
    }



    @POST
    @Path("/join")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public <Any> Any joinDetails(JoinCompleteRequest joinCompleteRequest) throws  Exception{
            List<String> tableNames = new ArrayList<String>();
            List<String> allTableNames = new ArrayList<String>();
            allTableNames = databaseDetails();
            tableNames = joinCompleteRequest.getJoinRequestTables().getTableNames();
            Map<String,List<String>> tableColumnInformation = new HashMap<String,List<String>>();
            Map<String,List<String>> tableIndexInformation = new HashMap<String,List<String>>();
            for(int i=0;i<tableNames.size();i++) {
                tableColumnInformation.put(tableNames.get(i), tableDetails(tableNames.get(i),0));
                tableIndexInformation.put(tableNames.get(i), tableDetails(tableNames.get(i),1));
                //System.out.println(tableInformation.get(tableNames.get(i)).size());
            }
            int queryPossible = 0;
            Connection connection = null;
            Statement statement = null;
            String sql  = "";
            String columns = "";
            int counter = 0;
            for(int i=0; i< joinCompleteRequest.getJoinRequests().size();i++) {
                if (!tableValidator(joinCompleteRequest.getJoinRequests().get(i).getSelectedTables(), allTableNames)) {
                    queryPossible = 1;
                }
                    for (int j = 0; j < joinCompleteRequest.getJoinRequests().get(i).getSelectedColumns().size(); j++) {
                        if (i == 0 && j == 0) {
                            if(!columnValidator(joinCompleteRequest.getJoinRequests().get(i).getSelectedTables(), joinCompleteRequest.getJoinRequests().get(i).getSelectedColumns().get(j), tableColumnInformation))
                                queryPossible = 1;
                            columns = columns + joinCompleteRequest.getJoinRequests().get(i).getSelectedTables() + "." + joinCompleteRequest.getJoinRequests().get(i).getSelectedColumns().get(j);
                            }
                            else {
                            if(!columnValidator(joinCompleteRequest.getJoinRequests().get(i).getSelectedTables(),joinCompleteRequest.getJoinRequests().get(i).getSelectedColumns().get(j),tableColumnInformation)) {
                                queryPossible = 1;
                            }
                            columns = columns + "," + joinCompleteRequest.getJoinRequests().get(i).getSelectedTables() + "." + joinCompleteRequest.getJoinRequests().get(i).getSelectedColumns().get(j);
                        }
                    }
                }

            sql = sql + " select " + columns + " From ";
            String joinCondition = "";
            for(int i=0; i< joinCompleteRequest.getJoinRequestTables().getTableNames().size();i++) {
                if (i == 0) {
                    if (!tableValidator(joinCompleteRequest.getJoinRequestTables().getTableNames().get(i), allTableNames)) {
                        queryPossible = 1;
                    }
                    sql = sql + joinCompleteRequest.getJoinRequestTables().getTableNames().get(i);
                } else {
                    if (!tableValidator(joinCompleteRequest.getJoinRequestTables().getTableNames().get(i), allTableNames)) {
                        queryPossible = 1;
                    }
                    sql = sql + " join " + joinCompleteRequest.getJoinRequestTables().getTableNames().get(i) + " on ";
                    for (int j = 0; j < joinCompleteRequest.getJoinRequestConditions().size(); j++) {
                        if (joinCompleteRequest.getJoinRequestConditions().get(j).getTableTwoName().equals(joinCompleteRequest.getJoinRequestTables().getTableNames().get(i))) {
                            ;
                            for (int k = 0; k <= i; k++) {
                                if (joinCompleteRequest.getJoinRequestConditions().get(j).getTableOneName().equals(joinCompleteRequest.getJoinRequestTables().getTableNames().get(k))) {
                                    if (!tableValidator(joinCompleteRequest.getJoinRequestConditions().get(j).getTableOneName(), allTableNames) && tableValidator(joinCompleteRequest.getJoinRequestConditions().get(j).getTableTwoName(), allTableNames) && columnValidator(joinCompleteRequest.getJoinRequestConditions().get(j).getTableOneName(), joinCompleteRequest.getJoinRequestConditions().get(j).getTableOneColumnName(), tableColumnInformation) && columnValidator(joinCompleteRequest.getJoinRequestConditions().get(j).getTableTwoName(), joinCompleteRequest.getJoinRequestConditions().get(j).getTableTwoColumnName(), tableColumnInformation)) {
                                        queryPossible = 1;
                                    }
                                    if (k == 0)
                                        sql = sql + joinCompleteRequest.getJoinRequestConditions().get(j).getTableOneName() + "." + joinCompleteRequest.getJoinRequestConditions().get(j).getTableOneColumnName() + "=" + joinCompleteRequest.getJoinRequestConditions().get(j).getTableTwoName() + "." + joinCompleteRequest.getJoinRequestConditions().get(j).getTableTwoColumnName();
                                    else
                                        sql = sql + " and " + joinCompleteRequest.getJoinRequestConditions().get(j).getTableOneName() + "." + joinCompleteRequest.getJoinRequestConditions().get(j).getTableOneColumnName() + "=" + joinCompleteRequest.getJoinRequestConditions().get(j).getTableTwoName() + "." + joinCompleteRequest.getJoinRequestConditions().get(j).getTableTwoColumnName();
                                }
                            }
                        }
                    }
                }
            }

            String whereCondition = "";
            counter = 0;
            for(int i=0;i<joinCompleteRequest.getEmployeeRequest().getWhereConditionList().size();i++) {
                if (!(tableValidator(joinCompleteRequest.getEmployeeRequest().getWhereConditionList().get(i).getTableName(), allTableNames) && columnValidator(joinCompleteRequest.getEmployeeRequest().getWhereConditionList().get(i).getTableName(), joinCompleteRequest.getEmployeeRequest().getWhereConditionList().get(i).getWhereConditionColumn(), tableColumnInformation) && isLeftParenthesis(joinCompleteRequest.getEmployeeRequest().getWhereConditionList().get(i).getLeftParenthesis()) && isRightParenthesis(joinCompleteRequest.getEmployeeRequest().getWhereConditionList().get(i).getRightParenthesis()))) {
                    //System.out.println("here");
                    queryPossible = 1;
                }
                if (counter == 0) {
                    if(joinCompleteRequest.getEmployeeRequest().getWhereConditionList().get(i).getWhereConditionOperator().equals("BETWEEN")){
                        whereCondition = whereCondition + "where" + joinCompleteRequest.getEmployeeRequest().getWhereConditionList().get(i).getLeftParenthesis() + joinCompleteRequest.getEmployeeRequest().getWhereConditionList().get(i).getTableName() + "." + joinCompleteRequest.getEmployeeRequest().getWhereConditionList().get(i).getWhereConditionColumn() + " " + "BETWEEN " + joinCompleteRequest.getEmployeeRequest().getWhereConditionList().get(i).getBetweenLeftValue() + " and " + joinCompleteRequest.getEmployeeRequest().getWhereConditionList().get(i).getBetweenRightValue() + joinCompleteRequest.getEmployeeRequest().getWhereConditionList().get(i).getRightParenthesis();
                    }
                    else {
                        whereCondition = whereCondition + " where " + joinCompleteRequest.getEmployeeRequest().getWhereConditionList().get(i).getLeftParenthesis() + joinCompleteRequest.getEmployeeRequest().getWhereConditionList().get(i).getTableName() + "." + joinCompleteRequest.getEmployeeRequest().getWhereConditionList().get(i).getWhereConditionColumn() + " " + joinCompleteRequest.getEmployeeRequest().getWhereConditionList().get(i).getWhereConditionOperator() + ":" +
                                joinCompleteRequest.getEmployeeRequest().getWhereConditionList().get(i).getWhereConditionColumn() + joinCompleteRequest.getEmployeeRequest().getWhereConditionList().get(i).getRightParenthesis();
                    }
                }
                else {
                    if(joinCompleteRequest.getEmployeeRequest().getWhereConditionList().get(i).getWhereConditionOperator().equals("BETWEEN")) {
                        whereCondition = whereCondition + " " + joinCompleteRequest.getEmployeeRequest().getWhereConditionList().get(i).getConnectorOperator() + " " +joinCompleteRequest.getEmployeeRequest().getWhereConditionList().get(i).getLeftParenthesis() + joinCompleteRequest.getEmployeeRequest().getWhereConditionList().get(i).getTableName() + "." + joinCompleteRequest.getEmployeeRequest().getWhereConditionList().get(i).getWhereConditionColumn() + " " + "BETWEEN " + joinCompleteRequest.getEmployeeRequest().getWhereConditionList().get(i).getBetweenLeftValue() + " and " + joinCompleteRequest.getEmployeeRequest().getWhereConditionList().get(i).getBetweenRightValue() + joinCompleteRequest.getEmployeeRequest().getWhereConditionList().get(i).getRightParenthesis();
                    }
                    else {
                        whereCondition = whereCondition + " " + joinCompleteRequest.getEmployeeRequest().getWhereConditionList().get(i).getConnectorOperator() + " " + joinCompleteRequest.getEmployeeRequest().getWhereConditionList().get(i).getLeftParenthesis() + " " + joinCompleteRequest.getEmployeeRequest().getWhereConditionList().get(i).getTableName() + "." + joinCompleteRequest.getEmployeeRequest().getWhereConditionList().get(i).getWhereConditionColumn() + " " + joinCompleteRequest.getEmployeeRequest().getWhereConditionList().get(i).getWhereConditionOperator() + ":" + joinCompleteRequest.getEmployeeRequest().getWhereConditionList().get(i).getWhereConditionColumn() + joinCompleteRequest.getEmployeeRequest().getWhereConditionList().get(i).getRightParenthesis();
                    }
                }
                counter++;
            }
            sql = sql + whereCondition;

            String groupBy = "";
            if(joinCompleteRequest.getEmployeeRequest().getGroupByList().size()>0) {
                for(int i=0;i<joinCompleteRequest.getEmployeeRequest().getGroupByList().size();i++) {
                    if(!(tableValidator(joinCompleteRequest.getEmployeeRequest().getGroupByList().get(i).getGroupByTableName(),allTableNames) && columnValidator(joinCompleteRequest.getEmployeeRequest().getGroupByList().get(i).getGroupByTableName(),joinCompleteRequest.getEmployeeRequest().getGroupByList().get(i).getGroupByColumnName(),tableColumnInformation))) {
                        //System.out.println("here");
                        queryPossible = 1;
                    }
                    if (i == 0) {
                        groupBy = groupBy + " Group By " + joinCompleteRequest.getEmployeeRequest().getGroupByList().get(i).getGroupByTableName() + "." + joinCompleteRequest.getEmployeeRequest().getGroupByList().get(i).getGroupByColumnName();
                    } else {
                        groupBy = groupBy + "," + joinCompleteRequest.getEmployeeRequest().getGroupByList().get(i).getGroupByTableName() + "." + joinCompleteRequest.getEmployeeRequest().getGroupByList().get(i).getGroupByColumnName();
                    }
                }
            }

            String having = "";
            if(joinCompleteRequest.getEmployeeRequest().getHavingList().size()>0) {
                for(int i=0;i<joinCompleteRequest.getEmployeeRequest().getHavingList().size();i++) {
                    if(!(tableValidator(joinCompleteRequest.getEmployeeRequest().getHavingList().get(i).getTableName(),allTableNames) && columnValidator(joinCompleteRequest.getEmployeeRequest().getHavingList().get(i).getTableName(),joinCompleteRequest.getEmployeeRequest().getHavingList().get(i).getColumnName(),tableColumnInformation) && isRightParenthesis(joinCompleteRequest.getEmployeeRequest().getHavingList().get(i).getRightParenthesis()) && isLeftParenthesis(joinCompleteRequest.getEmployeeRequest().getHavingList().get(i).getLeftParenthesis()))) {
                        queryPossible = 1;
                    }
                    if (i == 0) {
                        having = having + " Having " + joinCompleteRequest.getEmployeeRequest().getHavingList().get(i).getLeftParenthesis() + joinCompleteRequest.getEmployeeRequest().getHavingList().get(i).getAggregateFunction() + "(" + joinCompleteRequest.getEmployeeRequest().getHavingList().get(i).getTableName() + "." + joinCompleteRequest.getEmployeeRequest().getHavingList().get(i).getColumnName() + ")" + " ";
                        if (joinCompleteRequest.getEmployeeRequest().getHavingList().get(i).getOperatorType().equals("BETWEEN")) {
                            having = having + "BETWEEN " + joinCompleteRequest.getEmployeeRequest().getHavingList().get(i).getBetweenLeftValue() + " AND " + joinCompleteRequest.getEmployeeRequest().getHavingList().get(i).getBetweenRightValue() + joinCompleteRequest.getEmployeeRequest().getHavingList().get(i).getRightParenthesis();
                        } else {
                            having = having + joinCompleteRequest.getEmployeeRequest().getHavingList().get(i).getOperatorType() + joinCompleteRequest.getEmployeeRequest().getHavingList().get(i).getOperatorValue() + joinCompleteRequest.getEmployeeRequest().getHavingList().get(i).getRightParenthesis();
                        }
                    } else {
                        having = having + " " + joinCompleteRequest.getEmployeeRequest().getHavingList().get(i).getConnectorType() + " " + joinCompleteRequest.getEmployeeRequest().getHavingList().get(i).getLeftParenthesis() + joinCompleteRequest.getEmployeeRequest().getHavingList().get(i).getAggregateFunction() + "(" + joinCompleteRequest.getEmployeeRequest().getHavingList().get(i).getTableName() + "." + joinCompleteRequest.getEmployeeRequest().getHavingList().get(i).getColumnName() + ")" + " ";
                        if (joinCompleteRequest.getEmployeeRequest().getHavingList().get(i).getOperatorType().equals("BETWEEN")) {
                            having = having + "BETWEEN " + joinCompleteRequest.getEmployeeRequest().getHavingList().get(i).getBetweenLeftValue() + " AND " + joinCompleteRequest.getEmployeeRequest().getHavingList().get(i).getBetweenRightValue() + joinCompleteRequest.getEmployeeRequest().getHavingList().get(i).getRightParenthesis();
                        } else {
                            having = having + joinCompleteRequest.getEmployeeRequest().getHavingList().get(i).getOperatorType() + joinCompleteRequest.getEmployeeRequest().getHavingList().get(i).getOperatorValue() + joinCompleteRequest.getEmployeeRequest().getHavingList().get(i).getRightParenthesis();
                        }
                    }
                }
            }

            String orderBy = "";
            if(joinCompleteRequest.getEmployeeRequest().getOrderByList().size()>0) {
                for(int i=0;i<joinCompleteRequest.getEmployeeRequest().getOrderByList().size();i++) {
                    if (!(tableValidator(joinCompleteRequest.getEmployeeRequest().getOrderByList().get(i).getTableName(), allTableNames) && columnValidator(joinCompleteRequest.getEmployeeRequest().getOrderByList().get(i).getTableName(), joinCompleteRequest.getEmployeeRequest().getOrderByList().get(i).getColumnName(), tableColumnInformation))) {
                        queryPossible = 1;
                    }
                    if (i == 0) {
                        orderBy = orderBy + " Order By " + joinCompleteRequest.getEmployeeRequest().getOrderByList().get(i).getAggregateFunction() + "(" + joinCompleteRequest.getEmployeeRequest().getOrderByList().get(i).getTableName() + "." + joinCompleteRequest.getEmployeeRequest().getOrderByList().get(i).getColumnName() + ")" + " " + joinCompleteRequest.getEmployeeRequest().getOrderByList().get(i).getSortType();
                    } else {
                        orderBy = orderBy + "," + joinCompleteRequest.getEmployeeRequest().getOrderByList().get(i).getAggregateFunction() + "(" + joinCompleteRequest.getEmployeeRequest().getOrderByList().get(i).getTableName() + "." + joinCompleteRequest.getEmployeeRequest().getOrderByList().get(i).getColumnName() + ")" + " " + joinCompleteRequest.getEmployeeRequest().getOrderByList().get(i).getSortType();
                    }
                }
            }

            sql = sql + groupBy + having + orderBy;
            System.out.println(sql);
            boolean validator;
            if(isParenthesisMatch(sql) && queryPossible==0 && isNum(joinCompleteRequest.getMaxRows()) && isNum(joinCompleteRequest.getPageNumber()) && isNum(joinCompleteRequest.getPageSize())){
                validator = true;
            }
            else{
                validator = false;
            }

            if(validator==true) {
                Query<Map<String, Object>> temp = handle.createQuery("explain " + sql);
                for (int i=0;i<joinCompleteRequest.getEmployeeRequest().getWhereConditionList().size();i++) {
                    if(!joinCompleteRequest.getEmployeeRequest().getWhereConditionList().get(i).getWhereConditionOperator().equals("BETWEEN"))
                        temp.bind(joinCompleteRequest.getEmployeeRequest().getWhereConditionList().get(i).getWhereConditionColumn(), joinCompleteRequest.getEmployeeRequest().getWhereConditionList().get(i).getWhereConditionValue());
                }

                System.out.println(temp.list().get(0).get("rows"));
                String sqlTimeCheck = sql;
                if(!joinCompleteRequest.getPageSize().isEmpty()) {
                    if(!joinCompleteRequest.getPageNumber().isEmpty()){
                        sql = sql + " LIMIT " + joinCompleteRequest.getPageNumber() + "," + joinCompleteRequest.getPageSize();
                    }
                    else{
                        sql = sql + "LIMIT" + joinCompleteRequest.getPageSize();
                    }
                }

                Query<Map<String, Object>> query = handle.createQuery(sql);
                for (int i=0;i<joinCompleteRequest.getEmployeeRequest().getWhereConditionList().size();i++) {
                    if(!joinCompleteRequest.getEmployeeRequest().getWhereConditionList().get(i).getWhereConditionOperator().equals("BETWEEN"))
                        query.bind(joinCompleteRequest.getEmployeeRequest().getWhereConditionList().get(i).getWhereConditionColumn(), joinCompleteRequest.getEmployeeRequest().getWhereConditionList().get(i).getWhereConditionValue());
                }

                Query<Map<String, Object>> queryTimeCheck = handle.createQuery(sqlTimeCheck);
                for (int i=0;i<joinCompleteRequest.getEmployeeRequest().getWhereConditionList().size();i++) {
                    if(!joinCompleteRequest.getEmployeeRequest().getWhereConditionList().get(i).getWhereConditionOperator().equals("BETWEEN"))
                        queryTimeCheck.bind(joinCompleteRequest.getEmployeeRequest().getWhereConditionList().get(i).getWhereConditionColumn(), joinCompleteRequest.getEmployeeRequest().getWhereConditionList().get(i).getWhereConditionValue());
                }
                queryTimeCheck.setQueryTimeout(20);
                float startTime,endTime;
                startTime = System.currentTimeMillis();
                queryTimeCheck.list();
                endTime = System.currentTimeMillis();
                if((endTime-startTime)>=20000) {
                    return (Any) "Too Much Time Can't Execute";
                }
                else
                {
                    if(!joinCompleteRequest.getMaxRows().isEmpty()){
                        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
                        result = query.setMaxRows(Integer.parseInt(joinCompleteRequest.getMaxRows())).list();
                        return (Any) result;
                    }
                    else {
                        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
                        result = query.list();
                        return (Any) result;
                    }
                }
            }
            else {
                System.out.println("Error!!!");
                return (Any) "Error!!";
            }
        }

        public boolean columnValidator(String tableName, String columnName, Map<String,List<String>> tableColumnInformation) {
            Boolean check = tableColumnInformation.containsKey(tableName);
            if(check) {
                for(int i=0;i<tableColumnInformation.get(tableName).size();i++) {
                    if(columnName.equals(tableColumnInformation.get(tableName).get(i)))
                        return true;
                }
                return false;
            }
            else {
                return false;
            }
        }

        public boolean tableValidator(String tableName, List<String> allTableNames) {
            for(int i=0;i<allTableNames.size();i++) {
                if(tableName.equals(allTableNames.get(i)))
                    return true;
            }
            return false;
        }

        public boolean isParenthesisMatch(String s) {

            Stack<Character> stack = new Stack<Character>();
            char c;
            for(int i=0;i<s.length();i++){
                c = s.charAt(i);
                if(c=='(')
                    stack.push(c);
                else if(c==')'){
                    if(stack.empty()){
                        return false;
                    }
                    else if(stack.peek()=='('){
                        stack.pop();
                    }
                    else
                        return false;
                }
            }
            return stack.empty();
        }

        public boolean isNum(String s) {
            if(s.contains("[0-9]+") || s.isEmpty()){
                return true;
            }
            else {
                return false;
            }
        }

        public boolean isRightParenthesis(String s) {
            if(s.matches("^[)]+$") || s.isEmpty()) {
                return true;
            }
            else {
                return false;
            }
        }

        public boolean isLeftParenthesis(String s) {
            if(s.matches("^[(]+$") || s.isEmpty()) {
                return true;
            }
            else {
                 return false;
            }
        }
}
