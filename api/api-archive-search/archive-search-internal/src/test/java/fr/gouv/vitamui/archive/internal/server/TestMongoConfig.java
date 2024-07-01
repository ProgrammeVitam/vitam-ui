/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package fr.gouv.vitamui.archive.internal.server;

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
import org.jetbrains.annotations.NotNull;
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

    @NotNull
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
}
