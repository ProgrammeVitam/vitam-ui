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
package fr.gouv.vitamui.commons.vitam.api.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.gouv.vitamui.commons.vitam.api.dto.CollectionsDto;
import fr.gouv.vitamui.commons.vitam.api.dto.VitamUISearchResponseDto;
import fr.gouv.vitamui.commons.vitam.api.dto.FacetBucketDto;
import fr.gouv.vitamui.commons.vitam.api.dto.FacetResultsDto;
import fr.gouv.vitamui.commons.vitam.api.model.UnitTypeEnum;

public class VitamResponseHandler {

    private VitamResponseHandler() {
    }

    public static VitamUISearchResponseDto extractSearchResponse(JsonNode jsonResponse)
            throws JsonParseException, JsonMappingException, IOException {
        return extractResponse(jsonResponse, VitamUISearchResponseDto.class);
    }

    public static CollectionsDto extractCollections(JsonNode jsonResponse) throws Exception {
        VitamUISearchResponseDto searchResponse = extractResponse(jsonResponse, VitamUISearchResponseDto.class);
        List<String> ids = searchResponse.getResults().stream()
                .filter(res -> UnitTypeEnum.HOLDING_UNIT.getValue().equals(res.getUnitType())).map(res -> res.getId())
                .collect(Collectors.toList());
        return new CollectionsDto(ids, new ArrayList<>());
    }

    public static <T> T extractResponse(JsonNode jsonResponse, Class<T> clazz)
            throws JsonParseException, JsonMappingException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return objectMapper.treeToValue(jsonResponse, clazz);
    }

    public static FacetResultsDto extractAutoCompletionResponse(VitamUISearchResponseDto vitamuiSearchResponseDto,
            String word) {
        FacetResultsDto facetRes = new FacetResultsDto();
        List<FacetResultsDto> facetResults = vitamuiSearchResponseDto.getFacetResults();
        String[] parts = StringUtils.split(word);
        if (!facetResults.isEmpty() && parts.length > 0) {
            List<FacetBucketDto> buckets = facetResults.get(0).getBuckets();
            List<FacetBucketDto> bucks = buckets.stream()
                    .filter(b -> StringUtils.containsIgnoreCase(b.getValue(), parts[parts.length - 1]))
                    .sorted((b1, b2) -> b2.getCount().compareTo(b1.getCount())).collect(Collectors.toList());
            Long total = bucks.stream().map(b -> b.getCount()).reduce(0L, (a, b) -> a + b);
            bucks = bucks.stream().limit(5).collect(Collectors.toList());

            for (FacetBucketDto facetBucket : bucks) {
                String titre = vitamuiSearchResponseDto.getResults().stream().map(res -> res.getTitle())
                        .filter(title -> isTitlePartStartWithValue(title, facetBucket.getValue())).findFirst()
                        .orElse(null);
                if (titre != null) {
                    if (parts.length > 1) {
                        parts[parts.length - 1] = getCompleteWord(titre, facetBucket.getValue());
                        facetBucket.setValue(String.join(" ", parts));
                    } else {
                        facetBucket.setValue(getCompleteWord(titre, facetBucket.getValue()));
                    }
                }
            }
            bucks.add(new FacetBucketDto("all", total));
            facetRes.setBuckets(bucks);
            facetRes.setName(facetResults.get(0).getName());
        }
        return facetRes;
    }

    private static boolean isTitlePartStartWithValue(String title, String bucketValue) {
        if (StringUtils.containsIgnoreCase(title, bucketValue)) {
            String[] elements = StringUtils.split(title);
            return Arrays.stream(elements).anyMatch(e -> StringUtils.startsWithIgnoreCase(e, bucketValue));
        }
        return false;
    }

    private static String getCompleteWord(String title, String bucketValue) {
        String[] elements = StringUtils.split(title);
        return Arrays.stream(elements).filter(e -> StringUtils.startsWithIgnoreCase(e, bucketValue)).findFirst()
                .orElse(null);
    }
}
