@Api
@ApiIam
@ApiIamSubrogations
@ApiIamSubrogationsAccept
Feature: Opérations d'acceptation des subrogations

  Scenario: Accepter une subrogation
  	Given l'utilisateur admin@vitamui.com n'a pas de subrogations en cours
    And une subrogation a été créée pour le bon utilisateur
    When un utilisateur accepte la subrogation
    Then le serveur retourne la subrogation acceptée

  Scenario: Accepter une subrogation d'un autre utilisateur
  	Given l'utilisateur admin@vitamui.com n'a pas de subrogations en cours
    And une subrogation a été créée pour le bon utilisateur
    When un autre utilisateur accepte la subrogation
    Then le serveur refuse d'accepter la subrogation
