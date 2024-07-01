package fr.gouv.vitamui.iam.internal.server;

import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.connection.ClusterSettings;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import fr.gouv.vitamui.commons.api.converter.OffsetDateTimeToStringConverter;
import fr.gouv.vitamui.commons.api.converter.StringToOffsetDateTimeConverter;
import fr.gouv.vitamui.commons.api.identity.ServerIdentityAutoConfiguration;
import fr.gouv.vitamui.commons.mongo.repository.impl.VitamUIRepositoryImpl;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Collections;

@Configuration
@EnableMongoRepositories(
    basePackages = { "fr.gouv.vitamui.commons.mongo.repository" },
    repositoryBaseClass = VitamUIRepositoryImpl.class
)
@Import({ ServerIdentityAutoConfiguration.class })
public class TestMongoConfig extends AbstractMongoClientConfiguration {

    private static final MongodStarter starter = MongodStarter.getDefaultInstance();

    private final String MONGO_HOST = "localhost";

    private MongodExecutable _mongodExe;

    private MongodProcess _mongod;

    private int port;

    @PostConstruct
    public void initIt() throws Exception {
        port = Network.getFreeServerPort();

        _mongodExe = starter.prepare(
            MongodConfig.builder()
                .version(Version.Main.PRODUCTION)
                .net(new Net(MONGO_HOST, port, Network.localhostIsIPv6()))
                .build()
        );

        _mongod = _mongodExe.start();
    }

    @PreDestroy
    public void close() {
        if (_mongod != null) {
            _mongod.stop();
        }

        if (_mongodExe != null) {
            _mongodExe.stop();
        }
    }

    @Override
    protected void configureClientSettings(MongoClientSettings.Builder builder) {
        ClusterSettings clusterSettings = ClusterSettings.builder()
            .hosts(Collections.singletonList(new ServerAddress(MONGO_HOST, port)))
            .build();
        builder.applyToClusterSettings(b -> b.applySettings(clusterSettings));
    }

    @Override
    protected String getDatabaseName() {
        return "db";
    }

    @Override
    protected void configureConverters(
        MongoCustomConversions.MongoConverterConfigurationAdapter converterConfigurationAdapter
    ) {
        converterConfigurationAdapter.registerConverter(new OffsetDateTimeToStringConverter());
        converterConfigurationAdapter.registerConverter(new StringToOffsetDateTimeConverter());
    }

    @Override
    // Spring Data MongoDB can automatically create indexes for entity types annotated with @Document.
    // Index creation must be explicitly enabled since version 3.0 to prevent undesired effects with
    // collection lifecycle and performance impact.
    protected boolean autoIndexCreation() {
        return true;
    }
}
