<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:fo="http://www.w3.org/1999/XSL/Format"
	exclude-result-prefixes="fo">
	<xsl:output method="xml" version="1.0"
		omit-xml-declaration="no" indent="yes" />
	<xsl:param name="versionParam" select="'1.0'" />
	
	<!-- ========================= -->
	<!-- root element: report -->
	<!-- ========================= -->
	
	<xsl:template match="report">
		<fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
			<fo:layout-master-set>
				<fo:simple-page-master master-name="simpleA4"
					page-height="29.7cm" page-width="21cm" margin-top="3cm"
					margin-bottom="3cm" margin-left="2cm" margin-right="2cm"
					font-size="14pt">
					<fo:region-body margin-bottom="2cm" margin-top="2cm"/>
					<fo:region-before region-name="xsl-region-before" extent="2cm" />
					<fo:region-after region-name="xsl-region-after" extent="2cm" />
				</fo:simple-page-master>
			</fo:layout-master-set>
			<fo:page-sequence master-reference="simpleA4">
				<xsl:call-template name="globalHeader" />
				<xsl:call-template name="globalFooter" />
				<fo:flow flow-name="xsl-region-body">
					<xsl:apply-templates select="operationSummary" />
					<xsl:apply-templates select="reportSummary" />
				</fo:flow>
			</fo:page-sequence>
			<fo:page-sequence master-reference="simpleA4">
				<xsl:call-template name="globalHeader" />
				<xsl:call-template name="globalFooter" />
				<fo:flow flow-name="xsl-region-body">
					<xsl:apply-templates select="context" />
				</fo:flow>
			</fo:page-sequence>
			<xsl:apply-templates select="reportEntries/reportEntries" />
		</fo:root>
	</xsl:template>
	
	<xsl:template name="globalHeader">
		<fo:static-content flow-name="xsl-region-before">
			<fo:block font-size="20pt" font-weight="bold" text-align="center" border-width="1pt" border-style="solid" padding-top="10pt" padding-bottom="8pt">
				RELEVE DE VALEUR PROBANTE
			</fo:block>
		</fo:static-content>
	</xsl:template>
	
	<xsl:template name="globalFooter">
		<fo:static-content flow-name="xsl-region-after">
			<fo:block text-align="end">Page <fo:page-number/> / <fo:page-number-citation ref-id="TheVeryLastPage"/> </fo:block>
		</fo:static-content>
	</xsl:template>

	<xsl:template match="operationSummary">

		<fo:block font-size="16pt" font-weight="bold" padding-top="40pt">== RESUME DE L'OPERATION ==</fo:block>
		<fo:table border-width="1pt" border-style="none">
		
			<fo:table-column column-width="5.5cm" />
			<fo:table-column />

			<fo:table-body>
				<fo:table-row>
					<fo:table-cell padding-top="20pt">
						<fo:block>Tenant</fo:block>
					</fo:table-cell>
					<fo:table-cell padding-top="20pt">
						<fo:block>
							<xsl:value-of select="tenant" />
						</fo:block>
					</fo:table-cell>
				</fo:table-row>
				<fo:table-row>
					<fo:table-cell padding-top="5pt">
						<fo:block>Identifiant de l'opération</fo:block>
					</fo:table-cell>
					<fo:table-cell padding-top="5pt">
						<fo:block>
							<xsl:value-of select="evId" />
						</fo:block>
					</fo:table-cell>
				</fo:table-row>
				<fo:table-row>
					<fo:table-cell padding-top="5pt">
						<fo:block>Type d'opération</fo:block>
					</fo:table-cell>
					<fo:table-cell padding-top="5pt">
						<fo:block>
							<xsl:value-of select="evType" />
						</fo:block>
					</fo:table-cell>
				</fo:table-row>
				<fo:table-row>
					<fo:table-cell padding-top="5pt">
						<fo:block>Statut</fo:block>
					</fo:table-cell>
					<fo:table-cell padding-top="5pt">
						<fo:block>
							<xsl:value-of select="outcome" />
						</fo:block>
					</fo:table-cell>
				</fo:table-row>
				<fo:table-row>
					<fo:table-cell padding-top="5pt">
						<fo:block>Détail</fo:block>
					</fo:table-cell>
					<fo:table-cell padding-top="5pt">
						<fo:block>
							<xsl:value-of select="outDetail" />
						</fo:block>
					</fo:table-cell>
				</fo:table-row>
				<fo:table-row>
					<fo:table-cell padding-top="5pt">
						<fo:block>Message</fo:block>
					</fo:table-cell>
					<fo:table-cell padding-top="5pt">
						<fo:block>
							<xsl:value-of select="outMsg" />
						</fo:block>
					</fo:table-cell>
				</fo:table-row>
			</fo:table-body>

		</fo:table>
		
	</xsl:template>

	<xsl:template match="reportSummary">

		<fo:block font-size="16pt" font-weight="bold" padding-top="40pt">== RESUME DU RAPPORT ==</fo:block>
		<fo:table border-width="1pt" border-style="none">
		
			<fo:table-column column-width="5.5cm" />
			<fo:table-column />

			<fo:table-body>
				<fo:table-row>
					<fo:table-cell padding-top="20pt">
						<fo:block>Début</fo:block>
					</fo:table-cell>
					<fo:table-cell padding-top="20pt">
						<fo:block>
							<xsl:value-of select="evStartDateTime" />
						</fo:block>
					</fo:table-cell>
				</fo:table-row>
				<fo:table-row>
					<fo:table-cell padding-top="5pt">
						<fo:block>Fin</fo:block>
					</fo:table-cell>
					<fo:table-cell padding-top="5pt">
						<fo:block>
							<xsl:value-of select="evEndDateTime" />
						</fo:block>
					</fo:table-cell>
				</fo:table-row>
				<fo:table-row>
					<fo:table-cell padding-top="5pt">
						<fo:block>Type</fo:block>
					</fo:table-cell>
					<fo:table-cell padding-top="5pt">
						<fo:block>
							<xsl:value-of select="reportType" />
						</fo:block>
					</fo:table-cell>
				</fo:table-row>
				<xsl:apply-templates select="vitamResults" />
			</fo:table-body>

		</fo:table>

	</xsl:template>

	<xsl:template match="vitamResults">
		<fo:table-row>
			<fo:table-cell number-columns-spanned="2"
				padding-top="10pt">
				<fo:block>Résultats</fo:block>
			</fo:table-cell>
		</fo:table-row>
		<fo:table-row>
			<fo:table-cell padding-left="20pt" padding-top="10pt">
				<fo:block>Succès</fo:block>
			</fo:table-cell>
			<fo:table-cell padding-top="10pt">
				<fo:block>
					<xsl:value-of select="OK" />
				</fo:block>
			</fo:table-cell>
		</fo:table-row>
		<fo:table-row>
			<fo:table-cell padding-left="20pt" padding-top="5pt">
				<fo:block>Erreurs</fo:block>
			</fo:table-cell>
			<fo:table-cell padding-top="5pt">
				<fo:block>
					<xsl:value-of select="KO" />
				</fo:block>
			</fo:table-cell>
		</fo:table-row>
		<fo:table-row>
			<fo:table-cell padding-left="20pt" padding-top="5pt">
				<fo:block>Avertissements</fo:block>
			</fo:table-cell>
			<fo:table-cell padding-top="5pt">
				<fo:block>
					<xsl:value-of select="WARNING" />
				</fo:block>
			</fo:table-cell>
		</fo:table-row>
		<fo:table-row>
			<fo:table-cell padding-left="20pt" font-weight="bold" padding-top="5pt">
				<fo:block>Total</fo:block>
			</fo:table-cell>
			<fo:table-cell padding-top="5pt">
				<fo:block>
					<xsl:value-of select="total" />
				</fo:block>
			</fo:table-cell>
		</fo:table-row>

	</xsl:template>

	<xsl:template match="context">

		<fo:block font-size="16pt" font-weight="bold" padding-top="40pt">== REQUETE EFFECTUEE ==</fo:block>
		<fo:table border-width="1pt" border-style="none">
			<fo:table-column column-width="5.5cm" />
			<fo:table-column />

			<fo:table-body>
				<fo:table-row>
					<fo:table-cell padding-top="20pt">
						<fo:block>Requête</fo:block>
					</fo:table-cell>
					<fo:table-cell padding-top="20pt">
						<fo:block wrap-option="wrap">
							<xsl:value-of select="dslQuery" />
						</fo:block>
					</fo:table-cell>
				</fo:table-row>
				<fo:table-row>
					<fo:table-cell padding-top="10pt">
						<fo:block>Usage</fo:block>
					</fo:table-cell>
					<fo:table-cell padding-top="10pt">
						<fo:block>
							<xsl:value-of select="usage" />
						</fo:block>
					</fo:table-cell>
				</fo:table-row>
				<fo:table-row>
					<fo:table-cell padding-top="5pt">
						<fo:block>Version</fo:block>
					</fo:table-cell>
					<fo:table-cell padding-top="5pt">
						<fo:block>
							<xsl:value-of select="version" />
						</fo:block>
					</fo:table-cell>
				</fo:table-row>
			</fo:table-body>

		</fo:table>

	</xsl:template>

	<xsl:template match="reportEntries/reportEntries">

		<fo:page-sequence master-reference="simpleA4">
			<xsl:call-template name="globalHeader" />
			<xsl:call-template name="globalFooter" />
			
			<fo:flow flow-name="xsl-region-body">
				<fo:block font-size="16pt" font-weight="bold" text-align="center" border-width="1pt" border-style="none" padding-top="10pt" padding-bottom="8pt" text-decoration="underline">
					DETAIL DU RAPPORT NUM <xsl:value-of select="position()"/>
				</fo:block>
				<xsl:call-template name="reportEntries_reportEntries_resume"/>
				<xsl:call-template name="reportEntries_reportEntries_operations"/>
				<xsl:call-template name="reportEntries_reportEntries_checks"/>
				 <xsl:if test="position() = last()">
					<fo:block id="TheVeryLastPage"> </fo:block>
				</xsl:if>
			</fo:flow>
			
		</fo:page-sequence>
		
	</xsl:template>

	<xsl:template name="reportEntries_reportEntries_resume">

		<fo:block font-size="16pt" font-weight="bold" padding-top="40pt">== RESUME ==</fo:block>
		
		<fo:block padding-top="10pt">Unités archivistiques analysées</fo:block>
		<fo:list-block>
			<xsl:for-each select="unitIdWithLabels/unitIdWithLabels">
				<fo:list-item>
					<fo:list-item-label start-indent="0.5cm">
						<fo:block font-weight="bold"> • </fo:block>
					</fo:list-item-label>
					<fo:list-item-body start-indent="1.0cm">
						<fo:block space-after.optimum="14pt">
							<xsl:value-of select="item" /> - <xsl:value-of select="label" />
						</fo:block>
					</fo:list-item-body>
				</fo:list-item>
			</xsl:for-each>
		</fo:list-block>
		
		<fo:block padding-top="10pt">Groupe d'objet analysé</fo:block>
		<fo:list-block>
			<fo:list-item>
				<fo:list-item-label start-indent="0.5cm">
					<fo:block font-weight="bold"> • </fo:block>
				</fo:list-item-label>
				<fo:list-item-body start-indent="1.0cm">
					<fo:block space-after.optimum="14pt">
						<xsl:value-of select="objectGroupId" />
					</fo:block>
				</fo:list-item-body>
			</fo:list-item>
		</fo:list-block>
		
		<fo:block padding-top="10pt">Objet analysé</fo:block>
		<fo:list-block>
			<fo:list-item>
				<fo:list-item-label start-indent="0.5cm">
					<fo:block font-weight="bold"> • </fo:block>
				</fo:list-item-label>
				<fo:list-item-body start-indent="1.0cm">
					<fo:block space-after.optimum="14pt">
						<xsl:value-of select="objectId" /> - <xsl:value-of select="objectLabel" />
					</fo:block>
				</fo:list-item-body>
			</fo:list-item>
		</fo:list-block>
		
		<fo:table border-width="1pt" border-style="none">
			<fo:table-column column-width="5.5cm" />
			<fo:table-column />

			<fo:table-body>
				<fo:table-row>
					<fo:table-cell padding-top="20pt">
						<fo:block>Usage</fo:block>
					</fo:table-cell>
					<fo:table-cell padding-top="20pt">
						<fo:block>
							<xsl:value-of select="substring-before(usageVersion, '_')" />
						</fo:block>
					</fo:table-cell>
				</fo:table-row>
				<fo:table-row>
					<fo:table-cell padding-top="5pt">
						<fo:block>Version</fo:block>
					</fo:table-cell>
					<fo:table-cell padding-top="5pt">
						<fo:block>
							<xsl:value-of select="substring-after(usageVersion, '_')" />
						</fo:block>
					</fo:table-cell>
				</fo:table-row>
				<fo:table-row>
					<fo:table-cell padding-top="5pt">
						<fo:block>Date de début d'analyse</fo:block>
					</fo:table-cell>
					<fo:table-cell padding-top="5pt">
						<fo:block>
							<xsl:value-of select="evStartDateTime" />
						</fo:block>
					</fo:table-cell>
				</fo:table-row>
				<fo:table-row>
					<fo:table-cell padding-top="5pt">
						<fo:block>Date de fin d'analyse</fo:block>
					</fo:table-cell>
					<fo:table-cell padding-top="5pt">
						<fo:block>
							<xsl:value-of select="evEndDateTime" />
						</fo:block>
					</fo:table-cell>
				</fo:table-row>
				<fo:table-row>
					<fo:table-cell padding-top="5pt">
						<fo:block>Status</fo:block>
					</fo:table-cell>
					<fo:table-cell padding-top="5pt">
						<fo:block>
							<xsl:value-of select="status" />
						</fo:block>
					</fo:table-cell>
				</fo:table-row>
				<fo:table-row>
					<fo:table-cell padding-top="5pt">
						<fo:block>Version du rapport</fo:block>
					</fo:table-cell>
					<fo:table-cell padding-top="5pt">
						<fo:block>
					<xsl:value-of select="../../ReportVersion"/>
						</fo:block>
					</fo:table-cell>
				</fo:table-row>
			</fo:table-body>
		</fo:table>

	</xsl:template>

	<xsl:template name="reportEntries_reportEntries_operations">

		<fo:block font-size="16pt" font-weight="bold" padding-top="40pt" padding-bottom="20pt">== OPERATIONS ==</fo:block>
	
		<fo:table border-width="1pt" border-style="solid" font-size="8pt">
			<fo:table-column column-width="6cm" border-width="1pt" border-style="solid"/>
			<fo:table-column column-width="4cm" border-width="1pt" border-style="solid"/>
			<fo:table-column border-width="1pt" border-style="solid"/>
		
			<fo:table-header>
				<fo:table-cell>
					<fo:block font-weight="bold" padding-top="5pt" padding-bottom="5pt" text-align="center">Id</fo:block>
				</fo:table-cell>
				<fo:table-cell>
					<fo:block font-weight="bold" padding-top="5pt" padding-bottom="5pt" text-align="center">Date</fo:block>
				</fo:table-cell>
				<fo:table-cell>
					<fo:block font-weight="bold" padding-top="5pt" padding-bottom="5pt" text-align="center">Détail</fo:block>
				</fo:table-cell>
			</fo:table-header>
		
			<fo:table-body>
				<xsl:apply-templates select="operations/operations" />
			</fo:table-body>
		</fo:table>
	
	
	</xsl:template>
	
	<xsl:template match="operations/operations">
		<fo:table-row border-width="1pt" border-style="solid" margin="2pt">
			<fo:table-cell padding-top="5pt">
				<fo:block>
					<xsl:value-of select="id" />
				</fo:block>
			</fo:table-cell>
			<fo:table-cell padding-top="5pt">
				<fo:block>
					<xsl:value-of select="evDateTime" />
				</fo:block>
			</fo:table-cell>
			<fo:table-cell padding-top="5pt">
				<fo:block>
					Type : <xsl:value-of select="evTypeProc" />
				</fo:block>
				<xsl:if test="agIdApp != ''">
					<fo:block>
						Contexte applicatif : <xsl:value-of select="agIdApp" />
					</fo:block>
				</xsl:if>
				<xsl:template match="rightsStatementIdentifier"/>
			</fo:table-cell>
		</fo:table-row>
	</xsl:template>
	
	<xsl:template match="rightsStatementIdentifier">
		<xsl:if test="ArchivalAgreement != ''">
			<fo:block>
				ArchivalAgreement : <xsl:value-of select="ArchivalAgreement" />
			</fo:block>
		</xsl:if>
		<xsl:if test="Profil != ''">
			<fo:block>
				Profil : <xsl:value-of select="Profil" />
			</fo:block>
		</xsl:if>
		<xsl:if test="AccessContract != ''">
			<fo:block>
				Contrat d'accès : <xsl:value-of select="AccessContract" />
			</fo:block>
		</xsl:if>
	</xsl:template>

	<xsl:template name="reportEntries_reportEntries_checks">

		<fo:block font-size="16pt" font-weight="bold" padding-top="40pt">== VERIFICATIONS ==</fo:block>
		<xsl:apply-templates select="checks/checks" />
	
	</xsl:template>

	
	<xsl:template match="checks/checks">
		<fo:block font-size="12pt" font-weight="bold" padding-top="20pt">
			<xsl:if test="nameLabel != ''"><xsl:value-of select="nameLabel" /> - </xsl:if><xsl:value-of select="name" />
		</fo:block>
		<fo:table border-width="1pt" border-style="none">
			<fo:table-column column-width="4cm" />
			<fo:table-column />

			<fo:table-body>
				<fo:table-row>
					<fo:table-cell padding-top="10pt">
						<fo:block>Libellé explicite</fo:block>
					</fo:table-cell>
					<fo:table-cell padding-top="10pt">
						<fo:block>
							<xsl:value-of select="details" />
						</fo:block>
					</fo:table-cell>
				</fo:table-row>
				<fo:table-row>
					<fo:table-cell padding-top="5pt">
						<fo:block>Type</fo:block>
					</fo:table-cell>
					<fo:table-cell padding-top="5pt">
						<fo:block>
							<xsl:value-of select="type" />
						</fo:block>
					</fo:table-cell>
				</fo:table-row>
				<fo:table-row>
					<fo:table-cell padding-top="5pt">
						<fo:block>Source</fo:block>
					</fo:table-cell>
					<fo:table-cell padding-top="5pt">
						<fo:block>
							<xsl:value-of select="source" />
						</fo:block>
					</fo:table-cell>
				</fo:table-row>
				<fo:table-row>
					<fo:table-cell padding-top="5pt">
						<fo:block>Destination</fo:block>
					</fo:table-cell>
					<fo:table-cell padding-top="5pt">
						<fo:block>
							<xsl:value-of select="destination" />
						</fo:block>
					</fo:table-cell>
				</fo:table-row>
				<fo:table-row>
					<fo:table-cell padding-top="5pt">
						<fo:block>Action</fo:block>
					</fo:table-cell>
					<fo:table-cell padding-top="5pt">
						<fo:block>
							<xsl:value-of select="action" />
						</fo:block>
					</fo:table-cell>
				</fo:table-row>
				<fo:table-row>
					<fo:table-cell padding-top="5pt">
						<fo:block>Item</fo:block>
					</fo:table-cell>
					<fo:table-cell padding-top="5pt">
						<fo:block>
							<xsl:value-of select="item" />
						</fo:block>
					</fo:table-cell>
				</fo:table-row>
				<fo:table-row>
					<fo:table-cell padding-top="5pt">
						<fo:block>Status</fo:block>
					</fo:table-cell>
					<fo:table-cell padding-top="5pt">
						<fo:block>
							<xsl:value-of select="status" />
						</fo:block>
					</fo:table-cell>
				</fo:table-row>
				<fo:table-row>
					<fo:table-cell padding-top="5pt" number-columns-spanned="2">
						<fo:block>Source comparable</fo:block>
					</fo:table-cell>
				</fo:table-row>
				<fo:table-row>
					<fo:table-cell padding-left="20pt" padding-top="5pt" number-columns-spanned="2">
						<fo:block>
							<xsl:value-of select="sourceComparable" />
						</fo:block>
					</fo:table-cell>
				</fo:table-row>
				<fo:table-row>
					<fo:table-cell padding-top="5pt" number-columns-spanned="2">
						<fo:block>Destination comparable</fo:block>
					</fo:table-cell>
				</fo:table-row>
				<fo:table-row>
					<fo:table-cell padding-left="20pt" padding-top="5pt" number-columns-spanned="2">
						<fo:block >
							<xsl:value-of select="destinationComparable" />
						</fo:block>
					</fo:table-cell>
				</fo:table-row>
			</fo:table-body>
		</fo:table>
		
	</xsl:template>

</xsl:stylesheet>
