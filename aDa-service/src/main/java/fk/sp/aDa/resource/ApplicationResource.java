package fk.sp.aDa.resource;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import fk.sp.aDa.configuration.AdaConfiguration;
import fk.sp.aDa.request.*;
import org.skife.jdbi.v2.DBI;
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
public class ApplicationResource {

    //private EmployeeRepository employeeRepository;
    int queryPossible = 0;

    private AdaConfiguration adaConfiguration;

    private Map<String,DBI> dbiMap;

    @Inject
    public ApplicationResource(AdaConfiguration adaConfiguration,
                               Map<String, DBI> jdbi) {
        this.adaConfiguration = adaConfiguration;
        this.dbiMap = jdbi;
    }

    @GET
    @Path("/getTables")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public List<String> databaseInformation(@QueryParam("database") String databaseName) throws Exception {
        List<String> tableNames = databaseDetails(databaseName);
        return tableNames;
    }

    @GET
    @Path("/describeTableName")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public List<String> tableInformation(@QueryParam("table") String tableName, @QueryParam("code") int code, @QueryParam("database") String databaseName) throws Exception{
        List<String> columnNames = new ArrayList<String>();
        List<String> indexColumnNames = new ArrayList<String>();
        if(code==0)
            return columnNames;
        else
            return indexColumnNames;
    }

