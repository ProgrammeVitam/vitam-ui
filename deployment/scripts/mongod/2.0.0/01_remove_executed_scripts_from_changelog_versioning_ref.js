db = db.getSiblingDB("versioning");

print("Start 01_remove_executed_scripts_from_changelog_versioning_ref.js");

/**
The content of these scripts has been commented out due to regression. They are marked as executed despite this.
This script removes them from the changelog collection so that they are re-run again with their contents
 */

const scriptsChecksums = [
  "446adbc8507a0f84c9ef62e4cd5803012b37597e", // 41_add_search_with_rules_role_to_old_system_archive_profile.js.j2
  "50d0d8692ffa60fcecdf9789d2d3d79b38641967", // 47_create_new_profiles_during_upgrade.js.j2
  "543a53a3b22b1264b16d008ad8c7a75920dfe930", // 48_migrate_archivesearch_profiles.js.j2
  "e8c47240d2835f8f94fd1f290eb303f45ba21304", // 49_add_missing_role_in_contexts.js.j2
  "364d19ee42013299bb0ebe7954525772d4b9e1f1", // 49_switch_admin_group_to_archivesearch-admin_profile.js.j2
  "14bdd4d369965f49804da72aaee62dcd450e3f15", // 50_add_get_access_contract_role_to_archive_profiles.js.j2
  "e1043e00cdad0d27a9a029b09e1fd4c32793c016", // 50_update_archive_search_context_to_add_reclassification_role.js.j2
  "e1013228d4e40a14e79fc94ae319dd2e532b54e1", // 51_migration_roles_reclassification_updaterules_inheritesrules_script.js.j2
  "1b4e3641b7837de45028eea056b1091801e8997a", // 52_update_archive_search_context_to_add_unit_descriptive_metadata_update_role.js.j2
  "7e949b694309670f1ba9d83bad2613cd113e8c1d", // 53_migration_role_update_desc_unit_metadata_script.js.j2
  "5e1e736286d7d44e000918cfda1aa7490be06bb7", // 54_update_archive_search_application_informations.js.j2
  "d7b4357b253f8489a66665020765bd14b34d77db", // 55_update_archive_search_proles_description.js.j2
];

db.changelog.deleteMany({ checksum: { $in: scriptsChecksums } });

print("End 01_remove_executed_scripts_from_changelog_versioning_ref.js");
