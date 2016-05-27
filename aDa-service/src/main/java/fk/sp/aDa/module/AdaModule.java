package fk.sp.aDa.module;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import fk.sp.common.extensions.dropwizard.db.HasDataSourceFactory;
import fk.sp.mptreasury.mpasl.apl.common.jackson.JodaDateTimeDeserializer;
import fk.sp.mptreasury.mpasl.apl.common.jackson.JodaDateTimeSerializer;
import fk.sp.aDa.configuration.AdaConfiguration;
import fk.sp.aDa.resource.EmployeeResource;
import flipkart.retail.server.admin.config.RotationManagementConfig;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.setup.Environment;
import org.joda.time.DateTime;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * It provides all the configuration objects and binds the instances.
 */
public class AdaModule extends AbstractModule {

    @Override
    public void configure() {
        bind(EmployeeResource.class);
    }


    /**
     * Gets the instance of RotationManagementConfig.
     *
     //@param tdsConfigurationProvider Provider of TDS configuration instance.
     * @return Singleton instance of RotationManagementConfig.
     */
    @Provides
    @Singleton
    public RotationManagementConfig providesRotationManagementConfig(
            Provider<AdaConfiguration> adaConfigurationProvider) {
        return adaConfigurationProvider.get().getRotationManagementConfig();
    }

    /*
     * Gets the instance of HasDataSourceFactory.
     *
     * @param tdsConfigurationProvider Provider of TDS configuration instance.
     * @return Instance of HasDataSourceFactory.
     */
    @Provides
    public HasDataSourceFactory providesDataSource(final Provider<AdaConfiguration> adaConfigurationProvider) {
        return () -> {
            return adaConfigurationProvider.get().getAdaDbConfiguration();
        };
    }

    /**
     * Gets the instance of ObjectMapper.
     *
     * @param environmentProvider Provider of TDS configuration instance.
     * @return Singleton instance of ObjectMapper.
     */
    /*
    @Provides
    @Singleton
    public ObjectMapper providesObjectMapper(Provider<Environment> environmentProvider) {
        ObjectMapper objectMapper = environmentProvider.get().getObjectMapper();

        class CustomModule extends SimpleModule {
            public CustomModule() {
                addDeserializer(DateTime.class, new JodaDateTimeDeserializer());
                addSerializer(DateTime.class, new JodaDateTimeSerializer());
            }
        }

        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.registerModule(new CustomModule());
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.configure(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true)
                .configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true);
        objectMapper.setTimeZone(TimeZone.getTimeZone("IST"));
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss"));
        return objectMapper;
    }
    */

    /*
     * Gets the instance of JerseyClientConfiguration.
     *
     * @param tdsConfigurationProvider Provider of TDS configuration instance.
     * @return Singleton instance of JerseyClientConfiguration.
     *
    @Provides
    @Singleton
    JerseyClientConfiguration getJerseyClientConfiguration(Provider<AdaConfiguration> adaConfigurationProvider) {
        return adaConfigurationProvider.get().getClientConfiguration();
    }
    */
}
