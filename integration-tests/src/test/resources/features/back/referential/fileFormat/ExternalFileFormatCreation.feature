@Api
@ApiReferential
@ApiReferentialFileFormats
@ApiReferentialFileFormatsCreation
Feature: API FileFormats : Création et import de formats de fichier

  Scenario: Import d'un fichier référentiel de formats de fichier valide
    When un utilisateur importe des formats de fichier à partir d'un fichier xml valide
    Then l'import des formats de fichier a réussi

  Scenario: Import d'un fichier référentiel de formats de fichier invalide
    When un utilisateur importe des formats de fichier à partir d'un fichier xml invalide
    Then l'import des formats de fichier a échoué
