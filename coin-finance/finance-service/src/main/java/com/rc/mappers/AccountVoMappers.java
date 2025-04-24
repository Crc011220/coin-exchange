package com.rc.mappers;

import com.rc.domain.Account;
import com.rc.vo.AccountVo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AccountVoMappers {

    AccountVoMappers INSTANCE = Mappers.getMapper(AccountVoMappers.class);

    AccountVo toAccountVo(Account account);

    Account toAccount(AccountVo accountVo);

    List<AccountVo> toAccountVoList(List<Account> accountList);

    List<Account> toAccountList(List<AccountVo> accountVoList);
}
