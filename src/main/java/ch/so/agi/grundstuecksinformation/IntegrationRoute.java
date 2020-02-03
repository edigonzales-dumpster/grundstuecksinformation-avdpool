package ch.so.agi.grundstuecksinformation;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.dataformat.zipfile.ZipSplitter;
import org.apache.camel.support.processor.idempotent.FileIdempotentRepository;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class IntegrationRoute extends RouteBuilder {
    private static final Logger log = Logger.getLogger(AppConfig.class);

    @ConfigProperty(name = "pathToDownloadFolder") 
    String pathToDownloadFolder;
    
    @ConfigProperty(name = "pathToUnzipFolder") 
    String pathToUnzipFolder;    
    
    @ConfigProperty(name = "dataUrls") 
    List<String> dataUrls;
    
    @ConfigProperty(name = "dbHost") 
    String dbHost;

    @ConfigProperty(name = "dbPort") 
    String dbPort;

    @ConfigProperty(name = "dbDatabase") 
    String dbDatabase;

    @ConfigProperty(name = "dbSchema") 
    String dbSchema;

    @ConfigProperty(name = "dbUser") 
    String dbUser;

    @ConfigProperty(name = "dbPwd") 
    String dbPwd;
   
    @Inject
    AppConfig appConfig;
    
    @Inject
    CamelContext context;

    @Override
    public void configure() throws Exception {
        // TODO: send email
        onException(Exception.class)
        .handled(true)
        .log(LoggingLevel.ERROR, simple("${exception.stacktrace}").getText())
        .log(LoggingLevel.ERROR, simple("${exception.message}").getText());

        FileIdempotentRepository fileConsumerRepository = appConfig.fileConsumerRepo();
        
        for (String dataUrl : dataUrls) {
            // TODO: Use scheduler component and cron magic.
            from("timer:httpRequestTrigger?repeatCount=1")
            .routeId("_sendToSeda__"+dataUrl+"_")   
            .enrich(dataUrl)
            .log(LoggingLevel.INFO, "Send to seda: " + dataUrl)  
            .process(new Processor() {
                @Override
                public void process(Exchange exchange) throws Exception {
                    exchange.getIn().setHeader(Exchange.FILE_NAME, dataUrl.substring(dataUrl.lastIndexOf("/")+1));
                }
            })
            .to("seda:downloadZipFiles");
        }

        from("seda:downloadZipFiles")
        .routeId("_download_")
        .log(LoggingLevel.INFO, "Downloading: ${header.CamelFileName}")
        .streamCaching() // make message re-readable
        .to("file://"+pathToDownloadFolder)
        .split(new ZipSplitter())
        .streaming().convertBodyTo(String.class, "ISO-8859-1") 
            .choice()
                .when(simple("${file:name.ext.single} == 'itf' || ${file:name.ext.single} == 'ITF'"))
                    .setHeader(Exchange.FILE_NAME, simple("${file:name.noext}.${file:name.ext.single}"))
                    .to("file://"+pathToUnzipFolder+"?charset=ISO-8859-1")
            .end()
        .end();
        
        from("file://"+pathToUnzipFolder+"/?noop=true&charset=ISO-8859-1&include=.*\\.itf&delay=30000&initialDelay=5000&readLock=changed&readLockMinAge=120s")
        .routeId("_ili2pg_")
        //.idempotentConsumer(simple("ili2pg-${file:name}-${file:size}-${file:modified}"), fileConsumerRepository)
        .idempotentConsumer(simple("ili2pg-${file:name}-${file:size}"), fileConsumerRepository)
        .log(LoggingLevel.INFO, "Importing File: ${in.header.CamelFileNameOnly}")        
        .setProperty("dbhost", constant(dbHost))
        .setProperty("dbport", constant(dbPort))
        .setProperty("dbdatabase", constant(dbDatabase))
        .setProperty("dbschema", constant(dbSchema))
        .setProperty("dbusr", constant(dbUser))
        .setProperty("dbpwd", constant(dbPwd))
        .setProperty("dataset", simple("${header.CamelFileName}"))
        .process(new Ili2pgReplaceProcessor());
    }
}
