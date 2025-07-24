package com.teknokote.ess.core.dao;

import com.teknokote.core.dao.JpaActivatableGenericDao;
import com.teknokote.core.model.AuditableEntity;
import com.teknokote.ess.core.dao.mappers.UserMapper;
import com.teknokote.ess.core.model.organization.CustomerAccount;
import com.teknokote.ess.core.model.organization.User;
import com.teknokote.ess.core.repository.UserRepository;
import com.teknokote.ess.dto.UserDto;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
@Getter
public class UserDao extends JpaActivatableGenericDao<Long, User, UserDto, User> {
    @Autowired
    private UserMapper mapper;
    @Autowired
    private UserRepository repository;

    public Optional<UserDto> findAllByUsername(String name) {
        return getRepository().findUserByUsernameIgnoreCase(name).map(getMapper()::toDto);
    }

    public Page<UserDto> findAllUser(Pageable pageable) {
        return getRepository().findApplicationGlobalUsers(User.EnumUserType.APPLICATION, pageable).map(getMapper()::toDto);
    }

    public Page<UserDto> findUserByName(String name, Pageable pageable) {
        return getRepository().findUsertByName(name, pageable).map(getMapper()::toDto);
    }

    public Page<UserDto> findUserByParent(String parent, Pageable pageable) {
        return getRepository().findUserByParent(parent, pageable).map(getMapper()::toDto);
    }

    public Page<UserDto> findUserByCreator(String parent, Pageable pageable) {
        return getRepository().findUserByCreator(parent, pageable).map(getMapper()::toDto);
    }

    @Override
    protected User beforeCreate(User user, UserDto dto) {
        user.setDateStatusChange(LocalDateTime.now());
        user.setUserType(dto.getUserType());
        if (dto.getCustomerAccountId() != null) {
            user.setCustomerAccount(getEntityManager().getReference(CustomerAccount.class, dto.getCustomerAccountId()));
            user.setCreatorAccount(getEntityManager().getReference(CustomerAccount.class, dto.getCustomerAccountId()));
        }
        if (dto.getCreatorAccountId() != null)
            user.setCreatorAccount(getEntityManager().getReference(CustomerAccount.class, dto.getCreatorAccountId()));
        return super.beforeCreate(user, dto);
    }

    @Override
    protected User beforeUpdate(User entity, UserDto dto) {
        entity.setActif(true);
        entity.setCustomerAccount(entity.getCustomerAccount());
        if (entity.getAudit() == null) {
            entity.setAudit(new AuditableEntity<>());
        }
        entity.getAudit().setLastModifiedBy(entity);
        return super.beforeUpdate(entity, dto);
    }

    /**
     * Renvoie la liste des utilisateurs globaux (non liés à un customer account)
     *
     * @return
     */
    @Transactional
    public void updateLastConnection(String userName, LocalDateTime connectionDate) {
        getRepository().updateLastConnection(userName, connectionDate);
    }

    public Optional<UserDto> findByIdentifier(String masterUserIdentifier) {
        return Optional.ofNullable(getMapper().toDto(getRepository().findByUserIdentifier(masterUserIdentifier).orElse(null)));
    }
}
