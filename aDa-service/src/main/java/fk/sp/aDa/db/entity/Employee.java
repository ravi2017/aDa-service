package fk.sp.aDa.db.entity;

/**
 * Created by ravi.gupta on 25/5/16.
 */

import org.hibernate.validator.constraints.NotEmpty;
import org.joda.time.DateTime;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import io.dropwizard.jackson.JsonSnakeCase;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table (name = "clients")
@NamedQueries({
        @NamedQuery(name = "getAllEmployees",
                query = "Select e from  Employee e"),
        @NamedQuery(name="getAllEmployees.count",
                query = "SELECT count(l) as count from Employee l")
})

public class Employee
{

    @NotEmpty
    private String name;

    @NotEmpty
    private String email;

    @NotEmpty
    private String description;

    @Id
    @NotEmpty
    private long id;
}
