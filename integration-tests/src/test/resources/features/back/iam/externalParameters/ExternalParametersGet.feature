@Api
@ApiIam
@ApiIamExternalParameters
@ApiIamParametersGet
Feature: API external parameters : récupérer

  Scenario: les paramètres externes
    When un utilisateur avec le rôle ROLE_GET_EXTERNAL_PARAMS récupère le paramétrage associé à son profl en utilisant un certificat full access avec le rôle ROLE_GET_EXTERNAL_PARAMS
    Then le serveur retourne le paramétrage associé au profil de l'utilisateur
