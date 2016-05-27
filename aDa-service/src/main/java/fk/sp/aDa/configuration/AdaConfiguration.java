package fk.sp.aDa.configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import flipkart.retail.server.admin.config.RotationManagementConfig;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.db.DataSourceFactory;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class AdaConfiguration extends Configuration {
    /** Database configuration. */
    @Valid
    @NotNull
    private DataSourceFactory adaDbConfiguration = new DataSourceFactory();

    @JsonProperty("database")
    public DataSourceFactory getDataSourceFactory() {
        return adaDbConfiguration;
    }

    @Valid
    @NotNull
    private RotationManagementConfig rotationManagementConfig = new RotationManagementConfig();


}
