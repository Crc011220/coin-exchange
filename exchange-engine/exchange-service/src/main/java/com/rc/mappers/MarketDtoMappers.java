package com.rc.mappers;

import com.rc.domain.Market;
import com.rc.dto.MarketDto;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MarketDtoMappers {
    MarketDtoMappers INSTANCE = Mappers.getMapper(MarketDtoMappers.class);
    MarketDto toMarketDto(Market market);
    Market toMarket(MarketDto marketDto);
    List<MarketDto> toMarketDtoList(List<Market> marketList);
    List<Market> toMarketList(List<MarketDto> marketDtoList);
}
