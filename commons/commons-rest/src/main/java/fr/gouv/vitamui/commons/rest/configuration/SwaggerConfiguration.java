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
package fr.gouv.vitamui.commons.rest.configuration;

import fr.gouv.vitamui.commons.api.CommonConstants;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.util.MimeTypeUtils;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.RequestParameterBuilder;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.RequestParameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Enables the Swagger API documentation.
 * <p>
 * Call swagger-ui in our case http://localhost:[port]/swagger-ui/index.html
 */
@Configuration
//@Profile("swagger")
@EnableOpenApi
@PropertySource(value = {"classpath:swagger-${swagger.layer:default}.properties"}, encoding = "UTF-8")
public class SwaggerConfiguration {

    private static final String PARAMETER_TYPE_HEADER = "header";
    @Value("${spring.api.name}")
    @NotNull
    @Setter
    private String apiName;

    @Value("${swagger.layer:default}")
    @NotNull
    @Setter
    private String layer;

    @Value("${spring.api.description}")
    @NotNull
    @Setter
    private String apiDescription;

    @Value("${spring.api.version}")
    @NotNull
    @Setter
    private String apiVersion;

    @Value("${swagger.header.tenant.enabled}")
    @NotNull
    @Setter
    private Boolean tenantHeaderEnabled;

    @Value("${swagger.header.tenant.description}")
    @NotNull
    @Setter
    private String tenantHeaderDescription;

    @Value("${swagger.header.xsrfToken.enabled}")
    @NotNull
    @Setter
    private Boolean xrsfTokenHeaderEnabled;

    @Value("${swagger.header.userToken.enabled}")
    @NotNull
    @Setter
    private Boolean userTokenHeaderEnabled;

    @Value("${swagger.header.userToken.description}")
    @NotNull
    @Setter
    private String userTokenHeaderDescription;

    @Value("${swagger.header.applicationId.enabled}")
    @NotNull
    @Setter
    private Boolean applicationIdHeaderEnabled;

    @Value("${swagger.header.applicationId.description}")
    @NotNull
    @Setter
    private String applicationIdHeaderDescription;

    @Value("${swagger.header.applicationId.required}")
    @NotNull
    @Setter
    private Boolean applicationHeaderRequired;

    @Value("${swagger.header.requestId.enabled}")
    @NotNull
    @Setter
    private Boolean requestIdHeaderEnabled;

    @Value("${swagger.header.accessContract.enabled}")
    @NotNull
    @Setter
    private Boolean accessContractHeaderEnabled;

    public static final String SWAGGER_API = "/v3/api-docs";

    @Bean
    public Docket api() {
        final List<RequestParameter> parameters = new ArrayList<>();
        // @formatter:off

        if (tenantHeaderEnabled) {
            final RequestParameter tenantHeader = new RequestParameterBuilder()
                .name(CommonConstants.X_TENANT_ID_HEADER)
                .description(tenantHeaderDescription)
                //.contentModel(new ModelSpecification("int"))
                //.parameterType(PARAMETER_TYPE_HEADER)
                .required(true).build();
            parameters.add(tenantHeader);
        }

        if (xrsfTokenHeaderEnabled) {
            final RequestParameter xsrfHeader = new RequestParameterBuilder()
                //contentModel(MODEL_REF_TYPE);
                .description("XSRF Token")
                .required(false)
                .name(CommonConstants.X_XSRF_TOKEN_HEADER).build();
            parameters.add(xsrfHeader);
        }

        if (userTokenHeaderEnabled) {
            final RequestParameter userTokenHeader = new RequestParameterBuilder()
                //(MODEL_REF_TYPE);
                .description(userTokenHeaderDescription)
                .required(true)
                .name(CommonConstants.X_USER_TOKEN_HEADER).build();
            parameters.add(userTokenHeader);

        }

        if (applicationIdHeaderEnabled) {

            final RequestParameter applicationIdHeader = new RequestParameterBuilder()
                .name(CommonConstants.X_APPLICATION_ID_HEADER)
                .description(applicationIdHeaderDescription)
                .in(PARAMETER_TYPE_HEADER)
                //.modelRef(new ModelRef(MODEL_REF_TYPE))
                .required(applicationHeaderRequired).build();

            parameters.add(applicationIdHeader);
        }

        if (requestIdHeaderEnabled) {
            final RequestParameter requestIdHeader = new RequestParameterBuilder()
                .name(CommonConstants.X_REQUEST_ID_HEADER)
                .description("X-request-Id")
                //.modelRef(new ModelRef(MODEL_REF_TYPE))
                .in(PARAMETER_TYPE_HEADER)
                .required(true).build();
            parameters.add(requestIdHeader);
        }

        if (accessContractHeaderEnabled) {
            final RequestParameter requestIdHeader = new RequestParameterBuilder()
                .name(CommonConstants.X_ACCESS_CONTRACT_ID_HEADER)
                .description("X-Access-Contract-Id")
                //.modelRef(new ModelRef(MODEL_REF_TYPE))
                .in(PARAMETER_TYPE_HEADER)
                .required(true).build();
            parameters.add(requestIdHeader);
        }

        Set<String> produces = Collections.singleton(MimeTypeUtils.APPLICATION_JSON_VALUE);


        return new Docket(DocumentationType.OAS_30)
            .apiInfo(apiInfo())
            .select()
            .apis(RequestHandlerSelectors.any())
            .paths(PathSelectors.any())
            .build()
            .produces(produces)
            .globalRequestParameters(parameters)
            .useDefaultResponseMessages(false)
            .pathMapping("/")
            .genericModelSubstitutes(Optional.class);
        // @formatter:on
    }

    protected ApiInfo apiInfo() {
        // @formatter:off
        return new ApiInfo(apiName,
            apiDescription,
            apiVersion,
            "Terms of service",
            new Contact("Direction de la diffusion et des partenariats", "", "contact@programmevitam.fr"),
            "Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022) " +
                "and the signatories of the VITAM - Accord du Contributeur agreement.",
            "https://github.com/ProgrammeVitam/vitam-ui/blob/develop/LICENSE.txt",
            Collections.emptyList());
        // @formatter:on
    }

}
