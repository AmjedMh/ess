package com.teknokote.ess.controller.front.customer_account;

import com.teknokote.core.exceptions.ServiceValidationException;
import com.teknokote.ess.controller.EndPoints;
import com.teknokote.ess.core.model.organization.User;
import com.teknokote.ess.core.service.UserHistoryService;
import com.teknokote.ess.core.service.impl.CustomerAccountService;
import com.teknokote.ess.dto.CustomerAccountDto;
import com.teknokote.ess.dto.StationDto;
import com.teknokote.ess.dto.UserDto;
import com.teknokote.ess.dto.UserHistoryDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.ws.rs.PathParam;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@RestController
@CrossOrigin("*")
@RequestMapping(EndPoints.CUSTOMER_ACCOUNT_ROOT)
public class CustomerAccountController {
    @Autowired
    private CustomerAccountService customerAccountService;
    @Autowired
    private UserHistoryService userHistoryService;

    /**
     * Endpoints pour les traitements des fonctionnalités liés au Compte Client
     */
    @PostMapping(EndPoints.ADD)
    public ResponseEntity<Object> addCustomerAccount(@RequestBody CustomerAccountDto dto) {
        try {
            CustomerAccountDto savedCustomerAccount = customerAccountService.create(dto);
            return new ResponseEntity<>(savedCustomerAccount, HttpStatus.CREATED);
        } catch (ServiceValidationException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("isUserAttachedToParentOrCustomerAccountOf(#id)")
    @PostMapping(EndPoints.EXPORT)
    public void exportToSupplier(@RequestBody CustomerAccountDto dto) {
        customerAccountService.exportSupplier(dto);
    }

    @PutMapping(EndPoints.UPDATE)
    public ResponseEntity<CustomerAccountDto> updateCustomerAccount(@RequestBody CustomerAccountDto dto, HttpServletRequest servletRequest) {
        CustomerAccountDto savedCustomerAccount = customerAccountService.updateCustomerAccount(dto,servletRequest);
        return new ResponseEntity<>(savedCustomerAccount, HttpStatus.CREATED);
    }

    @GetMapping(EndPoints.CUSTOMER_ACCOUNT_LIST)
    public Page<CustomerAccountDto> listCustomerAccounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {
        User connectedUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<CustomerAccountDto> customerAccounts = customerAccountService.findChildCustomerAccounts(connectedUser);
        customerAccounts.sort(Comparator.comparing(CustomerAccountDto::getCreatedDate));
        int start = page * size;
        int end = Math.min((start + size), customerAccounts.size());
        return new PageImpl<>(customerAccounts.subList(start, end), PageRequest.of(page, size), customerAccounts.size());
    }

    @GetMapping(EndPoints.LIST_BY_ACTIF)
    public List<CustomerAccountDto> listCustomerAccountsByActif(@RequestParam boolean actif) {
        return customerAccountService.findAllByActif(actif);
    }

    @GetMapping
    public List<CustomerAccountDto> customerAccounts() {
        return customerAccountService.findCustomerAccountToExport();
    }

    @GetMapping(EndPoints.CUSTOMER_ACCOUNT_STATIONS_LIST)
    public List<StationDto> allCustomerAccountStations() {

        return customerAccountService.findAllStations();
    }

    @PreAuthorize("isUserAttachedToParentOrCustomerAccountOf(#customerAccountId)")
    @GetMapping(EndPoints.CUSTOMER_ACCOUNT_CREATOR_LIST)
    public List<CustomerAccountDto> getCreatorCustomerAccounts(@PathVariable Long customerAccountId) {
        return customerAccountService.getAllDescendants(customerAccountId);
    }

    @GetMapping(EndPoints.LIST_BY_FILTER)
    public Page<CustomerAccountDto> listCustomerAccountsByFilter(@RequestParam(required = false) String name,
                                                                 @RequestParam(required = false) String creator,
                                                                 @RequestParam(required = false) String parent,
                                                                 @RequestParam(defaultValue = "0") int page,
                                                                 @RequestParam(defaultValue = "25") int size) {
        if (size != 25 && size != 50 && size != 100 && size != 200 && size != 500) {
            throw new IllegalArgumentException("Invalid page size. Allowed values are 25, 50, 100, 500.");
        }

        return customerAccountService.findCustomerAccountByFilter(name, creator, parent, page, size);
    }

    @PreAuthorize("isUserAttachedToParentOrCustomerAccountOf(#customerAccountId)")
    @GetMapping(EndPoints.CUSTOMER_ACCOUNT_INFO)
    public CustomerAccountDto CustomerAccountInformation(@PathVariable Long customerAccountId) {
        return customerAccountService.getDetails(customerAccountId);
    }

    @PreAuthorize("isUserAttachedToParentCustomerAccountOf(#id)")
    @PutMapping(EndPoints.CUSTOMER_ACCOUNT_DEACTIVATE)
    public void deactivateCustomerAccount(@PathVariable Long id) {
        customerAccountService.deactivate(id);
    }

    @PreAuthorize("isUserAttachedToParentCustomerAccountOf(#id)")
    @PutMapping(EndPoints.CUSTOMER_ACCOUNT_ACTIVATE)
    public void activateCustomerAccount(@PathVariable Long id) {
        customerAccountService.activate(id);
    }

    /**
     * Endpoints por  les traitements des fonctionnalités liés aux utilisateurs attachés a un Compte Client
     */

    @PreAuthorize("isUserAttachedToParentOrCustomerAccountOf(#customerAccountId)")
    @PostMapping(EndPoints.CUSTOMER_ACCOUNT_USER_ADD)
    public ResponseEntity<Object> addUserToCustomerAccount(@PathVariable Long customerAccountId,
                                                           @RequestBody UserDto userDto,
                                                           @RequestParam(value = "photoFile", required = false) MultipartFile photoFile) {
        try {
            UserDto savedCustomerAccountUser = customerAccountService.addUserToCustomerAccount(customerAccountId, userDto, photoFile);
            return new ResponseEntity<>(savedCustomerAccountUser, HttpStatus.CREATED);
        } catch (ServiceValidationException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("isUserAttachedToParentOrCustomerAccountOf(#customerAccountId)")
    @GetMapping(EndPoints.CUSTOMER_ACCOUNT_USER_LIST)
    public List<UserDto> listCustomerAccountsUsers(@PathVariable Long customerAccountId) {
        return customerAccountService.listCustomerAccountUsers(customerAccountId);
    }

    @PreAuthorize("isUserAttachedToCustomerAccount(#customerAccountId)")
    @PostMapping(EndPoints.CUSTOMER_ACCOUNT_USER_ACTIVATE)
    public void activateCustomerAccountUser(@PathVariable Long customerAccountId, @PathVariable Long id,HttpServletRequest servletRequest) {
        customerAccountService.activateCustomerAccountUser(customerAccountId, id,servletRequest);
    }

    @PreAuthorize("isUserAttachedToCustomerAccount(#customerAccountId)")
    @PostMapping(EndPoints.CUSTOMER_ACCOUNT_USER_DEACTIVATE)
    public void deactivateCustomerAccountUser(@PathVariable Long customerAccountId, @PathVariable Long id,HttpServletRequest servletRequest) {
        customerAccountService.deactivateCustomerAccountUser(customerAccountId, id,servletRequest);

    }

    @PreAuthorize("isUserAttachedToCustomerAccount(#customerAccountId)")
    @PostMapping(EndPoints.CUSTOMER_ACCOUNT_USER_UPDATE)
    public void updateCustomerAccountUser(@PathVariable Long customerAccountId, @RequestBody UserDto userDto,HttpServletRequest servletRequest) {
        customerAccountService.updateCustomerAccountUser(customerAccountId, userDto,servletRequest);
    }

    @PreAuthorize("isUserAttachedToCustomerAccount(#customerAccountId)")
    @GetMapping(EndPoints.CUSTOMER_ACCOUNT_USER_LIST_BY_ACTIF)
    public List<UserDto> listCustomerAccountsUsersByActif(@PathVariable Long customerAccountId, @PathParam("status") boolean actif) {
        return customerAccountService.listCustomerAccountUsersByActif(customerAccountId, actif);
    }

    @PreAuthorize("isUserAttachedToCustomerAccount(#customerAccountId)")
    @GetMapping(EndPoints.USERS_LOG)
    public List<UserHistoryDto> logUserAction(@PathVariable Long customerAccountId,@PathVariable Long userId, @RequestParam(required = false) LocalDateTime startDate,
                                              @RequestParam(required = false) LocalDateTime endDate) {
        return userHistoryService.userLogs(userId,startDate,endDate);
    }
}