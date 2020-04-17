@Api
@ApiIam
@ApiIamApplication
@ApiIamApplicationGet
Feature: API applications : récupérer

  Scenario: Toutes les applications
    When un utilisateur récupère toutes les applications dans un tenant auquel il est autorisé en utilisant un certificat full access
    Then le serveur retourne toutes les applications

  Scenario: Seulement les applications autorisées
    When un utilisateur récupère toutes les applications dans un tenant auquel il est autorisé avec un accès limité aux applications
    Then le serveur retourne les applications autorisées

  Scenario: Toutes les applications sans filterApp
    When un utilisateur récupère toutes les applications dans un tenant auquel il est autorisé sans filtre sur les applications
    Then le serveur retourne toutes les applications non filtrées
