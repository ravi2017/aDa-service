package fk.sp.aDa.db.entity;

/**
 * Created by ravi.gupta on 27/5/16.
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
@Table (name = "databases_name")
@NamedQueries({
        @NamedQuery(name = "getAllDatabases",
                query = "Select e from  Databases e"),
        @NamedQuery(name="getAllDatabases.count",
                query = "SELECT count(l) as count from Databases l")
})

public class Databases
{

    @NotEmpty
    private String name;

    @Id
    @NotEmpty
    private long id;
}