    @POST
    @Path("/join")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public <Any> Any joinDetails(ClientRequest clientRequest) throws  Exception{
        Handle handle = null;
        try {
                List<String> tableNames = new ArrayList<String>();
                List<String> allTableNames = new ArrayList<String>();
                allTableNames = databaseDetails(clientRequest.getDatabaseName());
                tableNames = clientRequest.getJoinCompleteRequest().getJoinRequestTables().getTableNames();
                Map<String, List<String>> tableColumnInformation = new HashMap<String, List<String>>();
                Map<String, List<String>> tableIndexInformation = new HashMap<String, List<String>>();
                for (int i = 0; i < tableNames.size(); i++) {
                    tableColumnInformation.put(tableNames.get(i), tableDetails(tableNames.get(i), 0, clientRequest.getDatabaseName()));
                    tableIndexInformation.put(tableNames.get(i), tableDetails(tableNames.get(i), 1, clientRequest.getDatabaseName()));
                }
                queryPossible = 0;
                Connection connection = null;
                Statement statement = null;
                handle = dbiMap.get(clientRequest.getDatabaseName()).open();
                String sql = "";

                String columns = "";
                if(clientRequest.getSelectAllColumns()==0) {
                    columns = selectColumnBuilder(clientRequest, allTableNames, tableColumnInformation, tableIndexInformation);
                }
                else {
                    columns = "*";
                }
                if (columns.equals(null))
                    queryPossible = 1;

                if(clientRequest.getSelectDistinct()==0)
                    sql = sql + " select " + columns + " From ";
                else
                    sql = sql + " select DISTINCT " + columns + " From ";
                sql = joinPartBuilder(clientRequest, allTableNames, tableColumnInformation, tableIndexInformation, sql);


                String whereCondition = "";
                whereCondition = wherePartBuilder(clientRequest, allTableNames, tableColumnInformation, tableIndexInformation);
                sql = sql + whereCondition;

                String groupBy = "";
                groupBy = groupByPartBuilder(clientRequest, allTableNames, tableColumnInformation, tableIndexInformation);
                sql = sql + groupBy;

                String having = "";
                having = havingPartBuilder(clientRequest, allTableNames, tableColumnInformation, tableIndexInformation);
                sql = sql + having;

                String orderBy = "";
                orderBy = orderByPartBuilder(clientRequest, allTableNames, tableColumnInformation, tableIndexInformation);
                sql = sql + orderBy;

                System.out.println(sql);
                boolean validator;
                if (isParenthesisMatch(sql) && queryPossible == 0 && isNum(clientRequest.getMaxRows()) && isNum(clientRequest.getPageNumber()) && isNum(clientRequest.getPageSize())) {
                    validator = true;
                } else {
                    System.out.println(isParenthesisMatch(sql));
                    System.out.println(queryPossible);
                    System.out.println(isNum(clientRequest.getMaxRows()));
                    System.out.println(isNum(clientRequest.getPageNumber()));
                    System.out.println(isNum(clientRequest.getPageSize()));
                    validator = false;
                }

                if (validator == true) {
                    //explainQueryBuilder(clientRequest, sql);
                    String sqlTimeCheck = sql;
                    if (!clientRequest.getPageSize().isEmpty()) {
                        if (!clientRequest.getPageNumber().isEmpty()) {
                            sql = sql + " LIMIT " + clientRequest.getPageNumber() + "," + clientRequest.getPageSize();
                        } else {
                            sql = sql + "LIMIT" + clientRequest.getPageSize();
                        }
                    }

                    int counter = 0;
                    Query<Map<String, Object>> query = handle.createQuery(sql);
                    for (int i = 0; i < clientRequest.getWhereConditionList().size(); i++) {
                        if(clientRequest.getWhereConditionList().get(i).getWhereConditionOperator().equals("BETWEEN")) {
                            query.bind(counter, clientRequest.getWhereConditionList().get(i).getBetweenLeftValue());
                            counter++;
                            query.bind(counter, clientRequest.getWhereConditionList().get(i).getBetweenRightValue());
                            counter++;
                        }
                        else if (clientRequest.getWhereConditionList().get(i).getWhereConditionOperator().equals("IN") || clientRequest.getWhereConditionList().get(i).getWhereConditionOperator().equals("NOT IN")) {
                            for(int j=0;j<clientRequest.getWhereConditionList().get(i).getInList().size();j++) {
                                query.bind(counter, clientRequest.getWhereConditionList().get(i).getInList().get(j));
                                counter++;
                            }
                        }
                        else {
                            query.bind(counter, clientRequest.getWhereConditionList().get(i).getWhereConditionValue());
                            counter++;
                        }

                    }
                    //Calendar cal = Calendar.getInstance();
                    //System.out.println(cal.getTime());
                    counter = 0;
                    Query<Map<String, Object>> queryTimeCheck = handle.createQuery(sqlTimeCheck);
                    for (int i = 0; i < clientRequest.getWhereConditionList().size(); i++) {
                        if(clientRequest.getWhereConditionList().get(i).getWhereConditionOperator().equals("BETWEEN")) {
                            queryTimeCheck.bind(counter, clientRequest.getWhereConditionList().get(i).getBetweenLeftValue());
                            counter++;
                            queryTimeCheck.bind(counter, clientRequest.getWhereConditionList().get(i).getBetweenRightValue());
                            counter++;
                        }
                        else if (clientRequest.getWhereConditionList().get(i).getWhereConditionOperator().equals("IN") || clientRequest.getWhereConditionList().get(i).getWhereConditionOperator().equals("NOT IN")) {
                            for(int j=0;j<clientRequest.getWhereConditionList().get(i).getInList().size();j++) {
                                queryTimeCheck.bind(counter, clientRequest.getWhereConditionList().get(i).getInList().get(j));
                                counter++;
                            }
                        }
                        else {
                            queryTimeCheck.bind(counter, clientRequest.getWhereConditionList().get(i).getWhereConditionValue());
                            counter++;
                        }

                    }
                    queryTimeCheck.setQueryTimeout(60);
                    float startTime, endTime;
                    startTime = System.currentTimeMillis();
                    queryTimeCheck.list();
                    endTime = System.currentTimeMillis();
                    if ((endTime - startTime) >= 60000) {
                        return (Any) "Too Much Time Can't Execute";
                    } else {
                        if (!clientRequest.getMaxRows().isEmpty()) {
                            List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
                            result = query.setMaxRows(Integer.parseInt(clientRequest.getMaxRows())).list();
                            return (Any) result;
                        } else {
                            List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
                            result = query.list();
                            return (Any) result;
                        }
                    }
                } else {
                    System.out.println("Error!!!");
                    String errorMessage = "error";
                    return (Any) errorMessage;
                }
        }
        finally {
            if(handle != null){
                handle.close();
            }
        }
    }

