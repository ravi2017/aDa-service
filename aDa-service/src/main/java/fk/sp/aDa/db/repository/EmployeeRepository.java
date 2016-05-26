package fk.sp.aDa.db.repository;

/**
 * Created by ravi.gupta on 25/5/16.
 */

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.persist.Transactional;
import com.codahale.metrics.annotation.Timed;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import fk.sp.common.extensions.jpa.SimpleJpaGenericRepository;
import fk.sp.aDa.db.entity.Employee;

public class EmployeeRepository extends SimpleJpaGenericRepository<Employee, Long> {

    @Inject
    public EmployeeRepository(Provider<EntityManager> entityManagerProvider) {
        super(entityManagerProvider);
    }

    @Timed
    public List<Employee> getAllEmployeesDetails() {
        Query query = getEntityManager().createNamedQuery("getAllEmployees");
        return query.getResultList();
    }

}
