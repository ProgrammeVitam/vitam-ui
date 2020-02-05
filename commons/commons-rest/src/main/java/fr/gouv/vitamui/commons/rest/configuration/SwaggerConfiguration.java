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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.RequestMethod;

import fr.gouv.vitamui.commons.api.CommonConstants;
import lombok.Setter;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.ResponseMessageBuilder;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Parameter;
import springfox.documentation.service.ResponseMessage;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Enables the Swagger API documentation.
 * <p>
 * Call swagger-ui in our case http://localhost:[port]/swagger-ui.html#/
 */
@Configuration
@EnableSwagger2
@Profile("swagger")
@PropertySource(value = { "classpath:swagger-${swagger.layer:default}.properties" }, encoding = "UTF-8")
public class SwaggerConfiguration {

    private static final String MODEL_REF_TYPE = "string";

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

    public static final String HTTP_CODE_401_MSG = "La requête n'est pas autorisée. Le X-User-Token n'est pas valide";

    public static final String HTTP_CODE_403_MSG = "L'utilisateur ne possède pas les droits pour effectuer cette opération";

    public static final String HTTP_CODE_404_MSG = "Non trouvée";

    public static final String SWAGGER_API = "/swagger.json";

    @Bean
    public Docket api() {
        final List<Parameter> parameters = new ArrayList<>();
        // @formatter:off

        if(tenantHeaderEnabled) {
            final Parameter tenantHeader = new ParameterBuilder()
                    .name(CommonConstants.X_TENANT_ID_HEADER)
                    .description(tenantHeaderDescription)
                    .modelRef(new ModelRef("int"))
                    .parameterType(PARAMETER_TYPE_HEADER)
                    .required(true).build();
            parameters.add(tenantHeader);
        }

        if(xrsfTokenHeaderEnabled) {
            final Parameter xsrfHeader = new ParameterBuilder()
                    .name(CommonConstants.X_XSRF_TOKEN_HEADER)
                    .description("XSRF Token")
                    .modelRef(new ModelRef(MODEL_REF_TYPE))
                    .parameterType(PARAMETER_TYPE_HEADER).required(false).build();

            parameters.add(xsrfHeader);
        }

        if(userTokenHeaderEnabled) {
            final Parameter userTokenHeader = new ParameterBuilder()
                    .name(CommonConstants.X_USER_TOKEN_HEADER)
                    .description(userTokenHeaderDescription)
                    .modelRef(new ModelRef(MODEL_REF_TYPE))
                    .parameterType(PARAMETER_TYPE_HEADER).required(true).build();

            parameters.add(userTokenHeader);
        }

        if(applicationIdHeaderEnabled) {
            final Parameter applicationIdHeader = new ParameterBuilder()
                    .name(CommonConstants.X_APPLICATION_ID_HEADER)
                    .description(applicationIdHeaderDescription)
                    .modelRef(new ModelRef(MODEL_REF_TYPE))
                    .parameterType(PARAMETER_TYPE_HEADER)
                    .required(applicationHeaderRequired).build();

            parameters.add(applicationIdHeader);
        }

        if(requestIdHeaderEnabled) {
            final Parameter requestIdHeader = new ParameterBuilder()
                    .name(CommonConstants.X_REQUEST_ID_HEADER)
                    .description("X-request-Id")
                    .modelRef(new ModelRef(MODEL_REF_TYPE))
                    .parameterType(PARAMETER_TYPE_HEADER).required(true).build();

            parameters.add(requestIdHeader);
        }

        if(accessContractHeaderEnabled) {
            final Parameter requestIdHeader = new ParameterBuilder()
                    .name(CommonConstants.X_ACCESS_CONTRACT_ID_HEADER)
                    .description("X-Access-Contract-Id")
                    .modelRef(new ModelRef(MODEL_REF_TYPE))
                    .parameterType(PARAMETER_TYPE_HEADER).required(true).build();

            parameters.add(requestIdHeader);
        }


        Set<String> produces = Collections.singleton(MimeTypeUtils.APPLICATION_JSON_VALUE);
        return new Docket(DocumentationType.SWAGGER_2).select()
                .apis(RequestHandlerSelectors
                .any()).paths(PathSelectors.any()).build()
                .apiInfo(apiInfo())
                .produces(produces)
                .globalOperationParameters(parameters)
                .useDefaultResponseMessages(false)
                .globalResponseMessage(RequestMethod.GET,getResponseMessage(RequestMethod.GET))
                .globalResponseMessage(RequestMethod.POST,getResponseMessage(RequestMethod.POST))
                .globalResponseMessage(RequestMethod.PATCH,getResponseMessage(RequestMethod.PATCH))
                .globalResponseMessage(RequestMethod.PUT,getResponseMessage(RequestMethod.PUT))
                .globalResponseMessage(RequestMethod.HEAD,getResponseMessage(RequestMethod.HEAD))
                .pathMapping("/")
                .genericModelSubstitutes(Optional.class);
        // @formatter:on
    }

    private List<ResponseMessage> getResponseMessage(final RequestMethod requestMethod) {
        List<ResponseMessage> responseMessage = new ArrayList<>();
        ResponseMessage responseMessage403 = new ResponseMessageBuilder().code(HttpStatus.FORBIDDEN.value())
                .message(HTTP_CODE_403_MSG).build();
        ResponseMessage responseMessage401 = new ResponseMessageBuilder().code(HttpStatus.UNAUTHORIZED.value())
                .message(HTTP_CODE_401_MSG).build();
        ResponseMessage responseMessage404 = new ResponseMessageBuilder().code(HttpStatus.NOT_FOUND.value())
                .message(HTTP_CODE_404_MSG).build();

        responseMessage.add(responseMessage401);
        responseMessage.add(responseMessage403);
        switch (requestMethod) {
            case GET :
            case PATCH :
            case PUT :
                responseMessage.add(responseMessage404);
                break;
            default :
                break;
        }

        return responseMessage;
    }

    @Bean
    @Primary
    public SwaggerResourcesProvider swaggerResourcesProvider() {
        return new CustomInMemorySwaggerResourcesProvider();
    }

    @Bean
    public SwaggerController swaggerController() {
        return new SwaggerController();
    }

    protected ApiInfo apiInfo() {
        // @formatter:off
        return new ApiInfo(
                apiName,
                apiDescription,
                apiVersion,
                "Terms of service",
                new springfox.documentation.service.Contact("Direction de la diffusion et des partenariats", "", "contact@programmevitam.fr"),
                "License to be defined ...",
                "URL not defined.",
                Collections.emptyList());
        // @formatter:on
    }

}
