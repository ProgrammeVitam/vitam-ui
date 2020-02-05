@Api
@ApiIam
@ApiIamSubrogations
@ApiIamSubrogationsCheck
Feature: Opérations de vérification des subrogations

  Scenario: Vérifier l'existence d'une subrogation par son identifant
    When un utilisateur vérifie l'existence d'une subrogation par son identifiant
    Then le serveur refuse la vérification de l'existence car l'opération n'est pas implémentée
