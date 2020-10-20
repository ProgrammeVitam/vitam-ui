@Api
@ApiReferential
@ApiReferentialUnits
@ApiReferentialUnitsGet
Feature: API units : récupérer

Scenario: des unités avec une requête dsl invalide
    When un utilisateur recherche des unités avec une requête dsl invalide
    Then le serveur indique_que_la_requête_est_invalide

Scenario: des unités avec une requête dsl valide mais sans identifiant d'unité
    When un utilisateur recherche des unités avec une requête dsl valide sans identifiant d'unité
    Then le serveur retourne une réponse valide

Scenario: des unités avec une requête dsl valide mais avec un identifiant d'unité inconnu
    When un utilisateur recherche des unités avec une requête dsl valide mais un identifiant d'unité inconnu
    Then le serveur indique que l'unité n'existe pas

Scenario: des unités archivistiques avec une requête dsl valide mais avec un identifiant d'unité inconnu
    When un utilisateur recherche des unités archivistiques avec une requête dsl valide mais un identifiant d'unité inconnu
    Then le serveur indique que l'unité archivistique n'existe pas





 
