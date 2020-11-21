/* (c) Planet Labs Inc. - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package stratus;

import com.airbus.oneinsight.common.logging.http.ContentLengthServletFilter;
import com.airbus.oneinsight.common.logging.http.LoggingHttpConfig;
import com.airbus.oneinsight.common.logging.http.LoggingUtils;
import com.airbus.oneinsight.common.logging.http.RequestBodyLoggingServletFilter;
import com.airbus.oneinsight.common.utilsservlet.*;
import lombok.extern.slf4j.Slf4j;
import org.geoserver.rest.RestConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.*;
import stratus.commons.beanfactory.FilteringBeanDefinitionLoader;

/**
 * @author Josh Fix
 * @author tingold
 */
@Slf4j
@Configuration
@EnableAutoConfiguration
@ConfigurationPropertiesScan
@ComponentScan(basePackages = {"stratus", "org.geoserver.rest"},
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = RestConfiguration.class)
        })
@ImportResource(value = {
        "classpath*:/applicationContext.xml,classpath*:/applicationSecurityContext.xml,!file:geofence,!bean:logsPage,"
                + "!file:gs-rest,!bean:demoMenuPage,!bean:bruteForceListener,!bean:secureCatalog,!bean:wpstimerFactory,"
                + "!beanClass:org.geoserver.wps.executor.WPSExecutionManager,"
                + "!beanClass:org.geoserver.wps.executor.ProcessStatusTracker,"
                + "!beanClass:org.geoserver.wps.DefaultWebProcessingService,"
                + "!beanClass:org.geoserver.config.GeoServerLoaderProxy,"       //Handled by RedisGeoServerLoader
                + "!beanClass:org.geoserver.wps.WPSInitializer,"                //Handled by custom BSE impl
                + "!beanClass:org.geoserver.gwc.config.GwcInitializer,"         //Handled by custom BSE impl
                + "!beanClass:org.geoserver.config.LockProviderInitializer,"    //Handled by RedisGeoServerLoader
                + "!beanClass:org.geoserver.logging.LoggingInitializer"         //Logging handled differently by BSE
},
        reader = FilteringBeanDefinitionLoader.class)
@Import({LoggingHttpConfig.class, GetCapabilitiesConfig.class})
public class StratusApplication {

    private static final String GEOWEBCACHE_CACHE_DIR = "GEOWEBCACHE_CACHE_DIR";

    public static void main(String... args) {
        run(args);
    }

    public static ApplicationContext run(String... args) {
        preConfigure();
        ConfigurableApplicationContext app = SpringApplication.run(StratusApplication.class, args);
        log.debug("Spring context successfully created.");
        return app;
    }

    private static void preConfigure() {
        /* gwc tries to get cache dir from these locations in this order: 1) system properties, 2) servlet context init
           params, and 3) system env.  gwc does not have access to the servlet context for some reason and throws an NPE
           so we need to get it from the system env and add it to the system properties.
         */
        if (null == System.getProperty(GEOWEBCACHE_CACHE_DIR) && null == System.getenv(GEOWEBCACHE_CACHE_DIR)) {
            // TODO: unnecessary logic here -- this block only gets called if System.getenv is null, but next line checks if it's not null
            if (null != System.getenv(GEOWEBCACHE_CACHE_DIR)) {
                System.setProperty(GEOWEBCACHE_CACHE_DIR, System.getenv(GEOWEBCACHE_CACHE_DIR));
            } else {
                System.setProperty(GEOWEBCACHE_CACHE_DIR, "/tmp/gwc");
            }
        }

        System.setProperty("GEOSERVER_XSTREAM_WHITELIST", "org.geoserver.**");
        if (System.getProperty("org.geotools.referencing.forceXY") == null) {
            System.setProperty("org.geotools.referencing.forceXY", "true");
        }
    }

    // Added by JP
    // This will rewrite request parameters for any request containing the
    // TIME parameter such that a list of layers applicable to the given interval
    // is provided to the the upstream and the TIME parameter is removed.
    @Bean
    WMSTimeRequestRewriter wmsTimeRequestRewriter() {
        return new WMSTimeRequestRewriter();
    }
    
    // Added by MCD
    // When we log http request details if there is no Content-Length response header then calculate one.
    // This is to support reporting requirements using HttpTrace
    @Bean
    ContentLengthServletFilter contentLengthServletFilter() {
        return new ContentLengthServletFilter();
    }

    // Added by MCD
    // We need to ensure tht GetCapabilies reposnses are rewritten to contain api key
    @Bean
    GetCapabilitiesResponseRewriter getCapabilitiesResponseRewriter(@Autowired GetCapabiliesProperties getCapabiliesProperties) {
        return new GetCapabilitiesResponseRewriter(getCapabiliesProperties);
    }

    // Added by MCD
    // Capture POST and PUT request body to be used the
    @Bean
    RequestBodyLoggingServletFilter requestBodyLoggingServletFilter(@Autowired LoggingUtils LoggingUtils) {
        return new RequestBodyLoggingServletFilter(LoggingUtils);
    }

    // Added by MCD
    // Used to set XFrameOptions response header according to property common.servlet.xframe.policy
    @Bean
    XFrameOptionsFilter xFrameOptionsFilter() {
        return new XFrameOptionsFilter();
    }


    // Added by MCD
    // Used to rewrite single layer WMS GetMap requests directly to GWC WMS endpoint
    @Bean
    WMSGetMapRedirect wmsGetMapRequestRewriter() {
        return new WMSGetMapRedirect();
    }

}