    public String selectColumnBuilder(ClientRequest clientRequest, List<String> allTableNames, Map<String,List<String>> tableColumnInformation, Map<String,List<String>> tableIndexInformation) {
        String columns = "";
        for(int i=0; i< clientRequest.getJoinCompleteRequest().getSelectedColumnsList().size();i++) {
            if (!tableValidator(clientRequest.getJoinCompleteRequest().getSelectedColumnsList().get(i).getSelectedTables(), allTableNames)) {
                queryPossible = 1;
            }
            for (int j = 0; j < clientRequest.getJoinCompleteRequest().getSelectedColumnsList().get(i).getSelectedColumns().size(); j++) {
                if (i == 0 && j == 0) {
                    if(!columnValidator(clientRequest,clientRequest.getJoinCompleteRequest().getSelectedColumnsList().get(i).getSelectedTables(), clientRequest.getJoinCompleteRequest().getSelectedColumnsList().get(i).getSelectedColumns().get(j), tableColumnInformation))
                        queryPossible = 1;
                    columns = columns + clientRequest.getJoinCompleteRequest().getSelectedColumnsList().get(i).getAggregateFunction().get(j) + "(" +clientRequest.getJoinCompleteRequest().getSelectedColumnsList().get(i).getSelectedTables() + "." + clientRequest.getJoinCompleteRequest().getSelectedColumnsList().get(i).getSelectedColumns().get(j) + ")";
                }
                else {
                    if(!columnValidator(clientRequest,clientRequest.getJoinCompleteRequest().getSelectedColumnsList().get(i).getSelectedTables(),clientRequest.getJoinCompleteRequest().getSelectedColumnsList().get(i).getSelectedColumns().get(j),tableColumnInformation)) {
                        queryPossible = 1;
                    }
                    columns = columns + "," + clientRequest.getJoinCompleteRequest().getSelectedColumnsList().get(i).getAggregateFunction().get(j) + "(" + clientRequest.getJoinCompleteRequest().getSelectedColumnsList().get(i).getSelectedTables() + "." + clientRequest.getJoinCompleteRequest().getSelectedColumnsList().get(i).getSelectedColumns().get(j) + ")";
                }
            }
        }
        return  columns;
    }

    public String joinPartBuilder(ClientRequest clientRequest, List<String> allTableNames, Map<String,List<String>> tableColumnInformation, Map<String,List<String>> tableIndexInformation, String sql) {
        for(int i=0; i< clientRequest.getJoinCompleteRequest().getJoinRequestTables().getTableNames().size();i++) {
            if (i == 0) {
                if (!tableValidator(clientRequest.getJoinCompleteRequest().getJoinRequestTables().getTableNames().get(i), allTableNames)) {
                    queryPossible = 1;
                }
                sql = sql + clientRequest.getJoinCompleteRequest().getJoinRequestTables().getTableNames().get(i);
            } else {
                if (!tableValidator(clientRequest.getJoinCompleteRequest().getJoinRequestTables().getTableNames().get(i), allTableNames)) {
                    queryPossible = 1;
                }
                sql = sql + " join " + clientRequest.getJoinCompleteRequest().getJoinRequestTables().getTableNames().get(i) + " on ";
                for (int j = 0; j < clientRequest.getJoinCompleteRequest().getJoinRequestConditionList().size(); j++) {
                    if (clientRequest.getJoinCompleteRequest().getJoinRequestConditionList().get(j).getTableTwoName().equals(clientRequest.getJoinCompleteRequest().getJoinRequestTables().getTableNames().get(i))) {
                        for (int k = 0; k <= i; k++) {
                            if (clientRequest.getJoinCompleteRequest().getJoinRequestConditionList().get(j).getTableOneName().equals(clientRequest.getJoinCompleteRequest().getJoinRequestTables().getTableNames().get(k))) {
                                if (!tableValidator(clientRequest.getJoinCompleteRequest().getJoinRequestConditionList().get(j).getTableOneName(), allTableNames) && tableValidator(clientRequest.getJoinCompleteRequest().getJoinRequestConditionList().get(j).getTableTwoName(), allTableNames) && columnValidator(clientRequest,clientRequest.getJoinCompleteRequest().getJoinRequestConditionList().get(j).getTableOneName(), clientRequest.getJoinCompleteRequest().getJoinRequestConditionList().get(j).getTableOneColumnName(), tableColumnInformation) && columnValidator(clientRequest,clientRequest.getJoinCompleteRequest().getJoinRequestConditionList().get(j).getTableTwoName(), clientRequest.getJoinCompleteRequest().getJoinRequestConditionList().get(j).getTableTwoColumnName(), tableColumnInformation)) {
                                    queryPossible = 1;
                                }
                                if (k == 0)
                                    sql = sql + clientRequest.getJoinCompleteRequest().getJoinRequestConditionList().get(j).getTableOneName() + "." + clientRequest.getJoinCompleteRequest().getJoinRequestConditionList().get(j).getTableOneColumnName() + "=" + clientRequest.getJoinCompleteRequest().getJoinRequestConditionList().get(j).getTableTwoName() + "." + clientRequest.getJoinCompleteRequest().getJoinRequestConditionList().get(j).getTableTwoColumnName();
                                else
                                    sql = sql + " and " + clientRequest.getJoinCompleteRequest().getJoinRequestConditionList().get(j).getTableOneName() + "." + clientRequest.getJoinCompleteRequest().getJoinRequestConditionList().get(j).getTableOneColumnName() + "=" + clientRequest.getJoinCompleteRequest().getJoinRequestConditionList().get(j).getTableTwoName() + "." + clientRequest.getJoinCompleteRequest().getJoinRequestConditionList().get(j).getTableTwoColumnName();
                            }
                        }
                    }
                }
            }
        }
        return sql;
    }

