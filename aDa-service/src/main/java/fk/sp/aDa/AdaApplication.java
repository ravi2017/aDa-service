package fk.sp.aDa;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.Sets;
import com.google.inject.Stage;

import com.hubspot.dropwizard.guice.GuiceBundle;
import com.sun.jersey.api.container.filter.LoggingFilter;

import fk.sp.aDa.configuration.AdaConfiguration;
import fk.sp.aDa.module.AdaModule;

import fk.sp.common.extensions.guice.jpa.spring.JpaWithSpringModule;

import io.dropwizard.Application;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.servlets.CrossOriginFilter;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import java.text.SimpleDateFormat;
import java.util.EnumSet;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import static org.eclipse.jetty.servlets.CrossOriginFilter.*;

public class AdaApplication extends Application<AdaConfiguration> {

    public static void main(String args[]) throws Exception {

        new AdaApplication().run(args);
    }

    @Override
    public void initialize(Bootstrap<AdaConfiguration> bootstrap) {
        /*bootstrap.addBundle(new MigrationsBundle<AdaConfiguration>() {
            @Override
            public DataSourceFactory getDataSourceFactory(AdaConfiguration adaConfiguration) {
                return adaConfiguration.getAdaDbConfiguration();
            }
        });*/



        GuiceBundle.Builder<AdaConfiguration> guiceBundleBuilder = GuiceBundle.newBuilder();
        GuiceBundle<AdaConfiguration> guiceBundle = guiceBundleBuilder
                .setConfigClass(AdaConfiguration.class)
                .addModule(new AdaModule())
                //.addModule(new MetricsModule(bootstrap.getMetricRegistry()))
                //.addModule(new JerseyClientModule())
                //.addModule(new HystrixRequestContextModule())
                //.addModule(new JerseyOutgoingRequestModule())
                //.addModule(new JpaWithSpringModule(Sets.newHashSet("fk.sp.aDa.db"),
                //        new Properties()))
                .enableAutoConfig("fk.sp.aDa.resource")
                .build(Stage.DEVELOPMENT);

        bootstrap.addBundle(guiceBundle);
    }

//    @Override
//    public void run(AdaConfiguration adaConfiguration, Environment environment) {
//        environment.jersey().register(
//                new org.glassfish.jersey.filter.LoggingFilter(Logger.getLogger(org.glassfish.jersey.filter.LoggingFilter.class.getName()), true));
//        environment.getObjectMapper().disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
//        environment.getObjectMapper().setDateFormat(new SimpleDateFormat("MMM dd, yyyy"));
//        environment.getObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
//    }
@Override
public void run(AdaConfiguration adaConfiguration, Environment environment) {

    FilterRegistration.Dynamic filter = environment.servlets().addFilter("CORSFilter", CrossOriginFilter.class);

    filter.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), false, environment.getApplicationContext().getContextPath() + "*");
    filter.setInitParameter(ALLOWED_METHODS_PARAM, "GET,PUT,POST,OPTIONS");
    filter.setInitParameter(ALLOWED_ORIGINS_PARAM, "*");
    filter.setInitParameter(ALLOWED_HEADERS_PARAM, "Origin, Content-Type, Accept");
    filter.setInitParameter(ALLOW_CREDENTIALS_PARAM, "true");

    environment.jersey().register(
            new org.glassfish.jersey.filter.LoggingFilter(Logger.getLogger(org.glassfish.jersey.filter.LoggingFilter.class.getName()), true));
    environment.getObjectMapper().disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    environment.getObjectMapper().setDateFormat(new SimpleDateFormat("MMM dd, yyyy"));
    environment.getObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
}
}
