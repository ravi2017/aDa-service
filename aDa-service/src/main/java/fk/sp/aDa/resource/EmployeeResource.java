package fk.sp.aDa.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.hazelcast.instance.OutOfMemoryErrorDispatcher;
import fk.sp.aDa.configuration.AdaConfiguration;
import fk.sp.aDa.db.entity.Employee;
import fk.sp.aDa.db.repository.EmployeeRepository;
import io.dropwizard.hibernate.UnitOfWork;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Created by ravi.gupta on 25/5/16.
 */
@Path("/employee")
@Produces(MediaType.APPLICATION_JSON)
public class EmployeeResource {

    private EmployeeRepository employeeRepository;

    private AdaConfiguration adaConfiguration;

    @Inject
    public EmployeeResource(EmployeeRepository employeeRepository,
                            AdaConfiguration adaConfiguration) {
        this.adaConfiguration = adaConfiguration;
        this.employeeRepository = employeeRepository;
        //this.helper = helper;
        //this.objectMapper = objectMapper;
    }

    @GET
    @Path("/hello")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public List<Employee> employeeList() {
        //return "Hello";
        return employeeRepository.getAllEmployeesDetails();
    }


}