    public String wherePartBuilder(ClientRequest clientRequest, List<String> allTableNames, Map<String,List<String>> tableColumnInformation , Map<String,List<String>> tableIndexInformation) {
        String whereCondition = "";
        int counter = 0;
        counter = 0;
        for(int i=0;i<clientRequest.getWhereConditionList().size();i++) {
            if (!(tableValidator(clientRequest.getWhereConditionList().get(i).getTableName(), allTableNames) && columnValidator(clientRequest,clientRequest.getWhereConditionList().get(i).getTableName(), clientRequest.getWhereConditionList().get(i).getWhereConditionColumn(), tableColumnInformation) && isLeftParenthesis(clientRequest.getWhereConditionList().get(i).getLeftParenthesis()) && isRightParenthesis(clientRequest.getWhereConditionList().get(i).getRightParenthesis()))) {
                queryPossible = 1;
            }
            if (counter == 0) {
                if(clientRequest.getWhereConditionList().get(i).getWhereConditionOperator().equals("BETWEEN")){
                    whereCondition = whereCondition + " where " + clientRequest.getWhereConditionList().get(i).getLeftParenthesis() + "(" + clientRequest.getWhereConditionList().get(i).getTableName() + "." + clientRequest.getWhereConditionList().get(i).getWhereConditionColumn() + " >= " + " ?" + " and " + clientRequest.getWhereConditionList().get(i).getTableName() + "." + clientRequest.getWhereConditionList().get(i).getWhereConditionColumn() + " <= " + "?" + ")" + clientRequest.getWhereConditionList().get(i).getRightParenthesis();
                }
                else if(clientRequest.getWhereConditionList().get(i).getWhereConditionOperator().equals("IN") || clientRequest.getWhereConditionList().get(i).getWhereConditionOperator().equals("NOT IN") ){
                    whereCondition = whereCondition + " where " + clientRequest.getWhereConditionList().get(i).getLeftParenthesis() + clientRequest.getWhereConditionList().get(i).getTableName() + "." + clientRequest.getWhereConditionList().get(i).getWhereConditionColumn() + " " + clientRequest.getWhereConditionList().get(i).getWhereConditionOperator() + " " + inListBuilder(clientRequest.getWhereConditionList().get(i).getInList()) + clientRequest.getWhereConditionList().get(i).getRightParenthesis();

                }
                else {
                    whereCondition = whereCondition + " where " + clientRequest.getWhereConditionList().get(i).getLeftParenthesis() + clientRequest.getWhereConditionList().get(i).getTableName() + "." + clientRequest.getWhereConditionList().get(i).getWhereConditionColumn() + " " + clientRequest.getWhereConditionList().get(i).getWhereConditionOperator() + " ?" +
                            clientRequest.getWhereConditionList().get(i).getRightParenthesis();
                }
            }
            else {
                if(clientRequest.getWhereConditionList().get(i).getWhereConditionOperator().equals("BETWEEN")) {
                    whereCondition = whereCondition + " " + clientRequest.getWhereConditionList().get(i).getConnectorOperator() + " " +clientRequest.getWhereConditionList().get(i).getLeftParenthesis() + "(" + clientRequest.getWhereConditionList().get(i).getTableName() + "." + clientRequest.getWhereConditionList().get(i).getWhereConditionColumn() + " >= " + "?" + " and " + clientRequest.getWhereConditionList().get(i).getTableName() + "." + clientRequest.getWhereConditionList().get(i).getWhereConditionColumn() + " <= " + "?" + ")" + clientRequest.getWhereConditionList().get(i).getRightParenthesis();
                }
                else if(clientRequest.getWhereConditionList().get(i).getWhereConditionOperator().equals("IN") || clientRequest.getWhereConditionList().get(i).getWhereConditionOperator().equals("NOT IN")){
                    whereCondition = whereCondition + " " + clientRequest.getWhereConditionList().get(i).getConnectorOperator() + " " + clientRequest.getWhereConditionList().get(i).getLeftParenthesis() + clientRequest.getWhereConditionList().get(i).getTableName() + "." + clientRequest.getWhereConditionList().get(i).getWhereConditionColumn() + " " + clientRequest.getWhereConditionList().get(i).getWhereConditionOperator() + " " + inListBuilder(clientRequest.getWhereConditionList().get(i).getInList()) + clientRequest.getWhereConditionList().get(i).getRightParenthesis();
                }
                else {
                    whereCondition = whereCondition + " " + clientRequest.getWhereConditionList().get(i).getConnectorOperator() + " " + clientRequest.getWhereConditionList().get(i).getLeftParenthesis() + " " + clientRequest.getWhereConditionList().get(i).getTableName() + "." + clientRequest.getWhereConditionList().get(i).getWhereConditionColumn() + " " + clientRequest.getWhereConditionList().get(i).getWhereConditionOperator() + " ?" + clientRequest.getWhereConditionList().get(i).getRightParenthesis();
                }
            }
            counter++;
        }
        return  whereCondition;
    }

