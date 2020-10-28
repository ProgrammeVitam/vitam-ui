package fr.gouv.vitamui.cucumber.common.context;

import fr.gouv.vitamui.commons.api.domain.GroupDto;
import fr.gouv.vitamui.commons.api.domain.OwnerDto;
import fr.gouv.vitamui.commons.api.domain.ProfileDto;
import fr.gouv.vitamui.commons.api.domain.TenantDto;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.iam.common.dto.CustomerDto;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.common.dto.SubrogationDto;
import fr.gouv.vitamui.referential.common.dto.ContextDto;
import fr.gouv.vitamui.referential.common.dto.RuleDto;
import fr.gouv.vitamui.referential.common.dto.AccessContractDto;
import fr.gouv.vitamui.referential.common.dto.IngestContractDto;
import fr.gouv.vitamui.referential.common.dto.SecurityProfileDto;

public class TestContext {

    public RuntimeException exception;

    public String tokenUser;

    public int tenantIHMContext;

    public String[] certificateRoles;

    public Integer[] certificateTenants;

    public boolean fullAccess;

    public int mainTenant;

    public int otherTenant;

    public String defaultRole;

    public boolean bResponse;

    public CustomerDto basicCustomerDto;

    public CustomerDto savedBasicCustomerDto;

    public IdentityProviderDto identityProviderDto;

    public IdentityProviderDto savedIdentityProviderDto;

    public SubrogationDto savedSubrogationDto;

    public TenantDto tenantDto;

    public TenantDto savedTenantDto;

    public AuthUserDto authUserDto;

    public String level;

    public String superUserEmail;

    public String customerId;

    public String requestId;

    public GroupDto groupDto;

    public GroupDto savedGroupDto;

    public ProfileDto profileDto;

    public ProfileDto savedProfileDto;

    public UserDto userDto;

    public UserDto savedUserDto;

    public OwnerDto ownerDto;

    public OwnerDto savedOwnerDto;
    
    public ContextDto contextDto;

    public ContextDto savedContextDto;

    public RuleDto ruleDto;

    public RuleDto savedRuleDto;
    
    public AccessContractDto accessContractDto;

    public AccessContractDto savedAccessContractDto;

    public IngestContractDto ingestContractDto;

    public IngestContractDto savedIngestContractDto;

    public SecurityProfileDto securityProfileDto;

    public SecurityProfileDto savedSecurityProfileDto;

    public void reset() {
        exception = null;
        tokenUser = null;
        tenantIHMContext = -1;
        certificateRoles = null;
        certificateTenants = null;
        fullAccess = false;
        mainTenant = -1;
        otherTenant = -1;
        defaultRole = null;
        bResponse = false;
        basicCustomerDto = null;
        savedBasicCustomerDto = null;
        identityProviderDto = null;
        savedIdentityProviderDto = null;
        savedSubrogationDto = null;
        tenantDto = null;
        authUserDto = null;
        level = "";
        superUserEmail = null;
        customerId = null;
        requestId = null;
        groupDto = null;
        savedGroupDto = null;
        profileDto = null;
        savedProfileDto = null;
        userDto = null;
        savedUserDto = null;
        ownerDto = null;
        savedOwnerDto = null;
        contextDto = null;
        savedContextDto = null;
        ruleDto = null;
        savedRuleDto = null;
        accessContractDto = null;
        savedAccessContractDto = null;
        ingestContractDto = null;
        savedIngestContractDto = null;
        securityProfileDto = null;
        savedSecurityProfileDto = null;
    }
}
