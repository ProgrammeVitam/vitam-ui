import {Injectable} from '@angular/core';

export class Permission {
  activated: boolean;
  description: string;
  overridePermission?: string;
  overrideApiPermission?: string;
}

export class APIPermissions {
  service: string;
  type: string;
  name: string;
  readPermission?: Permission;
  writePermission?: Permission;
  deletePermission?: Permission;
}

export class PermissionStructure {
  apiPermissions: APIPermissions[];
}

@Injectable({
  providedIn: 'root'
})
export class PermissionUtils {
  getInitPermissions(defaultPermission: boolean): PermissionStructure {
    return {
      apiPermissions: [
        {
          service: 'managementcontracts',
          type: 'managementcontracts',
          name: 'Contrat de gestion',
          readPermission: {
            activated: defaultPermission,
            description: 'Lister le contenu du référentiel des contrats de gestion',
            overridePermission: null
          },
          writePermission: {
            activated: defaultPermission,
            description: 'Effectuer une mise à jour sur un contrat de gestion',
            overridePermission: 'create:json'
          },
          deletePermission: null
        },
        {
          service: 'managementcontracts:id',
          type: 'managementcontracts',
          name: 'Contrat de gestion (par ID)',
          readPermission: {
            activated: defaultPermission,
            description: 'Lire un contrat de gestion donné',
            overridePermission: null
          },
          writePermission: {
            activated: defaultPermission,
            description: 'Effectuer une mise à jour sur un contrat de gestion',
            overridePermission: null
          },
          deletePermission: null
        },
        {
          service: 'dipexport',
          type: 'dipexport',
          name: 'Export DIP',
          readPermission: {
            activated: defaultPermission,
            description: 'Récupérer le DIP',
            overridePermission: 'id:dip:read'
          },
          writePermission: {
            activated: defaultPermission,
            description: 'Générer le DIP à partir d\'un DSL',
            overridePermission: 'create'
          },
          deletePermission: null
        },
        {
          service: 'dipexportv2',
          type: 'dipexportv2',
          name: 'Export DIP V2',
          readPermission: null,
          writePermission: {
            activated: defaultPermission,
            description: 'Générer le DIP à partir d\'un DSL',
            overridePermission: 'create'
          },
          deletePermission: null
        },
        {
          service: 'transfers',
          type: 'transfers',
          name: 'Transfert de SIP',
          readPermission: {
            activated: defaultPermission,
            description: 'Récupérer le SIP du transfer',
            overridePermission: 'id:sip:read'
          },
          writePermission: {
            activated: defaultPermission,
            description: 'Générer le SIP pour transfer à partir d\'un DSL',
            overridePermission: 'create'
          },
          deletePermission: null
        },
        {
          service: 'transfers',
          type: 'transfers',
          name: 'Transfert de SIP (Reply)',
          readPermission: null,
          writePermission: {
            activated: defaultPermission,
            description: 'Start transfer reply workflow',
            overridePermission: 'reply'
          },
          deletePermission: null
        },
        {
          service: 'logbookoperations',
          type: 'logbookoperations',
          name: 'Journaux des opérations',
          readPermission: {
            activated: defaultPermission,
            description: 'Lister toutes les opérations',
            overridePermission: null
          },
          writePermission: {
            activated: defaultPermission,
            description: 'Créer une opération',
            overridePermission: 'create'
          },
          deletePermission: null
        },
        {
          service: 'logbookoperations:id',
          type: 'logbookoperations',
          name: 'Journaux des opérations (par ID)',
          readPermission: {
            activated: defaultPermission,
            description: 'Récupérer le journal d\'une opération donnée',
            overridePermission: null
          },
          writePermission: null,
          deletePermission: null
        },
        {
          service: 'logbookunitlifecycles:id',
          type: 'logbookunitlifecycles',
          name: 'Cycles de vies (UA)',
          readPermission: {
            activated: defaultPermission,
            description: 'Récupérer le journal de cycle de vie d\'une unité archivistique',
            overridePermission: null
          },
          writePermission: null,
          deletePermission: null
        },
        {
          service: 'logbookobjectslifecycles:id',
          type: 'logbookobjectslifecycles',
          name: 'Cycles de vies (objets)',
          readPermission: {
            activated: defaultPermission,
            description: 'Récupérer le journal de cycle de vie d\'un groupe d\'objet',
            overridePermission: null
          },
          writePermission: null,
          deletePermission: null
        },
        {
          service: 'units',
          type: 'units',
          name: 'Unités Archivistiques',
          readPermission: {
            activated: defaultPermission,
            description: 'Récupérer la liste des unités archivistiques',
            overridePermission: null
          },
          writePermission: {
            activated: defaultPermission,
            description: 'Mise à jour en masse des unités archivistiques',
            overridePermission: null
          },
          deletePermission: null
        },
        {
          service: 'units:id',
          type: 'units',
          name: 'Unités Archivistiques (par ID)',
          readPermission: {
            activated: defaultPermission,
            description: 'Obtenir le détail d\'une unité archivistique au format json',
            overridePermission: 'read:json'
          },
          writePermission: {
            activated: defaultPermission,
            description: 'Réaliser la mise à jour d\'une unité archivistique',
            overridePermission: null
          },
          deletePermission: null
        },
        {
          service: 'units:id:objects',
          type: 'units',
          name: 'Groupe d\'objets (JSON)',
          readPermission: {
            activated: defaultPermission,
            description: 'Télécharger le groupe d\'objet technique de l\'unité archivistique donnée',
            overridePermission: 'read:json'
          },
          writePermission: null,
          deletePermission: null
        },
        {
          service: 'objects',
          type: 'objects',
          name: 'Groupes d\'objets',
          readPermission: {
            activated: defaultPermission,
            description: 'Récupérer la liste des groupes d\'objets',
            overridePermission: null
          },
          writePermission: null,
          deletePermission: null
        },
        {
          service: 'units:id:objects',
          type: 'units',
          name: 'Objets (Binaire)',
          readPermission: {
            activated: defaultPermission,
            description: 'Télecharger un objet',
            overridePermission: 'read:binary'
          },
          writePermission: null,
          deletePermission: null
        },
        {
          service: 'units:rules',
          type: 'units',
          name: 'Règles associées aux Unités archivistiques',
          readPermission: null,
          writePermission: {
            activated: defaultPermission,
            description: 'Mise à jour en masse des règles de gestion',
            overridePermission: null
          },
          deletePermission: null
        },
        {
          service: 'unitsWithInheritedRules',
          type: 'unitsWithInheritedRules',
          name: 'Héritage des règles d\'unités archivistiques',
          readPermission: {
            activated: defaultPermission,
            description: 'Récupérer la liste des unités archivistiques avec leurs règles de gestion héritées',
            overridePermission: null
          },
          writePermission: null,
          deletePermission: null
        },
        {
          service: 'accesscontracts',
          type: 'accesscontracts',
          name: 'Contrats d\'accès',
          readPermission: {
            activated: defaultPermission,
            description: 'Lister le contenu du référentiel des contrats d\'accès',
            overridePermission: null
          },
          writePermission: {
            activated: defaultPermission,
            description: 'Importer des contrats d\'accès dans le référentiel',
            overridePermission: 'create:json'
          },
          deletePermission: null
        },
        {
          service: 'accesscontracts:id',
          type: 'accesscontracts',
          name: 'Contrats d\'accès (par ID)',
          readPermission: {
            activated: defaultPermission,
            description: 'Lire un contrat d\'accès donné',
            overridePermission: null
          },
          writePermission: {
            activated: defaultPermission,
            description: 'Effectuer une mise à jour sur un contrat d\'accès',
            overridePermission: null
          },
          deletePermission: null
        },
        {
          service: 'accessionregisters',
          type: 'accessionregisters',
          name: 'Registre de fonds',
          readPermission: {
            activated: defaultPermission,
            description: 'Lister le contenu du référentiel des registres des fonds',
            overridePermission: null
          },
          writePermission: null,
          deletePermission: null
        },
        {
          service: 'accessionregisters:id',
          type: 'accessionregisters',
          name: 'Registre de fonds (par ID)',
          readPermission: {
            activated: defaultPermission,
            description: 'Lister les détails d\'un registre de fonds',
            overridePermission: 'accessionregisterdetails:read'
          },
          writePermission: null,
          deletePermission: null
        },
        {
          service: 'accessionregisterssymbolic',
          type: 'accessionregisterssymbolic',
          name: 'Registre de fonds (Symbolic)',
          readPermission: {
            activated: defaultPermission,
            description: 'Get accession register symbolic',
            overridePermission: null
          },
          writePermission: null,
          deletePermission: null
        },
        {
          service: 'agencies',
          type: 'agencies',
          name: 'Services producteurs',
          readPermission: {
            activated: defaultPermission,
            description: 'Lister le contenu du référentiel des services producteurs',
            overridePermission: null
          },
          writePermission: {
            activated: defaultPermission,
            description: 'Importer le référentiel des services producteurs',
            overridePermission: 'create'
          },
          deletePermission: null
        },
        {
          service: 'agencies:id',
          type: 'agencies',
          name: 'Services producteurs (par ID)',
          readPermission: {
            activated: defaultPermission,
            description: 'Trouver un service producteur avec son identifier',
            overridePermission: null
          },
          writePermission: null,
          deletePermission: null
        },
        {
          service: 'agenciesfile',
          type: 'agenciesfile',
          name: 'Services producteurs (Validité CSV)',
          readPermission: {
            activated: defaultPermission,
            description: 'Vérifier si le référentiel de services producteurs que l\'on souhaite importer est valide',
            overridePermission: 'check'
          },
          writePermission: null,
          deletePermission: null
        },
        {
          service: 'agenciesreferential:id',
          type: 'agenciesreferential',
          name: 'Export des services producteurs',
          readPermission: {
            activated: defaultPermission,
            description: 'Récupérer le référentiel pour une opération d\'import des service agents',
            overridePermission: null
          },
          writePermission: null,
          deletePermission: null
        },
        {
          service: 'audits',
          type: 'audits',
          name: 'Audit (Existance)',
          readPermission: null,
          writePermission: {
            activated: defaultPermission,
            description: 'Lancer un audit de l\'existance des objets',
            overridePermission: 'create'
          },
          deletePermission: null
        },
        {
          service: 'evidenceaudit',
          type: 'evidenceaudit',
          name: 'Audit (Cohérence)',
          readPermission: null,
          writePermission: {
            activated: defaultPermission,
            description: 'Audit de traçabilité d\'unités archivistiques',
            overridePermission: 'check'
          },
          deletePermission: null
        },
        {
          service: 'contexts',
          type: 'contexts',
          name: 'Contextes applicatifs',
          readPermission: {
            activated: defaultPermission,
            description: 'Lister le contenu du référentiel des contextes',
            overridePermission: null
          },
          writePermission: {
            activated: defaultPermission,
            description: 'Importer des contextes dans le référentiel',
            overridePermission: 'create:json'
          },
          deletePermission: null
        },
        {
          service: 'contexts:id',
          type: 'contexts',
          name: 'Contextes applicatifs (par ID)',
          readPermission: {
            activated: defaultPermission,
            description: 'Lire un contexte donné',
            overridePermission: null
          },
          writePermission: {
            activated: defaultPermission,
            description: 'Effectuer une mise à jour sur un contexte',
            overridePermission: null
          },
          deletePermission: null
        },
        {
          service: 'distributionreport:id',
          type: 'distributionreport',
          name: 'Rapports d\'opérations distribuée',
          readPermission: {
            activated: defaultPermission,
            description: 'Récupérer le rapport pour une opération de mise à jour de masse distribuée',
            overridePermission: null
          },
          writePermission: null,
          deletePermission: null
        },
        {
          service: 'formats',
          type: 'formats',
          name: 'Formats de fichiers',
          readPermission: {
            activated: defaultPermission,
            description: 'Lister le contenu du référentiel des formats',
            overridePermission: null
          },
          writePermission: {
            activated: defaultPermission,
            description: 'Importer un référentiel des formats',
            overridePermission: 'create'
          },
          deletePermission: null
        },
        {
          service: 'formats:id',
          type: 'formats',
          name: 'Formats de fichiers (par ID)',
          readPermission: {
            activated: defaultPermission,
            description: 'Lire un format donné',
            overridePermission: null
          },
          writePermission: null,
          deletePermission: null
        },
        {
          service: 'formatsfile',
          type: 'formatsfile',
          name: 'Formats de fichiers (Validité XML)',
          readPermission: {
            activated: defaultPermission,
            description: 'Vérifier si le référentiel des formats que l\'on souhaite importer est valide',
            overridePermission: 'check'
          },
          writePermission: null,
          deletePermission: null
        },
        {
          service: 'ingestcontracts',
          type: 'ingestcontracts',
          name: 'Contrats d\'Entrée',
          readPermission: {
            activated: defaultPermission,
            description: 'Lister le contenu du référentiel des contrats d\'entrée',
            overridePermission: null
          },
          writePermission: {
            activated: defaultPermission,
            description: 'Importer des contrats d\'entrées dans le référentiel',
            overridePermission: 'create:json'
          },
          deletePermission: null
        },
        {
          service: 'ingestcontracts:id',
          type: 'ingestcontracts',
          name: 'Contrats d\'Entrée (par ID)',
          readPermission: {
            activated: defaultPermission,
            description: 'Lire un contrat d\'entrée donné',
            overridePermission: null
          },
          writePermission: {
            activated: defaultPermission,
            description: 'Effectuer une mise à jour sur un contrat d\'entrée',
            overridePermission: null
          },
          deletePermission: null
        },
        {
          service: 'operations',
          type: 'operations',
          name: 'Opérations',
          readPermission: {
            activated: defaultPermission,
            description: 'Récupérer les informations sur une opération donnée',
            overridePermission: null
          },
          writePermission: null,
          deletePermission: null
        },
        {
          service: 'operations:id',
          type: 'operations',
          name: 'Opérations (Status)',
          readPermission: {
            activated: defaultPermission,
            description: 'Récupérer le code HTTP d\'une opération donnée',
            overridePermission: 'read:status'
          },
          writePermission: null,
          deletePermission: null
        },
        {
          service: 'operations:id',
          type: 'operations',
          name: 'Opérations (par ID)',
          readPermission: {
            activated: defaultPermission,
            description: 'Récupérer le journal d\'une opération donnée',
            overridePermission: null
          },
          writePermission: {
            activated: defaultPermission,
            description: 'Changer le statut d\'une opération donnée',
            overridePermission: null
          },
          deletePermission: {
            activated: defaultPermission,
            description: 'Annuler une opération donnée',
            overridePermission: null
          }
        },
        {
          service: 'profiles',
          type: 'profiles',
          name: 'Profiles',
          readPermission: {
            activated: defaultPermission,
            description: 'Lister le contenu du référentiel des profils',
            overridePermission: null
          },
          writePermission: {
            activated: defaultPermission,
            description: 'Importer des profils dans le référentiel',
            overridePermission: 'create:binary'
          },
          deletePermission: null
        },
        {
          service: 'profiles',
          type: 'profiles',
          name: 'Profiles (JSON)',
          readPermission: {
            activated: defaultPermission,
            description: 'Lire un profil donné',
            overridePermission: 'id:read:json'
          },
          writePermission: {
            activated: defaultPermission,
            description: 'Ecrire un profil dans le référentiel',
            overridePermission: 'create:json'
          },
          deletePermission: null
        },
        {
          service: 'profiles:id',
          type: 'profiles',
          name: 'Profiles (par ID, Binaire)',
          readPermission: {
            activated: defaultPermission,
            description: 'Télecharger le fichier xsd ou rng attaché à un profil',
            overridePermission: 'read:binary'
          },
          writePermission: {
            activated: defaultPermission,
            description: 'Importer un fichier xsd ou rng dans un profil',
            overridePermission: 'update:binaire'
          },
          deletePermission: null
        },
        {
          service: 'profiles:id',
          type: 'profiles',
          name: 'Profiles (par ID, JSON)',
          readPermission: null,
          writePermission: {
            activated: defaultPermission,
            description: 'Effectuer une mise à jour sur un profil',
            overridePermission: 'update:json'
          },
          deletePermission: null
        },
        {
          service: 'rules',
          type: 'rules',
          name: 'Règles de gestion',
          readPermission: {
            activated: defaultPermission,
            description: 'Lister le contenu du référentiel des règles de gestion',
            overridePermission: null
          },
          writePermission: {
            activated: defaultPermission,
            description: 'Importer un référentiel des règles de gestion',
            overridePermission: 'create'
          },
          deletePermission: null
        },
        {
          service: 'rules:id',
          type: 'rules',
          name: 'Règles de gestion',
          readPermission: {
            activated: defaultPermission,
            description: 'Lire une règle de gestion donnée',
            overridePermission: null
          },
          writePermission: null,
          deletePermission: null
        },
        {
          service: 'rulesfile',
          type: 'rules',
          name: 'Règles de gestion (Validité CSV)',
          readPermission: {
            activated: defaultPermission,
            description: 'Vérifier si le référentiel de règles de gestions que l\'on souhaite importer est valide',
            overridePermission: 'check'
          },
          writePermission: null,
          deletePermission: null
        },
        {
          service: 'rulesreferential:id',
          type: 'rules',
          name: 'Règles de gestion (Export)',
          readPermission: {
            activated: defaultPermission,
            description: 'Récupérer le référentiel pour une opération d\'import de règles de gestion',
            overridePermission: null
          },
          writePermission: null,
          deletePermission: null
        },
        {
          service: 'rulesreport:id',
          type: 'rules',
          name: 'Règles de gestion (Rapport)',
          readPermission: {
            activated: defaultPermission,
            description: 'Récupérer le rapport pour une opération d\'import de règles de gestion',
            overridePermission: null
          },
          writePermission: null,
          deletePermission: null
        },
        {
          service: 'securityprofiles',
          type: 'securityprofiles',
          name: 'Profiles de sécurité',
          readPermission: {
            activated: defaultPermission,
            description: 'Lister le contenu du référentiel des profiles de sécurité',
            overridePermission: null
          },
          writePermission: {
            activated: defaultPermission,
            description: 'Importer des profiles de sécurité dans le référentiel',
            overridePermission: 'create:json'
          },
          deletePermission: null
        },
        {
          service: 'securityprofiles:id',
          type: 'securityprofiles',
          name: 'Profiles de sécurité (par ID)',
          readPermission: {
            activated: defaultPermission,
            description: 'Lire un profile de sécurité donné',
            overridePermission: null
          },
          writePermission: {
            activated: defaultPermission,
            description: 'Effectuer une mise à jour sur un profil de sécurité',
            overridePermission: null
          },
          deletePermission: null
        },
        {
          service: 'traceabilitychecks',
          type: 'traceability',
          name: 'Sécurisations',
          readPermission: null,
          writePermission: {
            activated: defaultPermission,
            description: 'Tester l\'intégrité d\'un journal sécurisé',
            overridePermission: 'create'
          },
          deletePermission: null
        },
        {
          service: 'traceability:id',
          type: 'traceability',
          name: 'Sécurisations (par ID)',
          readPermission: {
            activated: defaultPermission,
            description: 'Télécharger le logbook sécurisé attaché à une opération de sécurisation',
            overridePermission: null
          },
          writePermission: null,
          deletePermission: null
        },
        {
          service: 'workflows',
          type: 'workflows',
          name: 'Workflows',
          readPermission: {
            activated: defaultPermission,
            description: 'Récupérer la liste des tâches des workflows',
            overridePermission: null
          },
          writePermission: null,
          deletePermission: null
        },
        {
          service: 'ingests',
          type: 'ingests',
          name: 'Entrées',
          readPermission: null,
          writePermission: {
            activated: defaultPermission,
            description: 'Envoyer un SIP à Vitam afin qu\'il en réalise l\'entrée',
            overridePermission: 'create'
          },
          deletePermission: null
        },
        {
          service: 'ingests:local',
          type: 'ingests',
          name: 'Entrées (Local)',
          readPermission: null,
          writePermission: {
            activated: defaultPermission,
            description: 'Envoyer un SIP en local à Vitam afin qu\'il en réalise l\'entrée',
            overridePermission: 'create'
          },
          deletePermission: null
        },
        {
          service: 'ingests:id:archivetransfertreply',
          type: 'ingests',
          name: 'Entrées (ATR)',
          readPermission: {
            activated: defaultPermission,
            description: 'Récupérer l\'accusé de récéption pour une opération d\'entrée donnée',
            overridePermission: null
          },
          writePermission: null,
          deletePermission: null
        },
        {
          service: 'ingests:id:manifests',
          type: 'ingests',
          name: 'Entrées (Manifest)',
          readPermission: {
            activated: defaultPermission,
            description: 'Récupérer le bordereau de versement pour une opération d\'entrée donnée',
            overridePermission: null
          },
          writePermission: null,
          deletePermission: null
        },
        {
          service: 'archiveunitprofiles',
          type: 'archiveunitprofiles',
          name: 'Profil d\'unité archivistique',
          readPermission: {
            activated: defaultPermission,
            description: 'Lister le contenu du référentiel des document types',
            overridePermission: null
          },
          writePermission: {
            activated: defaultPermission,
            description: 'Importer un ou plusieurs document types dans le référentiel',
            overridePermission: 'create:binary'
          },
          deletePermission: null
        },
        {
          service: 'archiveunitprofiles',
          type: 'archiveunitprofiles',
          name: 'Profil d\'unité archivistique (JSON)',
          readPermission: null,
          writePermission: {
            activated: defaultPermission,
            description: 'Ecrire un ou plusieurs document type dans le référentiel',
            overridePermission: 'create:json'
          },
          deletePermission: null
        },
        {
          service: 'archiveunitprofiles:id',
          type: 'archiveunitprofiles',
          name: 'Profil d\'unité archivistique (par ID)',
          readPermission: {
            activated: defaultPermission,
            description: 'Lire un document type donné',
            overridePermission: 'read:json'
          },
          writePermission: {
            activated: defaultPermission,
            description: 'Effectuer une mise à jour sur un document type',
            overridePermission: 'update:json'
          },
          deletePermission: null
        },
        {
          service: 'ontologies',
          type: 'ontologies',
          name: 'Ontologies',
          readPermission: {
            activated: defaultPermission,
            description: 'Lister le contenu du référentiel des ontologies',
            overridePermission: null
          },
          writePermission: {
            activated: defaultPermission,
            description: 'Importer les ontologies dans le référentiel',
            overridePermission: 'create:json'
          },
          deletePermission: null
        },
        {
          service: 'ontologies:id',
          type: 'ontologies',
          name: 'Ontologies (par ID)',
          readPermission: {
            activated: defaultPermission,
            description: 'Lire une ontologie',
            overridePermission: 'read:json'
          },
          writePermission: null,
          deletePermission: null
        },
        {
          service: 'reclassification',
          type: 'reclassification',
          name: 'Reclassification',
          readPermission: null,
          writePermission: {
            activated: defaultPermission,
            description: 'Mise à jour d\'arborescence des unités archivistiques',
            overridePermission: null
          },
          deletePermission: null
        },
        {
          service: 'rectificationaudit',
          type: 'rectificationaudit',
          name: 'Rectification après Audit',
          readPermission: null,
          writePermission: {
            activated: defaultPermission,
            description: 'Rectification de données suite a un audit',
            overridePermission: 'check'
          },
          deletePermission: null
        },
        {
          service: 'storageaccesslog',
          type: 'storageaccesslog',
          name: 'Journal d\'accès au stockage',
          readPermission: {
            activated: defaultPermission,
            description: 'Télécharger les journaux d\'accès',
            overridePermission: 'read:binary'
          },
          writePermission: null,
          deletePermission: null
        },
        {
          service: 'elimination',
          type: 'elimination',
          name: 'Elimination',
          readPermission: {
            activated: defaultPermission,
            description: 'Analyse de l\'élimination d\'unités archivistiques',
            overridePermission: 'analysis'
          },
          writePermission: {
            activated: defaultPermission,
            description: 'Elimination définitive d\'unités archivistiques',
            overridePermission: 'action'
          },
          deletePermission: null
        },
        {
          service: 'forcepause',
          type: 'forcepause',
          name: 'Mise en pause d\'opération',
          readPermission: null,
          writePermission: {
            activated: defaultPermission,
            description: 'Force la pause sur un type d\'operation et/ou sur un tenant',
            overridePermission: 'check'
          },
          deletePermission: {
            activated: defaultPermission,
            description: 'Retire la pause sur un type d\'operation et/ou sur un tenant',
            overrideApiPermission: 'removeforcepause:check'
          }
        },
        {
          service: 'probativevalue',
          type: 'probativevalue',
          name: 'Relevé de valeur probante',
          writePermission: {
            activated: defaultPermission,
            description: 'Lancer un export du relevé de valeur probante',
            overridePermission: 'create'
          }
        },
        {
          service: 'griffins',
          type: 'griffin',
          name: 'Griffons',
          readPermission: {
            activated: defaultPermission,
            description: 'Lister le contenu du référentiel des griffons',
            overridePermission: null
          },
          writePermission: {
            activated: defaultPermission,
            description: 'Import du griffon',
            overridePermission: 'create'
          },
          deletePermission: null
        },
        {
          service: 'griffin',
          type: 'griffin',
          name: 'Griffons (par ID)',
          readPermission: {
            activated: defaultPermission,
            description: 'Lecture d\'un griffin par identifier',
            overridePermission: null
          },
          writePermission: null,
          deletePermission: null
        },
        {
          service: 'preservationScenarios',
          type: 'preservationScenario',
          name: 'Scénario de préservation',
          readPermission: {
            activated: defaultPermission,
            description: 'Lister le contenu du référentiel des préservation scénarios',
            overridePermission: null
          },
          writePermission: {
            activated: defaultPermission,
            description: 'Import des perservation scénarios',
            overridePermission: 'create'
          },
          deletePermission: null
        },
        {
          service: 'preservationScenario',
          type: 'preservationScenario',
          name: 'Scénario de préservation (par ID)',
          readPermission: {
            activated: defaultPermission,
            description: 'Lecture d\'un scenario par identifier',
            overridePermission: null
          },
          writePermission: null,
          deletePermission: null
        },
        {
          service: 'preservation',
          type: 'preservation',
          name: 'Préservation',
          readPermission: null,
          writePermission: {
            activated: defaultPermission,
            description: 'Lancer le processus de préservation',
            overridePermission: null
          },
          deletePermission: null
        },
        {
          service: 'batchreport:id',
          type: 'batchreport',
          name: 'Traitement de masse (Rapport)',
          readPermission: {
            activated: defaultPermission,
            description: 'Récupérer le rapport pour un traitement de masse (Elimination, Preservation, Audit, Mise à jour)',
            overridePermission: null
          },
          writePermission: null,
          deletePermission: null
        },
        {
          service: 'computeInheritedRules',
          type: 'computeInheritedRules',
          name: 'Calcul des règles hérités',
          readPermission: null,
          writePermission: {
            activated: defaultPermission,
            description: 'Lancer le processus de calcul des règles hérité pour la recherche',
            overridePermission: 'action'
          },
          deletePermission: {
            activated: defaultPermission,
            description: 'Lancer le processus de suppression des règles hérité pour la recherche',
            overridePermission: 'delete'
          }
        }
      ]
    };
  }

