package org.apereo.cas.authentication;

import lombok.Getter;
import lombok.Setter;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;

@Getter
@Setter
public class SurrogateUsernamePasswordCredential extends UsernamePasswordCredential {

    private String surrogateUsername;
}