    public String groupByPartBuilder(ClientRequest clientRequest, List<String> allTableNames, Map<String,List<String>> tableColumnInformation , Map<String,List<String>> tableIndexInformation) {
        String groupBy = "";
        if(clientRequest.getGroupByList().size()>0) {
            for(int i=0;i<clientRequest.getGroupByList().size();i++) {
                if(!(tableValidator(clientRequest.getGroupByList().get(i).getGroupByTableName(),allTableNames) && columnValidator(clientRequest,clientRequest.getGroupByList().get(i).getGroupByTableName(),clientRequest.getGroupByList().get(i).getGroupByColumnName(),tableColumnInformation))) {
                    queryPossible = 1;
                }
                if (i == 0) {
                    groupBy = groupBy + " Group By " + clientRequest.getGroupByList().get(i).getGroupByTableName() + "." + clientRequest.getGroupByList().get(i).getGroupByColumnName();
                } else {
                    groupBy = groupBy + "," + clientRequest.getGroupByList().get(i).getGroupByTableName() + "." + clientRequest.getGroupByList().get(i).getGroupByColumnName();
                }
            }
        }
        return  groupBy;
    }

    public String havingPartBuilder(ClientRequest clientRequest, List<String> allTableNames, Map<String,List<String>> tableColumnInformation , Map<String,List<String>> tableIndexInformation) {
        String having = "";
        if(clientRequest.getHavingList().size()>0) {
            for(int i=0;i<clientRequest.getHavingList().size();i++) {
                if(!(tableValidator(clientRequest.getHavingList().get(i).getTableName(),allTableNames) && columnValidator(clientRequest,clientRequest.getHavingList().get(i).getTableName(),clientRequest.getHavingList().get(i).getColumnName(),tableColumnInformation) && isRightParenthesis(clientRequest.getHavingList().get(i).getRightParenthesis()) && isLeftParenthesis(clientRequest.getHavingList().get(i).getLeftParenthesis()))) {
                    queryPossible = 1;
                }
                if (i == 0) {
                    having = having + " Having " + clientRequest.getHavingList().get(i).getLeftParenthesis() + clientRequest.getHavingList().get(i).getAggregateFunction() + "(" + clientRequest.getHavingList().get(i).getTableName() + "." + clientRequest.getHavingList().get(i).getColumnName() + ")" + " ";
                    if (clientRequest.getHavingList().get(i).getOperatorType().equals("BETWEEN")) {
                        having = having + "BETWEEN " + clientRequest.getHavingList().get(i).getBetweenLeftValue() + " AND " + clientRequest.getHavingList().get(i).getBetweenRightValue() + clientRequest.getHavingList().get(i).getRightParenthesis();
                    } else {
                        having = having + clientRequest.getHavingList().get(i).getOperatorType() + clientRequest.getHavingList().get(i).getOperatorValue() + clientRequest.getHavingList().get(i).getRightParenthesis();
                    }
                } else {
                    having = having + " " + clientRequest.getHavingList().get(i).getConnectorType() + " " + clientRequest.getHavingList().get(i).getLeftParenthesis() + clientRequest.getHavingList().get(i).getAggregateFunction() + "(" + clientRequest.getHavingList().get(i).getTableName() + "." + clientRequest.getHavingList().get(i).getColumnName() + ")" + " ";
                    if (clientRequest.getHavingList().get(i).getOperatorType().equals("BETWEEN")) {
                        having = having + "BETWEEN " + clientRequest.getHavingList().get(i).getBetweenLeftValue() + " AND " + clientRequest.getHavingList().get(i).getBetweenRightValue() + clientRequest.getHavingList().get(i).getRightParenthesis();
                    } else {
                        having = having + clientRequest.getHavingList().get(i).getOperatorType() + clientRequest.getHavingList().get(i).getOperatorValue() + clientRequest.getHavingList().get(i).getRightParenthesis();
                    }
                }
            }
        }
        return  having;
    }