  getPermissionsAsList(permissions: PermissionStructure): string[] {
    const permissionsAsList: string[] = [];
    permissions.apiPermissions.forEach(apiPermission => {
      if (apiPermission.readPermission && apiPermission.readPermission.activated) {
        permissionsAsList.push(this.getPermissionString(apiPermission.service, apiPermission.readPermission, 'read'));
      }
      if (apiPermission.writePermission && apiPermission.writePermission.activated) {
        permissionsAsList.push(this.getPermissionString(apiPermission.service, apiPermission.writePermission, 'update'));
      }
      if (apiPermission.deletePermission && apiPermission.deletePermission.activated) {
        permissionsAsList.push(this.getPermissionString(apiPermission.service, apiPermission.deletePermission, 'delete'));
      }
    });
    return permissionsAsList;
  }

  getFormConfigFromPermission(permissions: PermissionStructure): any {
    const formConfig: any = {};
    permissions.apiPermissions.forEach(apiPermission => {
      if (apiPermission.readPermission) {
        formConfig[this.getPermissionString(apiPermission.service, apiPermission.readPermission, 'read')] =
          apiPermission.readPermission.activated;
      }
      if (apiPermission.writePermission) {
        formConfig[this.getPermissionString(apiPermission.service, apiPermission.writePermission, 'update')] =
          apiPermission.writePermission.activated;
      }
      if (apiPermission.deletePermission) {
        formConfig[this.getPermissionString(apiPermission.service, apiPermission.deletePermission, 'delete')] =
          apiPermission.deletePermission.activated;
      }
    });
    return formConfig;
  }

  getPermissionsFromList(permissionsAsList: string[]): PermissionStructure {
    const permissions: PermissionStructure = this.getInitPermissions(false);
    permissions.apiPermissions.forEach(apiPermission => {
      if (apiPermission.readPermission
        && permissionsAsList.includes(this.getPermissionString(apiPermission.service, apiPermission.readPermission, 'read'))) {
        apiPermission.readPermission.activated = true;
      }
      if (apiPermission.writePermission
        && permissionsAsList.includes(this.getPermissionString(apiPermission.service, apiPermission.writePermission, 'update'))) {
        apiPermission.writePermission.activated = true;
      }
      if (apiPermission.deletePermission
        && permissionsAsList.includes(this.getPermissionString(apiPermission.service, apiPermission.deletePermission, 'delete'))) {
        apiPermission.deletePermission.activated = true;
      }
    });
    return permissions;
  }

  getPermissionString(service: string, permission: Permission, defaultPermission: string): string {
    if (permission.overrideApiPermission) {
      return permission.overrideApiPermission;
    }
    if (permission.overridePermission) {
      return service + ':' + permission.overridePermission;
    }
    return service + ':' + defaultPermission;
  }
}
