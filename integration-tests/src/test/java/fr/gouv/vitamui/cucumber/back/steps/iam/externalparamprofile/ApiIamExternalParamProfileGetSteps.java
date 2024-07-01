package fr.gouv.vitamui.cucumber.back.steps.iam.externalparamprofile;

import fr.gouv.vitamui.commons.api.domain.ExternalParamProfileDto;
import fr.gouv.vitamui.commons.api.domain.ExternalParametersDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.domain.ProfileDto;
import fr.gouv.vitamui.cucumber.common.CommonSteps;
import fr.gouv.vitamui.utils.FactoryDto;
import fr.gouv.vitamui.utils.TestConstants;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class ApiIamExternalParamProfileGetSteps extends CommonSteps {

    private List<ExternalParamProfileDto> externalParamProfileDtoList;
    private ExternalParamProfileDto externalParamProfileDto;

    @When("^un utilisateur avec le rôle ROLE_CREATE_PROFILES crée un profil avec référence au paramétrage externe")
    public void un_utilisateur_avec_le_rôle_ROLE_CREATE_PROFILES_crée_un_profil_de_paramétrage_externe() {
        deleteProfile(TestConstants.EXTERNAL_PARAM_IDENTIFEIR);
        deleteExternalParameter(TestConstants.EXTERNAL_PARAM_IDENTIFEIR);
        ProfileDto profile = FactoryDto.buildDto(ProfileDto.class);
        profile.setExternalParamId(TestConstants.EXTERNAL_PARAM_IDENTIFEIR);
        profile.setId(null);
        profile.setTenantIdentifier(proofTenantIdentifier);
        testContext.savedProfileDto = profile;
        testContext.profileDto = getProfileRestClient()
            .create(getSystemTenantUserAdminContext(), testContext.savedProfileDto);
    }

    @When("^un utilisateur crée un paramétrage externe")
    public void un_utilisateur_crée_un_paramétrage_externe() {
        ExternalParametersDto externalParameters = FactoryDto.buildDto(ExternalParametersDto.class);
        writeExternalParameter(externalParameters);
        testContext.savedExternalParametersDto = externalParameters;
        testContext.externalParametersDto = externalParameters;
    }

    @When("^un utilisateur avec le rôle ROLE_SEARCH_EXTERNAL_PARAM_PROFILE cherche un profile externe avec id")
    public void un_utilisateur_avec_le_rôle_ROLE_SEARCH_EXTERNAL_PARAM_PROFILE_récupère_les_résultas() {
        externalParamProfileDto = getExternalParamProfileExternalRestClient()
            .getOne(getSystemTenantUserAdminContext(), testContext.profileDto.getId());
    }

    @Then("^le serveur retourne le profil externe associé$")
    public void le_serveur_retourne_profil_externe_associé() {
        assertThat(externalParamProfileDto.getExternalParamIdentifier()).isEqualTo(
            TestConstants.EXTERNAL_PARAM_IDENTIFEIR
        );
    }

    @When(
        "^un utilisateur avec le rôle ROLE_SEARCH_EXTERNAL_PARAM_PROFILE récupère toutes les entrées des profils et du paramétrage externe par page associé à son profil en utilisant un certificat full access avec le rôle ROLE_SEARCH_EXTERNAL_PARAM_PROFILE$"
    )
    public void un_utilisateur_avec_le_rôle_ROLE_SEARCH_EXTERNAL_PARAM_PROFILE_récupère_les_entrées_des_profils_et_du_paramétrage_externe_par_page_associé_à_son_profil_en_utilisant_un_certificat_full_access_avec_le_rôle_ROLE_SEARCH_EXTERNAL_PARAM_PROFILE() {
        final PaginatedValuesDto<ExternalParamProfileDto> allPaginated = getExternalParamProfileExternalRestClient()
            .getAllPaginated(
                getSystemTenantUserAdminContext(),
                0,
                20,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
            );

        externalParamProfileDtoList = new ArrayList<>(allPaginated.getValues());
    }

    @Then("^le serveur retourne la totalité des résultats")
    public void le_serveur_retourne_la_totalité_des_résultats() {
        assertThat(externalParamProfileDtoList).isNotNull();
        assertThat(externalParamProfileDtoList.size()).isGreaterThan(0);
    }
}
