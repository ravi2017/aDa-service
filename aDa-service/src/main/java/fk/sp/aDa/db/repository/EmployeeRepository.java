package fk.sp.aDa.db.repository;

/**
 * Created by ravi.gupta on 25/5/16.
 */

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.codahale.metrics.annotation.Timed;

import java.util.*;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import fk.sp.aDa.request.EmployeeRequest;
import fk.sp.common.extensions.jpa.Page;
import fk.sp.common.extensions.jpa.PageRequest;
import fk.sp.common.extensions.jpa.SimpleJpaGenericRepository;
import fk.sp.aDa.db.entity.Employee;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EmployeeRepository extends SimpleJpaGenericRepository<Employee, Long> {

    @Inject
    public EmployeeRepository(Provider<EntityManager> entityManagerProvider) {
        super(entityManagerProvider);
    }

    @Timed
    public List<Employee> getAllEmployeesDetails() {
        //Query query = getEntityManager().createNamedQuery("getAllEmployees");
        //return query.getResultList();
        Map<String, Object> queryParam = new HashMap<>();
        PageRequest pageRequest = PageRequest.builder().pageNumber(0)
                .pageSize(10)
                .build();
        Page<Employee> offerAssociationPage = findAllByNamedQuery("getAllEmployees", queryParam, pageRequest);
        return offerAssociationPage.getContent();
    }

    /*@Timed
    public void getDatabaseDetails() {
        /*List<String> tableNames = new ArrayList<>();
        Session session = getEntityManager().unwrap(Session.class);
        SessionFactory sessionFactory = session.getSessionFactory();
        Map<String, ClassMetadata>  map = (Map<String, ClassMetadata>) sessionFactory.getAllClassMetadata();
        for(String entityName : map.keySet()){
            SessionFactoryImpl sfImpl = (SessionFactoryImpl) sessionFactory;
            String tableName = ((AbstractEntityPersister)sfImpl.getEntityPersister(entityName)).getTableName();
            tableNames.add(tableName);
        }
        return (Any) tableNames;*/
        /*Connection jdbcConnection = DriverManager.getConnection("","","");
        DatabaseMetaData md = jdbcConnection.getMetaData();
        ResultSet rs = md.getTables(null, null, "%", null);
        while(rs.next()) {
            System.out.println(rs.getString(3));
        }
    }*/

    /*@Timed
    public <Any> Any getQueryResult(EmployeeRequest employeeRequest) {
        String columns = "";
        String whereCondition = "";
        int counter = 0;
        for (String a : employeeRequest.getSelectedColumns()) {
            if(counter == 0)
                columns = columns + "e." + a;
            else
                columns = columns + ", " + "e." + a;
            counter++;
        }
        counter = 0;
        for(String a: employeeRequest.getWhereConditionColumns().keySet()) {
           if(counter == 0)
                whereCondition = whereCondition + "e." + a + " " + employeeRequest.getWhereConditionOperators().get(counter) + ":" +
                        a;
            else
                whereCondition = whereCondition + " and " + "e." + a + " " + employeeRequest.getWhereConditionOperators().get(counter) + ":" + a;
            counter++;
        }
        Query queryResult  = getEntityManager().createQuery(
                "select " + columns  + " From Employee e where " + whereCondition
        );
        counter = 0;
        System.out.println("select " + columns  + " From Employee e where " + whereCondition);
        for(String a: employeeRequest.getWhereConditionColumns().keySet()) {
            System.out.println(a);
            System.out.println(employeeRequest.getWhereConditionColumns().get(a));
            //System.out.println(employeeRequest.getWhereConditionValues().get(counter).getClass());
            queryResult.setParameter(a, employeeRequest.getWhereConditionColumns().get(a));
            counter++;
        }
        //queryResult.setParameter("name", "ravi");
        return (Any) queryResult.getResultList();



        //log.info(employeeList.size()+ " Size of the list");
        //log.info(employeeList.toString());

    }*/


}
