package fk.sp.aDa;

import com.google.common.collect.Sets;
import com.google.inject.Stage;

import com.hubspot.dropwizard.guice.GuiceBundle;
import com.sun.jersey.api.container.filter.LoggingFilter;

import fk.sp.aDa.configuration.AdaConfiguration;
import fk.sp.aDa.module.AdaModule;
import fk.sp.aDa.module.MetricsModule;
import fk.sp.aDa.resource.EmployeeResource;
import fk.sp.common.extensions.dropwizard.hystrix.HystrixRequestContextModule;
import fk.sp.common.extensions.dropwizard.jersey.JerseyClientModule;
import fk.sp.common.extensions.dropwizard.jersey.JerseyOutgoingRequestModule;
import fk.sp.common.extensions.guice.jpa.spring.JpaWithSpringModule;
import flipkart.retail.server.admin.bundle.RotationManagementBundle;
import flipkart.retail.server.admin.config.RotationManagementConfig;
import io.dropwizard.Application;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import java.util.Properties;
import java.util.logging.Logger;

public class AdaApplication extends Application<AdaConfiguration> {

    public static void main(String args[]) throws Exception {
        new AdaApplication().run(args);
    }

    @Override
    public void initialize(Bootstrap<AdaConfiguration> bootstrap) {
        bootstrap.addBundle(new MigrationsBundle<AdaConfiguration>() {
            @Override
            public DataSourceFactory getDataSourceFactory(AdaConfiguration adaConfiguration) {
                return adaConfiguration.getAdaDbConfiguration();
            }
        });



        GuiceBundle.Builder<AdaConfiguration> guiceBundleBuilder = GuiceBundle.newBuilder();
        GuiceBundle<AdaConfiguration> guiceBundle = guiceBundleBuilder
                .setConfigClass(AdaConfiguration.class)
                .addModule(new AdaModule())
                //.addModule(new MetricsModule(bootstrap.getMetricRegistry()))
                //.addModule(new JerseyClientModule())
                //.addModule(new HystrixRequestContextModule())
                //.addModule(new JerseyOutgoingRequestModule())
                .addModule(new JpaWithSpringModule(Sets.newHashSet("fk.sp.aDa.db.entity"),
                        new Properties()))
                .enableAutoConfig("fk.sp.aDa.resource")
                .build(Stage.DEVELOPMENT);

        bootstrap.addBundle(guiceBundle);
    }

    @Override
    public void run(AdaConfiguration adaConfiguration, Environment environment) {
        environment.jersey().register(
                new org.glassfish.jersey.filter.LoggingFilter(Logger.getLogger(org.glassfish.jersey.filter.LoggingFilter.class.getName()), true));
        //environment.jersey().register(MultipartFeature.class);
    }
}
