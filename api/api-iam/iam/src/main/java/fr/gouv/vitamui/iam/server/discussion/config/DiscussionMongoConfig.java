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
package fr.gouv.vitamui.iam.server.discussion.config;

import com.mongodb.ConnectionString;
import fr.gouv.vitamui.commons.mongo.repository.impl.VitamUIRepositoryImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.SimpleReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * Dedicated MongoDB configuration for the discussion feature.
 *
 * Discussions are stored in their own database, configured with {@code discussion.mongodb.uri}.
 * When this property is not set, the main IAM database ({@code spring.mongodb.uri}) is used, so
 * existing deployments keep working unchanged until their data is migrated.
 */
@Configuration
@EnableMongoRepositories(
    basePackages = "fr.gouv.vitamui.iam.server.discussion.dao",
    repositoryBaseClass = VitamUIRepositoryImpl.class,
    mongoTemplateRef = DiscussionMongoConfig.DISCUSSION_MONGO_TEMPLATE
)
public class DiscussionMongoConfig {

    public static final String DISCUSSION_MONGO_TEMPLATE = "discussionMongoTemplate";
    public static final String DISCUSSION_REACTIVE_MONGO_TEMPLATE = "discussionReactiveMongoTemplate";
    public static final String DISCUSSION_MONGO_DATABASE_FACTORY = "discussionMongoDatabaseFactory";
    public static final String DISCUSSION_REACTIVE_MONGO_DATABASE_FACTORY = "discussionReactiveMongoDatabaseFactory";

    @Value("${discussion.mongodb.uri:${spring.mongodb.uri}}")
    private String discussionMongoUri;

    @Bean(DISCUSSION_MONGO_DATABASE_FACTORY)
    public MongoDatabaseFactory discussionMongoDatabaseFactory() {
        return new SimpleMongoClientDatabaseFactory(discussionMongoUri);
    }

    @Bean(DISCUSSION_MONGO_TEMPLATE)
    public MongoTemplate discussionMongoTemplate(
        @Qualifier(DISCUSSION_MONGO_DATABASE_FACTORY) final MongoDatabaseFactory discussionMongoDatabaseFactory,
        final MappingMongoConverter mappingMongoConverter
    ) {
        return new MongoTemplate(discussionMongoDatabaseFactory, mappingMongoConverter);
    }

    @Bean(DISCUSSION_REACTIVE_MONGO_DATABASE_FACTORY)
    public ReactiveMongoDatabaseFactory discussionReactiveMongoDatabaseFactory() {
        return new SimpleReactiveMongoDatabaseFactory(new ConnectionString(discussionMongoUri));
    }

    @Bean(DISCUSSION_REACTIVE_MONGO_TEMPLATE)
    public ReactiveMongoTemplate discussionReactiveMongoTemplate(
        @Qualifier(
            DISCUSSION_REACTIVE_MONGO_DATABASE_FACTORY
        ) final ReactiveMongoDatabaseFactory discussionReactiveMongoDatabaseFactory,
        final MappingMongoConverter mappingMongoConverter
    ) {
        return new ReactiveMongoTemplate(discussionReactiveMongoDatabaseFactory, mappingMongoConverter);
    }
}
