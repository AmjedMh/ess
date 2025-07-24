package com.teknokote.ess.core.repository;

import com.teknokote.core.dao.JpaActivatableRepository;
import com.teknokote.ess.core.model.organization.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaActivatableRepository<User, Long>
{
    Optional<User> findUserByUsernameIgnoreCase(String username);

    @Query("SELECT ca FROM User ca WHERE ca.userType = 'APPLICATION' and LOWER(ca.username) LIKE LOWER(CONCAT('%', :name, '%')) order by ca.audit.createdDate desc")
    Page<User> findUsertByName(String name, Pageable pageable);

    @Query("SELECT ca FROM User ca WHERE LOWER(ca.customerAccount.name) LIKE LOWER(CONCAT('%', :parent, '%')) order by ca.audit.createdDate desc")
    Page<User> findUserByParent(String parent, Pageable pageable);

    @Query("SELECT ca FROM User ca WHERE LOWER(ca.creatorAccount.name) LIKE LOWER(CONCAT('%', :creator, '%')) order by ca.audit.createdDate desc")
    Page<User> findUserByCreator(String creator, Pageable pageable);

    @Query("select u from User u where u.userType= :userType order by u.audit.createdDate desc")
    Page<User> findApplicationGlobalUsers(User.EnumUserType userType, Pageable pageable);

    @Modifying
    @Query("update User set lastConnectionDate= :connectionDate where username= :userName")
    void updateLastConnection(String userName, LocalDateTime connectionDate);

    Optional<User> findByUserIdentifier(String userIdentifier);
}
