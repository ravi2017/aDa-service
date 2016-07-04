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
import fk.sp.mptreasury.mpasl.apl.common.jackson.JodaDateTimeDeserializer;
import fk.sp.mptreasury.mpasl.apl.common.jackson.JodaDateTimeSerializer;
import fk.sp.aDa.configuration.AdaConfiguration;
import fk.sp.aDa.resource.ApplicationResource;
import flipkart.retail.server.admin.config.RotationManagementConfig;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.setup.Environment;
import org.joda.time.DateTime;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * It provides all the configuration objects and binds the instances.
 */
public class AdaModule extends AbstractModule {

    @Override
    public void configure() {
        bind(ApplicationResource.class);
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
    /*@Provides
    public HasDataSourceFactory providesDataSource(final Provider<AdaConfiguration> adaConfigurationProvider, int i) {
        return () -> {
            return adaConfigurationProvider.get().getAdaDbConfiguration().get(i);
        };
    }*/

    /**
     * Gets the instance of ObjectMapper.
     *
     * @param environmentProvider Provider of TDS configuration instance.
     * @return Singleton instance of ObjectMapper.
     */

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

    @Provides
    @Singleton
    public Map<String, DBI> providesJDBIHandle(Provider<AdaConfiguration> adaConfigurationProvider, Provider<Environment> environmentProvider) throws ClassNotFoundException {
        Map<String,DBI> dbiMap = new HashMap<String,DBI>();
        for(String a: adaConfigurationProvider.get().getDataSourceFactory().keySet()){
            final DBIFactory factory = new DBIFactory();
            final DBI dbi = factory.build(environmentProvider.get(), adaConfigurationProvider.get().getDataSourceFactory().get(a), "db");
            dbiMap.put(a,dbi);
        }
        return dbiMap;
    }
}
