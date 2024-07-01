package fr.gouv.vitamui.referential.internal.server.archivalprofileunit;

import fr.gouv.vitam.common.model.administration.ArchiveUnitProfileModel;
import fr.gouv.vitamui.commons.utils.VitamUIUtils;
import fr.gouv.vitamui.referential.common.dto.ArchivalProfileUnitDto;

import java.util.List;
import java.util.stream.Collectors;

public class ArchivalProfileUnitConverter {

    public ArchiveUnitProfileModel convertDtoToVitam(final ArchivalProfileUnitDto dto) {
        return VitamUIUtils.copyProperties(dto, new ArchiveUnitProfileModel());
    }

    public ArchivalProfileUnitDto convertVitamToDto(final ArchiveUnitProfileModel archivalUnitProfile) {
        ArchivalProfileUnitDto archivalProfileUnitDto = VitamUIUtils.copyProperties(
            archivalUnitProfile,
            new ArchivalProfileUnitDto()
        );
        archivalProfileUnitDto.setActivationDate(archivalUnitProfile.getActivationdate());
        archivalProfileUnitDto.setCreationDate(archivalUnitProfile.getCreationdate());
        archivalProfileUnitDto.setDeactivationDate(archivalUnitProfile.getDeactivationdate());
        archivalProfileUnitDto.setLastUpdate(archivalUnitProfile.getLastupdate());
        return archivalProfileUnitDto;
    }

    public List<ArchiveUnitProfileModel> convertDtosToVitams(final List<ArchivalProfileUnitDto> dtos) {
        return dtos.stream().map(this::convertDtoToVitam).collect(Collectors.toList());
    }

    public List<ArchivalProfileUnitDto> convertVitamsToDtos(
        final List<ArchiveUnitProfileModel> archiveUnitProfileModels
    ) {
        return archiveUnitProfileModels.stream().map(this::convertVitamToDto).collect(Collectors.toList());
    }
}
