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
import scala.util.parsing.combinator.testing.Str;

import java.util.List;
import java.util.Vector;

@Getter
@Setter
@Entity
@Table (name = "clients")
@NamedQueries({
        @NamedQuery(name = "getAllEmployees",
                query = "Select e from  Employee e"),
        @NamedQuery(name="getAllEmployees.count",
                query = "SELECT count(l) as count from Employee l"),
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
    private int id;

    public Vector<String> columnNames() {
        Vector<String> columnNamesEmployee = new Vector<String>();
        columnNamesEmployee.addElement("name");
        columnNamesEmployee.addElement("email");
        columnNamesEmployee.addElement("description");
        columnNamesEmployee.addElement("id");
        return columnNamesEmployee;
    }

    public Vector<String> primaryKey() {
        Vector<String> primaryKeyEmployee = new Vector<String>();
        primaryKeyEmployee.addElement("id");
        return primaryKeyEmployee;
    }

}
