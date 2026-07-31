/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 * contact.vitam@culture.gouv.fr
 * This software is governed by the CeCILL 2.1 license.
 */

package fr.gouv.vitamui.authserver.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Serves the tiny vanilla SPAs bundled under {@code src/main/resources/static/}. Spring's static
 * handler doesn't auto-map {@code /foo} to {@code /foo/index.html}, so we do that explicitly for
 * each entry point.
 */
@Controller
public class LoginPageController {

    @GetMapping("/login")
    public String loginPage() {
        return "forward:/login/index.html";
    }

    @GetMapping("/change-password")
    public String changePasswordPage() {
        return "forward:/change-password/index.html";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage() {
        return "forward:/reset-password/index.html";
    }
}
