package com.teknokote.ess.core.repository;

import com.teknokote.core.dao.JpaActivatableRepository;
import com.teknokote.ess.core.model.organization.CustomerAccount;
import com.teknokote.ess.core.model.organization.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerAccountRepository extends JpaActivatableRepository<CustomerAccount, Long> {
    @Query("select ca from CustomerAccount ca join ca.masterUser masterUser where masterUser.username = :masterUsername")
    Optional<CustomerAccount> findAllByMasterUsername(String masterUsername);

    @Query("select ca from CustomerAccount ca where ca.exported = :exported and ca.cardManager =:cardManager")
    List<CustomerAccount> findAllyByExportedStatusAndReachedExportDate(boolean exported, boolean cardManager);

    @Query("select ca from CustomerAccount ca where ca.name = :customerAccountName")
    Optional<CustomerAccount> findByName(String customerAccountName);

    @Query("SELECT ca FROM CustomerAccount ca  order by ca.audit.createdDate desc")
    Page<CustomerAccount> findAllOrderedByCreationDateDesc(Pageable pageable);

    @Query("SELECT ca FROM CustomerAccount ca WHERE LOWER(ca.name) LIKE LOWER(CONCAT('%', :name, '%')) order by ca.audit.createdDate desc")
    Page<CustomerAccount> findCustomerAccountByName(String name, Pageable pageable);

    @Query("SELECT ca FROM CustomerAccount ca WHERE LOWER(ca.name) LIKE LOWER(CONCAT('%', :parent, '%')) order by ca.audit.createdDate desc")
    Page<CustomerAccount> findCustomerAccountByParent(String parent, Pageable pageable);

    @Query("SELECT ca FROM CustomerAccount ca WHERE LOWER(ca.creatorAccount.name) LIKE LOWER(CONCAT('%', :creator, '%')) order by ca.audit.createdDate desc")
    Page<CustomerAccount> findCustomerAccountByCreator(String creator, Pageable pageable);

    @Query("SELECT ca FROM CustomerAccount ca WHERE ca.parentId = :parentId")
    List<CustomerAccount> findByParent(Long parentId);

    @Query("select ca from CustomerAccount ca join ca.stations station where station.controllerPts.ptsId = :ptsId")
    CustomerAccount findByStationControllerPtsId(String ptsId);
    @Query("select ca.masterUser from CustomerAccount ca where ca.id = :customerAccountId")
    User findMasterUserWithCustomerAccountId(Long customerAccountId);

    Optional<CustomerAccount> findByIdentifier(String customerAccountIdentifier);
}
