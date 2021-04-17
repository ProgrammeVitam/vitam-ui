package fr.gouv.vitamui.commons.logbook;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;


import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import fr.gouv.vitamui.commons.logbook.dao.EventRepository;
import fr.gouv.vitamui.commons.mongo.repository.CommonsMongoRepository;
import fr.gouv.vitamui.commons.mongo.repository.impl.VitamUIRepositoryImpl;
import org.springframework.test.context.TestPropertySource;

@Configuration
@EnableMongoRepositories(
        basePackageClasses = {EventRepository.class, CommonsMongoRepository.class},
        repositoryBaseClass = VitamUIRepositoryImpl.class)
@TestPropertySource(properties = { "spring.config.name=common-logbook" })
public class TestMongoConfig extends AbstractMongoClientConfiguration {


    private static final MongodStarter starter = MongodStarter.getDefaultInstance();

    private final String MONGO_DB_NAME = "db";
    private final String MONGO_HOST= "localhost";

    private MongodExecutable _mongodExe;
    private MongodProcess _mongod;
    private int port ;

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
    public void close(){
        if (_mongod != null) {
            _mongod.stop();
        }

        if (_mongodExe != null) {
            _mongodExe.stop();
        }
    }

    @Override
    protected String getDatabaseName() {
        return MONGO_DB_NAME;
    }

}
