/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package fr.gouv.vitamui.referential.internal.server.config;

import fr.gouv.vitamui.referential.internal.server.accesscontract.AccessContractConverter;
import fr.gouv.vitamui.referential.internal.server.agency.AgencyConverter;
import fr.gouv.vitamui.referential.internal.server.context.ContextConverter;
import fr.gouv.vitamui.referential.internal.server.fileformat.FileFormatConverter;
import fr.gouv.vitamui.referential.internal.server.ingestcontract.IngestContractConverter;
import fr.gouv.vitamui.referential.internal.server.ontology.OntologyConverter;
import fr.gouv.vitamui.referential.internal.server.managementcontract.ManagementContractConverter;
import fr.gouv.vitamui.referential.internal.server.profile.ProfileConverter;
import fr.gouv.vitamui.referential.internal.server.rule.RuleConverter;
import fr.gouv.vitamui.referential.internal.server.securityprofile.SecurityProfileConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConverterConfig {

    @Bean
    public AccessContractConverter accessContractConverter() {
        return new AccessContractConverter();
    }

    @Bean
    public IngestContractConverter ingestContractConverter() {
        return new IngestContractConverter();
    }

    @Bean
    public AgencyConverter agencyConverter() {
        return new AgencyConverter();
    }

    @Bean
    public FileFormatConverter fileFormatConverter() {
        return new FileFormatConverter();
    }

    @Bean
    public ContextConverter contextConverter() {
        return new ContextConverter();
    }

    @Bean
    public SecurityProfileConverter securityProfileConverter() {
        return new SecurityProfileConverter();
    }

    @Bean
    public OntologyConverter ontologyConverter() {
        return new OntologyConverter();
    }

    @Bean
    public ManagementContractConverter managementContractConverter() {
        return new ManagementContractConverter();
    }

    @Bean
    public ProfileConverter profileConverter() {
        return new ProfileConverter();
    }

    @Bean
    public RuleConverter ruleConverter() {
        return new RuleConverter();
    }

}
