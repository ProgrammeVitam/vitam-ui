/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 * contact.vitam@culture.gouv.fr
 * This software is governed by the CeCILL 2.1 license.
 */
package fr.gouv.vitamui.iam.common.dto.cas;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Subset of {@code PasswordConfiguration} exposed to the SPA so it can render the rules a new
 * password must satisfy — used on both the change and reset screens. Kept intentionally flat and
 * front-facing: no internal profile/constraint machinery, just the numbers and messages a human
 * needs to see before typing.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordPolicyDto {

    /** Minimum password length (from {@code password.length}). */
    private Integer minLength;

    /** Underlying profile name (e.g. {@code anssi}, {@code custom}) — for the SPA to badge/label. */
    private String profile;

    /**
     * Number of past passwords a user cannot reuse (from {@code password.maxOldPassword}). Front-end
     * shows this as "ne pas réutiliser vos N derniers mots de passe".
     */
    private Integer maxOldPassword;

    /**
     * Human-readable rules the SPA renders as a bullet list. Populated from the messages defined
     * under {@code password.constraints.*} in the active profile.
     */
    private List<String> messages;
}
