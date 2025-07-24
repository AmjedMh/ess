package com.teknokote.ess.core.dao;

import com.teknokote.core.dao.JpaActivatableGenericDao;
import com.teknokote.ess.core.dao.mappers.CustomerAccountMapper;
import com.teknokote.ess.core.dao.mappers.StationMapper;
import com.teknokote.ess.core.dao.mappers.UserMapper;
import com.teknokote.ess.core.model.organization.CustomerAccount;
import com.teknokote.ess.core.model.organization.User;
import com.teknokote.ess.core.repository.CustomerAccountRepository;
import com.teknokote.ess.dto.CustomerAccountDto;
import com.teknokote.ess.dto.UserDto;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@Getter
public class CustomerAccountDao extends JpaActivatableGenericDao<Long, User, CustomerAccountDto, CustomerAccount> {
    @Autowired
    private CustomerAccountMapper mapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private StationMapper stationMapper;
    @Autowired
    private CustomerAccountRepository repository;

    public List<CustomerAccountDto> findAllByExportedStatusAndReachedExportDate(boolean exported, boolean cardManager) {
        return getRepository().findAllyByExportedStatusAndReachedExportDate(exported, cardManager).stream().map(getMapper()::toDto).toList();
    }

    public Page<CustomerAccountDto> findAllCustomerAcount(Pageable pageable) {
        return getRepository().findAllOrderedByCreationDateDesc(pageable).map(getMapper()::toDto);
    }

    public Page<CustomerAccountDto> findCustomerAccountByName(String name, Pageable pageable) {
        return getRepository().findCustomerAccountByName(name, pageable).map(getMapper()::toDto);
    }

    public Page<CustomerAccountDto> findByCustomerAccountByParent(String parent, Pageable pageable) {
        return getRepository().findCustomerAccountByParent(parent, pageable).map(getMapper()::toDto);
    }

    @Transactional
    public List<CustomerAccountDto> findCustomerAccountByParent(Long parentId) {
        return getRepository().findByParent(parentId).stream().map(getMapper()::toDto).collect(Collectors.toList());
    }

    public Page<CustomerAccountDto> findByCustomerAccountByCreator(String userCreator, Pageable pageable) {
        return getRepository().findCustomerAccountByCreator(userCreator, pageable).map(getMapper()::toDto);
    }

    public CustomerAccountDto findByStationControllerPtsId(String ptsId) {
        return getMapper().toDto(getRepository().findByStationControllerPtsId(ptsId));
    }
    public UserDto findMasterUserWithCustomerAccountId(Long customerAccountId){
        return userMapper.toDto(getRepository().findMasterUserWithCustomerAccountId(customerAccountId));
    }

    @Override
    protected CustomerAccount beforeCreate(CustomerAccount customerAccount, CustomerAccountDto dto) {
        customerAccount.setActif(true);
        if (dto.getParentId() != null) {
            customerAccount.setParent(getEntityManager().getReference(CustomerAccount.class, dto.getParentId()));
        }
        if (dto.getCreatorAccountId() != null) {
            customerAccount.setCreatorAccount(getEntityManager().getReference(CustomerAccount.class, dto.getCreatorAccountId()));
        }
        customerAccount.setCreatorUser(getEntityManager().getReference(User.class, dto.getCreatorUserId()));

        customerAccount.setDateStatusChange(LocalDateTime.now());
        customerAccount.getMasterUser().setCustomerAccount(customerAccount);
        customerAccount.getMasterUser().setCreatorAccount(customerAccount);
        customerAccount.getMasterUser().setUserType(User.EnumUserType.APPLICATION);
        customerAccount.getMasterUser().setDateStatusChange(LocalDateTime.now());
        if (!dto.isCardManager()){
            customerAccount.setPlannedExportDate(null);
        }
        CustomerAccount savedCustomerAccount = super.beforeCreate(customerAccount, dto);
        if (!dto.getPaymentMethods().isEmpty()) {
            savedCustomerAccount.getPaymentMethods().forEach(paymentMethod -> paymentMethod.setCustomerAccount(savedCustomerAccount));
        }
        return savedCustomerAccount;

    }

    @Override
    protected CustomerAccount beforeUpdate(CustomerAccount customerAccount, CustomerAccountDto dto) {
        if (dto.getParentId() != null) {
            customerAccount.setParent(getEntityManager().getReference(CustomerAccount.class, dto.getParentId()));
        }
        if (dto.getCreatorAccountId() != null) {
            customerAccount.setCreatorAccount(getEntityManager().getReference(CustomerAccount.class, dto.getCreatorAccountId()));
        }
        if (Objects.nonNull(dto.getCreatorUserId())) {
            customerAccount.setCreatorUser(getEntityManager().getReference(User.class, dto.getCreatorUserId()));
        }
        if (Objects.nonNull(dto.getMasterUserId())) {
            customerAccount.setMasterUser(getEntityManager().getReference(User.class, dto.getMasterUserId()));
        }
        if (Objects.nonNull(dto.getPaymentMethods())) {
            customerAccount.getPaymentMethods().forEach(paymentMethod -> paymentMethod.setCustomerAccount(customerAccount));
        }
        if (!dto.isCardManager()){
            customerAccount.setPlannedExportDate(null);
        }
        if (dto.isExported()){
            customerAccount.setExported(true);
        }
        customerAccount.setDateStatusChange(LocalDateTime.now());
        return super.beforeUpdate(customerAccount, dto);
    }

    public Optional<CustomerAccountDto> findByIdentifier(String customerAccountIdentifier) {
        return Optional.ofNullable(getMapper().toDto(getRepository().findByIdentifier(customerAccountIdentifier).orElse(null)));

    }

    @Transactional
    public Optional<CustomerAccountDto> findByMasterUser(String username) {
        return Optional.ofNullable(getMapper().toDto(getRepository().findAllByMasterUsername(username).orElse(null)));
    }
}
