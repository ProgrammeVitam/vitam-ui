package fr.gouv.vitamui.commons.mongo.config;

import com.mongodb.MongoClient;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import fr.gouv.vitamui.commons.api.converter.OffsetDateTimeToStringConverter;
import fr.gouv.vitamui.commons.api.converter.StringToOffsetDateTimeConverter;
import fr.gouv.vitamui.commons.mongo.repository.impl.VitamUIRepositoryImpl;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.CustomConversions;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableMongoRepositories(basePackages = {
    "fr.gouv.vitamui.commons.mongo.dao",
    "fr.gouv.vitamui.commons.mongo.repository"
}, repositoryBaseClass = VitamUIRepositoryImpl.class)

public class TestMongoConfig extends AbstractMongoConfiguration {

    private static final MongodStarter starter = MongodStarter.getDefaultInstance();

    private final String MONGO_DB_NAME = "test";
    private final String MONGO_HOST = "localhost";

    private MongodExecutable _mongodExe;
    private MongodProcess _mongod;
    private int port;

    @PostConstruct
    public void initIt() throws Exception {
        port = Network.getFreeServerPort();

        _mongodExe = starter.prepare(new MongodConfigBuilder()
            .version(Version.Main.PRODUCTION)
            .net(new Net(MONGO_HOST, port, Network.localhostIsIPv6()))
            .build());

        _mongod = _mongodExe.start();
    }

    @PreDestroy
    public void close() {
        if (_mongod != null)
            _mongod.stop();

        if (_mongodExe != null)
            _mongodExe.stop();
    }

    @Override
    protected String getDatabaseName() {
        return MONGO_DB_NAME;
    }

    @Override
    public MongoClient mongoClient() {
        return new MongoClient(MONGO_HOST, port);
    }

    @Override
    protected String getMappingBasePackage() {
        return "fr.gouv.vitamui.commons.mongo";
    }

    @Override
    public CustomConversions customConversions() {
        final List<Converter<?, ?>> converterList = new ArrayList<>();
        converterList.add(new OffsetDateTimeToStringConverter());
        converterList.add(new StringToOffsetDateTimeConverter());
        return new MongoCustomConversions(converterList);
    }

}
