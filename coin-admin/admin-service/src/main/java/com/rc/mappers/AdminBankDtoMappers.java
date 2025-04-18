package com.rc.mappers;

import com.rc.domain.AdminBank;
import com.rc.dto.AdminBankDto;
import org.checkerframework.checker.units.qual.A;
import org.checkerframework.checker.units.qual.min;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AdminBankDtoMappers {

    AdminBankDtoMappers INSTANCE = Mappers.getMapper(AdminBankDtoMappers.class);

    AdminBank toConvertEntity(AdminBankDto adminBankDto);

    AdminBankDto toConvertDto(AdminBank adminBank);

    List<AdminBank> toConvertEntity(List<AdminBankDto> adminBankDto);

    List<AdminBankDto> toConvertDto(List<AdminBank> adminBank);

}
