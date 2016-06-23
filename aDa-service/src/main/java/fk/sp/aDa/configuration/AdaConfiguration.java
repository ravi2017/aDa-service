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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class AdaConfiguration extends Configuration {
    /** Database configuration. */
    @Valid
    @NotNull
    private Map<String,DataSourceFactory> adaDbConfiguration = new HashMap<String,DataSourceFactory>();


    @JsonProperty("database")
    public Map<String,DataSourceFactory> getDataSourceFactory() {
        return adaDbConfiguration;
    }

    @Valid
    @NotNull
    private RotationManagementConfig rotationManagementConfig = new RotationManagementConfig();


}
