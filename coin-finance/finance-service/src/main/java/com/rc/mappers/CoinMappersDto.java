package com.rc.mappers;

import com.rc.domain.Coin;
import com.rc.dto.CoinDto;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CoinMappersDto {
    CoinMappersDto INSTANCE = Mappers.getMapper(CoinMappersDto.class);

    Coin toConvertEntity(CoinDto coin);

    List<Coin> toConvertEntity(List<CoinDto> coin);

    CoinDto toConvertDto(Coin coin);

    List<CoinDto> toConvertDto(List<Coin> coin);

}