    public String orderByPartBuilder(ClientRequest clientRequest, List<String> allTableNames, Map<String,List<String>> tableColumnInformation , Map<String,List<String>> tableIndexInformation) {
        String orderBy = "";
        if(clientRequest.getOrderByList().size()>0) {
            for(int i=0;i<clientRequest.getOrderByList().size();i++) {
                if (!(tableValidator(clientRequest.getOrderByList().get(i).getTableName(), allTableNames) && columnValidator(clientRequest,clientRequest.getOrderByList().get(i).getTableName(), clientRequest.getOrderByList().get(i).getColumnName(), tableColumnInformation))) {
                    queryPossible = 1;
                }
                if (i == 0) {
                    orderBy = orderBy + " Order By " + clientRequest.getOrderByList().get(i).getAggregateFunction() + "(" + clientRequest.getOrderByList().get(i).getTableName() + "." + clientRequest.getOrderByList().get(i).getColumnName() + ")" + " " + clientRequest.getOrderByList().get(i).getSortType();
                } else {
                    orderBy = orderBy + "," + clientRequest.getOrderByList().get(i).getAggregateFunction() + "(" + clientRequest.getOrderByList().get(i).getTableName() + "." + clientRequest.getOrderByList().get(i).getColumnName() + ")" + " " + clientRequest.getOrderByList().get(i).getSortType();
                }
            }
        }
        return  orderBy;
    }

    public void explainQueryBuilder(ClientRequest clientRequest, String sql) {
        Handle handle = dbiMap.get(clientRequest.getDatabaseName()).open();
        Query<Map<String, Object>> temp = handle.createQuery("explain " + sql);
        for (int i=0;i<clientRequest.getWhereConditionList().size();i++) {
            if(!clientRequest.getWhereConditionList().get(i).getWhereConditionOperator().equals("BETWEEN"))
                temp.bind(clientRequest.getWhereConditionList().get(i).getWhereConditionColumn(), clientRequest.getWhereConditionList().get(i).getWhereConditionValue());
        }

        System.out.println(temp.list().get(0).get("rows"));
    }

    public boolean columnValidator(ClientRequest clientRequest,String tableName, String columnName, Map<String,List<String>> tableColumnInformation) {
        Boolean check = (tableColumnInformation.containsKey(tableName) && tableValidator(tableName,clientRequest.getJoinCompleteRequest().getJoinRequestTables().getTableNames()));
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

    public List<String> databaseDetails(String databaseName) throws Exception {
        Connection connection = null;
        List<String> tableNames = new ArrayList<String>();
        try {
            Handle handle = dbiMap.get(databaseName).open();
            connection = handle.getConnection();
            // Gets the metadata of the database
            DatabaseMetaData dbmd = connection.getMetaData();
            String[] types = {"TABLE"};

            ResultSet rs = dbmd.getTables(null, null, "%", types);
            while (rs.next()) {
                String tableCatalog = rs.getString(1);
                String tableSchema = rs.getString(2);
                String tableName = rs.getString(3);
                tableNames.add(tableName);
            }
            handle.close();
        } catch (SQLException e) {
            if (connection != null && !connection.isClosed()) {
                connection.close();

            }
        }
        return tableNames;
    }

    public List<String> tableDetails(String tableName, int code, String databaseName) throws Exception{
        Connection connection = null;
        Statement statement = null;
        List<String> columnNames = new ArrayList<String>();
        List<String> indexColumnNames = new ArrayList<String>();
        try {
            Handle handle = dbiMap.get(databaseName).open();
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
            handle.close();
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

    public String inListBuilder(List<String> inList) {
        String inValueList = "";
        inValueList = "(";
        for(int i=0;i<inList.size();i++) {
            if(i==0)
                inValueList = inValueList + "?";
            else
                inValueList = inValueList + ",?";
        }
        inValueList = inValueList + ")";
        return inValueList;
    }

}
