<html xmlns="http://www.w3.org/1999/xhtml" lang="fr">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <title>Relevé de valeur probante</title>
    <style type="text/css">
        @page {
            size: A4;
            margin: 16mm 12mm;
        }

        body {
            color: #222222;
            font-family: sans-serif;
            font-size: 9pt;
            word-wrap: break-word;
        }

        h1 {
            font-size: 17pt;
            margin-bottom: 18pt;
        }

        h2 {
            border-bottom: 1px solid #666666;
            font-size: 12pt;
            margin-top: 18pt;
            page-break-after: avoid;
        }

        h3 {
            font-size: 10pt;
            margin-bottom: 4pt;
            page-break-after: avoid;
        }

        h4 {
            font-size: 9pt;
            margin-bottom: 2pt;
            page-break-after: avoid;
        }

        table {
            border-collapse: collapse;
            margin-bottom: 10pt;
            table-layout: fixed;
            width: 100%;
        }

        th,
        td {
            border: 1px solid #999999;
            padding: 4pt;
            text-align: left;
            vertical-align: top;
        }

        th {
            background-color: #eeeeee;
            width: 28%;
        }

        tr {
            page-break-inside: avoid;
        }

        ul {
            margin: 0;
            padding-left: 14pt;
        }

        pre {
            white-space: pre-wrap;
            word-wrap: break-word;
        }
    </style>
</head>
<body>
<h1>Relevé de valeur probante</h1>

<h2>Résumé de l'opération</h2>
<table>
    <tbody>
    <tr><th>Tenant</th><td>${report.operationSummary.tenant}</td></tr>
    <tr><th>Identifiant de l'opération</th><td>${report.operationSummary.evId}</td></tr>
    <tr><th>Type d'opération</th><td>${report.operationSummary.evType}</td></tr>
    <tr><th>Statut</th><td>${report.operationSummary.outcome}</td></tr>
    <tr><th>Détail</th><td>${report.operationSummary.outDetail}</td></tr>
    <tr><th>Message</th><td>${report.operationSummary.outMsg}</td></tr>
    </tbody>
</table>

<h2>Résumé du rapport</h2>
<table>
    <tbody>
    <tr><th>Début</th><td>${report.reportSummary.evStartDateTime}</td></tr>
    <tr><th>Fin</th><td>${report.reportSummary.evEndDateTime}</td></tr>
    <tr><th>Type</th><td>${report.reportSummary.reportType}</td></tr>
    <tr><th>Succès</th><td>${report.reportSummary.vitamResults.nbOk}</td></tr>
    <tr><th>Erreurs</th><td>${report.reportSummary.vitamResults.nbKo}</td></tr>
    <tr><th>Avertissements</th><td>${report.reportSummary.vitamResults.nbWarning}</td></tr>
    <tr><th>Total</th><td>${report.reportSummary.vitamResults.total}</td></tr>
    </tbody>
</table>

<h2>Requête effectuée</h2>
<table>
    <tbody>
    <tr><th>Requête</th><td><pre>${report.context.dslQuery}</pre></td></tr>
    <tr><th>Usage</th><td>${report.context.usage}</td></tr>
    <tr><th>Version</th><td>${report.context.version}</td></tr>
    </tbody>
</table>

[#list report.reportEntries as reportEntry]
<h2>Détail du rapport numéro ${reportEntry?counter}</h2>
<h3>Résumé</h3>
<table>
    <tbody>
    <tr>
        <th>Unités archivistiques analysées</th>
        <td>
            <ul>
                [#list reportEntry.unitIdWithLabels as unit]
                <li>${unit.item} - ${unit.label!}</li>
                [/#list]
            </ul>
        </td>
    </tr>
    <tr><th>Groupe d'objet analysé</th><td>${reportEntry.objectGroupId}</td></tr>
    <tr><th>Objet analysé</th><td>${reportEntry.objectId} - ${reportEntry.objectLabel!}</td></tr>
    <tr><th>Usage</th><td>${reportEntry.usageVersion?keep_before_last("_")}</td></tr>
    <tr><th>Version</th><td>${reportEntry.usageVersion?keep_after_last("_")}</td></tr>
    <tr><th>Date de début d'analyse</th><td>${reportEntry.evStartDateTime}</td></tr>
    <tr><th>Date de fin d'analyse</th><td>${reportEntry.evEndDateTime}</td></tr>
    <tr><th>Statut</th><td>${reportEntry.status}</td></tr>
    <tr><th>Version du rapport</th><td>${report.reportVersion}</td></tr>
    </tbody>
</table>

<h3>Opérations</h3>
[#list reportEntry.operations as operation]
<h4>Opération ${operation?counter} — ${operation.id}</h4>
<table>
    <tbody>
    <tr><th>Date</th><td>${operation.evDateTime}</td></tr>
    <tr><th>Type</th><td>${operation.evType}</td></tr>
    <tr><th>Contexte applicatif</th><td>${operation.agIdApp!}</td></tr>
    <tr><th>Contrat d'entrée</th><td>${operation.archivalAgreement!}</td></tr>
    <tr><th>Profil</th><td>${operation.profil!}</td></tr>
    <tr><th>Contrat d'accès</th><td>${operation.accessContract!}</td></tr>
    </tbody>
</table>
[/#list]

<h3>Vérifications</h3>
[#list reportEntry.checks as check]
<h4>Vérification ${check?counter} — ${check.name}</h4>
<table>
    <tbody>
    <tr><th>Détails</th><td>${check.details}</td></tr>
    <tr><th>Type</th><td>${check.type}</td></tr>
    <tr><th>Source</th><td>${check.source}</td></tr>
    <tr><th>Destination</th><td>${check.destination}</td></tr>
    <tr><th>Action</th><td>${check.action}</td></tr>
    <tr><th>Élément</th><td>${check.item}</td></tr>
    <tr><th>Statut</th><td>${check.status}</td></tr>
    <tr><th>Source comparable</th><td><pre>${check.sourceComparable}</pre></td></tr>
    <tr><th>Destination comparable</th><td><pre>${check.destinationComparable}</pre></td></tr>
    </tbody>
</table>
[/#list]
[/#list]
</body>
</html>
