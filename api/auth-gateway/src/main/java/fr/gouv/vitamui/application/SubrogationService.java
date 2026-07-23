/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */

package fr.gouv.vitamui.application;

import fr.gouv.vitamui.api.AccessTokenResponse;
import fr.gouv.vitamui.api.SubrogationRequest;
import fr.gouv.vitamui.domain.ApplicationUser;
import fr.gouv.vitamui.domain.Identity;
import fr.gouv.vitamui.domain.SecurityContext;
import fr.gouv.vitamui.domain.ports.IdentityProviderResolver;
import fr.gouv.vitamui.domain.ports.SubrogationRepository;
import fr.gouv.vitamui.domain.ports.TokenGenerator;
import fr.gouv.vitamui.exception.SubrogationNotAllowedException;
import fr.gouv.vitamui.iam.common.enums.SubrogationStatusEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service de contrôle des règles métier de subrogation.
 *
 * <p>Règles :
 * <ul>
 *   <li><b>GENERIC</b> : peut être subrogé par tout utilisateur portant le rôle ROLE_SUBROGATION.</li>
 *   <li><b>NOMINATIVE</b> : peut être subrogé uniquement par un utilisateur portant le rôle ROLE_SUBROGATION
 *       ET uniquement pendant une fenêtre de temps active accordée explicitement.</li>
 *   <li><b>SERVICE</b> : ne peut jamais être subrogé.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class SubrogationService {

    public final String ROLE_SUBROGATION = "ROLE_SUBROGATION";

    private final IdentityProviderResolver resolver;
    private final TokenGenerator generator;
    private final SecurityContextService securityContextService;
    private final SubrogationRepository repository;

    /**
     * Subrogation explicite : l'administrateur prend l'identité du compte cible.
     * Le jeton retourné porte les droits du compte subrogé, avec une durée de vie réduite.
     */
    public AccessTokenResponse subrogate(String identityToken, SubrogationRequest request) {
        Identity identity = resolver.authenticate(identityToken);
        SecurityContext adminContext = securityContextService.create(identity);

        ApplicationUser targetUser = securityContextService.resolveTargetUser(
            request.getSurrogateUserEmail(),
            request.getSurrogateCustomerId()
        );

        if (!allowed(adminContext.authenticatedUser(), targetUser)) {
            throw new SubrogationNotAllowedException(targetUser.id().toString());
        }

        SecurityContext subrogatedContext = securityContextService.subrogate(adminContext, targetUser);
        String accessToken = generator.generate(subrogatedContext);
        return new AccessTokenResponse(accessToken);
    }

    boolean allowed(ApplicationUser subrogator, ApplicationUser target) {
        if (!hasSubrogationRole(subrogator)) {
            return false;
        }

        return switch (target.accountType()) {
            case GENERIC -> true;
            case NOMINATIVE -> hasActiveSubrogation(subrogator, target);
        };
    }

    private boolean hasSubrogationRole(ApplicationUser user) {
        return user.permissions() != null && user.permissions().contains(ROLE_SUBROGATION);
    }

    /**
     * Une subrogation "active" = un document encore présent en base (le TTL Mongo sur
     * `date` supprime automatiquement les subrogations expirées) et au statut ACCEPTED.
     */
    private boolean hasActiveSubrogation(ApplicationUser subrogator, ApplicationUser target) {
        return repository
            .findBySuperUserAndSurrogate(subrogator.login(), target.login())
            .map(s -> s.getStatus() == SubrogationStatusEnum.ACCEPTED)
            .orElse(false);
    }
}
