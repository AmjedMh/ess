package com.teknokote.ess.core.service.impl;

import com.teknokote.core.exceptions.EntityNotFoundException;
import com.teknokote.core.exceptions.ServiceValidationException;
import com.teknokote.core.service.ActivatableGenericCheckedService;
import com.teknokote.core.service.ESSValidationResult;
import com.teknokote.ess.core.dao.CustomerAccountDao;
import com.teknokote.ess.core.dao.StationDao;
import com.teknokote.ess.core.dao.mappers.UserMapper;
import com.teknokote.ess.core.model.organization.User;
import com.teknokote.ess.core.service.UserScopeService;
import com.teknokote.ess.core.service.impl.validators.CustomerAccountValidator;
import com.teknokote.ess.core.service.impl.validators.UserValidator;
import com.teknokote.ess.core.service.shifts.PaymentMethodService;
import com.teknokote.ess.dto.CustomerAccountDto;
import com.teknokote.ess.dto.FuelGradeConfigDto;
import com.teknokote.ess.dto.StationDto;
import com.teknokote.ess.dto.UserDto;
import com.teknokote.ess.events.publish.CustomerAccountExport;
import com.teknokote.ess.events.publish.cm.CMSupplierDto;
import com.teknokote.ess.events.publish.cm.exception.ExceptionHandlerUtil;
import com.teknokote.ess.http.logger.EntityActionEvent;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.IntStream;

@Service
@Getter
@Setter
@Slf4j
public class CustomerAccountService extends ActivatableGenericCheckedService<Long, CustomerAccountDto> {
    @Autowired
    private CustomerAccountDao dao;
    @Autowired
    private CustomerAccountValidator validator;
    @Autowired
    private StationService stationService;
    @Autowired
    private UserValidator userValidator;
    @Autowired
    private UserService userService;
    @Autowired
    private KeycloakService keycloakService;
    @Autowired
    private UserScopeService userScopeService;
    @Autowired
    private PaymentMethodService paymentMethodService;
    @Autowired
    private CustomerAccountExport customerAccountExport;
    @Autowired
    private StationDao stationDao;
    @Autowired
    private UserMapper userMapper;
    @Value("${photo.storage.path}")
    private String photoStoragePath;
    @Autowired
    private FuelGradesService fuelGradesService;
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public static final String INEXISTANT_CUSTOMER_ACCOUNT = "Compte client inexistant";
    private static final String CARD_MANAGER = "Card Manager";
    private static final String CUSTOMER_ACCOUNT_NOT_FOUND = ") not found on customer id(";
    private static final String USER_WITH_ID = "User with id(";
    private static final String FOR_CUSTOMER_ACCOUNT = " pour le  compte client ";
    /**
     * Methodes de gestion d'un Compte Client
     */
    @Transactional
    @Override
    public CustomerAccountDto create(CustomerAccountDto dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        HttpServletRequest request = getCurrentHttpRequest();
        String loggedInUsername = authentication.getName();
        final ESSValidationResult customerAccountValidation = getValidator().validateOnCreate(dto);

        if (customerAccountValidation.hasErrors()) {
            throw new ServiceValidationException(customerAccountValidation.getMessage());
        }

        Optional<UserDto> userDto = userService.findByUsername(loggedInUsername);
        if (userDto.isEmpty()) {
            throw new EntityNotFoundException("User not found with username: " + loggedInUsername);
        }

        dto.setCreatorUserId(userDto.get().getId());
        CustomerAccountDto savedCustomerAccount = create(dto, false);

        UserDto masterUser = dto.getMasterUser();
        masterUser.setCustomerAccountId(savedCustomerAccount.getId());
        masterUser.setUserType(User.EnumUserType.APPLICATION);
        masterUser.setUsername(masterUser.getUsername().toLowerCase());
        masterUser.setCreatorAccountId(savedCustomerAccount.getId());
        keycloakService.createUser(masterUser);

        EntityActionEvent event = new EntityActionEvent(
                this,
                "Ajout compte client '" + savedCustomerAccount.getName() + "'",
                (User) authentication.getPrincipal(),
                request
        );
        eventPublisher.publishEvent(event);
        return savedCustomerAccount;
    }
    private HttpServletRequest getCurrentHttpRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            return ((ServletRequestAttributes) requestAttributes).getRequest();
        }
        return null;
    }

    @Transactional
    public void exportSupplier(CustomerAccountDto customerAccountDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        HttpServletRequest request = getCurrentHttpRequest();
        if (customerAccountDto.isCardManager() && !customerAccountDto.isExported()) {
            try {
                customerAccountExport.customerAccountCreated(customerAccountDto);
                customerAccountDto.setExported(true);
                getDao().update(customerAccountDto);
                // Retrieve unique FuelGradeConfigDto for all stations linked to customerAccount
                List<FuelGradeConfigDto> uniqueFuelGrades = getUniqueFuelGradesForCustomerAccount(customerAccountDto);
                exportProduct(customerAccountDto.getId(),uniqueFuelGrades);
                EntityActionEvent event = new EntityActionEvent(
                        this,
                        "export customerAccount " +"'"+ customerAccountDto.getName()+"'",
                        authentication != null ? (User) authentication.getPrincipal() : null,
                        request
                );
                eventPublisher.publishEvent(event);
            } catch (Exception e) {
                ExceptionHandlerUtil.handleException(e, CARD_MANAGER);
            }
        } else {
            throw new ServiceValidationException("You can't export this account to Card Manager Module because it doesn't have rights!");
        }
    }
    public List<FuelGradeConfigDto> getUniqueFuelGradesForCustomerAccount(CustomerAccountDto customerAccountDto) {
        Set<StationDto> stations = customerAccountDto.getStations();
        Map<String, FuelGradeConfigDto> uniqueFuelGradesMap = new HashMap<>();
        for (StationDto station : stations) {
            List<FuelGradeConfigDto> stationFuelGrades = fuelGradesService.findFuelGradesByControllerOnCurrentConfiguration(station.getControllerPtsId()).stream()
                    .map(fuelGrade -> fuelGradesService.mapToFuelGradeConfigDto(fuelGrade)).toList();
            for (FuelGradeConfigDto fuelGrade : stationFuelGrades) {
                uniqueFuelGradesMap.putIfAbsent(fuelGrade.getName(), fuelGrade);
            }
        }
        // Return unique fuel grades
        return new ArrayList<>(uniqueFuelGradesMap.values());
    }
    void exportProduct(Long customerAccountId, List<FuelGradeConfigDto> uniqueFuelGrades) {
        uniqueFuelGrades.forEach(fuelGrade -> {
            fuelGrade.setReference(String.valueOf(String.valueOf(customerAccountId)));
            // export product
            customerAccountExport.productCreated(fuelGrade);
        });
    }
    @Transactional
    public CustomerAccountDto updateCustomerAccount(CustomerAccountDto customerAccountDto, HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomerAccountDto oldCustomerAccount = checkedFindById(customerAccountDto.getId());
        CustomerAccountDto customerAccount = update(customerAccountDto);

        if (customerAccount == null) {
            // Handle the null case gracefully
            throw new IllegalStateException("Customer account update failed.");
        }

        if (oldCustomerAccount != null && oldCustomerAccount.getMasterUser() != null) {
            keycloakService.updateUser(oldCustomerAccount.getMasterUser().getUsername(), customerAccountDto.getMasterUser());
        }

        if (customerAccount.isCardManager()) {
            if (customerAccount.isExported()) {
                try {
                    customerAccountExport.customerAccountUpdated(customerAccountDto);
                } catch (Exception e) {
                    ExceptionHandlerUtil.handleException(e, CARD_MANAGER);
                }
            } else {
                customerAccountDto.setStations(customerAccount.getStations());
                customerAccountDto.setAttachedUsers(customerAccount.getAttachedUsers());
            }
        }

        // Ensure customerAccount and its properties are not null before accessing them
        String customerAccountName = (customerAccount.getName() != null) ? customerAccount.getName() : "Unknown Account";

        EntityActionEvent event = new EntityActionEvent(
                this,
                "Modification compte client '" + customerAccountName + "'",
                (User) authentication.getPrincipal(),
                request
        );

        eventPublisher.publishEvent(event);
        return customerAccount;
    }

    @Transactional
    public StationDto updateStation(StationDto stationDto, HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        StationDto oldStationDto = stationService.checkedFindById(stationDto.getId());
        CustomerAccountDto customerAccount = checkedFindById(stationDto.getCustomerAccountId());

        // Ensure ControllerPts and UserController are not null before accessing them
        if (stationDto.getControllerPts() != null
                && stationDto.getControllerPts().getUserController() != null
                && stationDto.getControllerPts().getUserController().getPassword() == null) {
            stationDto.getControllerPts().setUserController(oldStationDto.getControllerPts().getUserController());
        }

        // Update station
        StationDto stationDto1 = stationService.update(stationDto);
        if (stationDto1 == null) {
            throw new IllegalStateException("Failed to update station for customer account: " + customerAccount.getId());
        }

        // Update Keycloak user if the update was successful
        if (oldStationDto.getControllerPts() != null && oldStationDto.getControllerPts().getUserController() != null) {
            keycloakService.updateUser(oldStationDto.getControllerPts().getUserController().getUsername(), stationDto.getControllerPts().getUserController());

            // Change user controller password if it's provided
            if (stationDto.getControllerPts().getUserController().getPassword() != null) {
                stationService.changeUserController(stationDto1.getId(), stationDto.getControllerPts().getUserController());
            }
        }

        // Export updated information if the customer is a card manager and exported
        if (customerAccount.isCardManager() && customerAccount.isExported()) {
            Optional<CMSupplierDto> supplierDto = customerAccountExport.exportedCustomerAccount(String.valueOf(customerAccount.getId()));
            supplierDto.ifPresent(supplier -> {
                try {
                    customerAccountExport.salePointUpdated(supplier.getId(), stationDto);
                } catch (Exception e) {
                    ExceptionHandlerUtil.handleException(e, CARD_MANAGER);
                }
            });
        }

        // Safely access station and customer account names
        String stationName = (stationDto1.getName() != null) ? stationDto1.getName() : "Unknown Station";
        String customerAccountName = (customerAccount.getName() != null) ? customerAccount.getName() : "Unknown Account";

        // Publish entity action event
        EntityActionEvent event = new EntityActionEvent(
                this,
                "Modification station '" + stationName + "' pour le compte client '" + customerAccountName + "'",
                (User) authentication.getPrincipal(),
                request
        );
        eventPublisher.publishEvent(event);

        return stationDto1;
    }

    public Page<CustomerAccountDto> findAllCustomerAccounts(Pageable pageable) {
        Page<CustomerAccountDto> allCustomerAccounts = getDao().findAllCustomerAcount(pageable);
        for (CustomerAccountDto customerAccount : allCustomerAccounts) {
            customerAccount.setStationsCount(stationService.countStations(customerAccount.getId()));
        }
        return allCustomerAccounts;
    }

    public Page<CustomerAccountDto> findCustomerAccountByFilter(String name, String creator, String parent, int page, int size) {
        Page<CustomerAccountDto> customerAccountDtoList = null;

        if (name != null) {
            customerAccountDtoList = getDao().findCustomerAccountByName(name, PageRequest.of(page, size));
        } else if (creator != null) {
            customerAccountDtoList = getDao().findByCustomerAccountByCreator(creator, PageRequest.of(page, size));
        } else if (parent != null) {
            customerAccountDtoList = getDao().findByCustomerAccountByParent(parent, PageRequest.of(page, size));
        } else if (name == null && creator == null && parent == null) {
            Page<CustomerAccountDto> allCustomerAccounts = this.findAllCustomerAccounts(PageRequest.of(page, size));
            customerAccountDtoList = allCustomerAccounts;
        } else {
            return null;
        }

        return customerAccountDtoList;
    }

    public List<StationDto> findStationByFilter(User user, String name, String creator, String parent) {
        List<StationDto> stationDtoList = findStationsByCustomerAccount(user);
        if (!stationDtoList.isEmpty()) {
            if (name != null) {
                stationDtoList = stationDtoList.stream()
                        .filter(stationDto -> stationDto.getName().toLowerCase().contains(name.toLowerCase()))
                        .toList();
            } else if (creator != null) {
                stationDtoList = stationDtoList.stream()
                        .filter(stationDto -> stationDto.getCreatorCustomerAccountName().toLowerCase().contains(creator.toLowerCase()))
                        .toList();
            } else if (parent != null) {
                stationDtoList = stationDtoList.stream()
                        .filter(stationDto -> stationDto.getCustomerAccountName().toLowerCase().contains(parent.toLowerCase()))
                        .toList();
            }
            return stationDtoList;
        }

        return stationDtoList;
    }

    public List<UserDto> listCustomerAccountUsers(Long id) {
        final CustomerAccountDto customerAccountDto = checkedFindById(id);
        List<UserDto> userDtoList = customerAccountDto.getAttachedUsers().stream().toList();
        // Retrieve the usernames of attached users
        List<String> usernames = userDtoList.stream()
                .map(UserDto::getUsername)
                .toList();
        // Use the modified method to get user details for the list of usernames
        List<UserRepresentation> userRepresentations = keycloakService.getUserIdentities(usernames);
        // Map Keycloak user representations to your UserDto
        List<UserDto> mappedUserDtos = userRepresentations.stream()
                .map(userRepresentation ->
                    userMapper.userRepresentationToDto(userRepresentation)
                )
                .toList();
        // Set the id for each UserDto based on the original list
        IntStream.range(0, userDtoList.size())
                .forEach(index -> mappedUserDtos.get(index).setId(userDtoList.get(index).getId()));

        return mappedUserDtos;
    }

    public List<StationDto> listCustomerAccountStations(Long id) {
        final CustomerAccountDto customerAccountDto = checkedFindById(id);

        return customerAccountDto.getStations().stream().toList();
    }

    public List<UserDto> listCustomerAccountUsersByActif(Long id, Boolean actif) {
        final CustomerAccountDto customerAccountDto = checkedFindById(id);
        return customerAccountDto.getAttachedUsers()
                .stream()
                .filter(userDto -> actif.equals(userDto.getActif()))
                .toList();
    }

    @Transactional
    public UserDto addUserToCustomerAccount(Long customerAccountId, UserDto userDto, MultipartFile photoFile) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        HttpServletRequest request=getCurrentHttpRequest();
        final ESSValidationResult validationResult = getValidator().validateOnAddUser(customerAccountId, userDto);

        if (validationResult.hasErrors()) {
            throw new ServiceValidationException(validationResult.getMessage());
        }
        CustomerAccountDto customerAccountDto = checkedFindById(customerAccountId);
        userDto.setActif(true);
        userDto.setDateStatusChange(LocalDateTime.now());
        userDto.setUserType(User.EnumUserType.APPLICATION);
        userDto.setCustomerAccountId(customerAccountId);
        userDto.setUsername(userDto.getUsername().toLowerCase());
        if (photoFile != null && !photoFile.isEmpty()) {
            String photoPath = savePhoto(photoFile);
            userDto.setPhotoPath(photoPath);
        }
        userService.create(userDto);
        keycloakService.createUser(userDto);
        EntityActionEvent event = new EntityActionEvent(
                this,
                "Ajout utilisateur "+"'"+userDto.getUsername()+"'"+FOR_CUSTOMER_ACCOUNT +"'"+ customerAccountDto.getName()+"'",
                (User)authentication.getPrincipal(),
                request
        );
        eventPublisher.publishEvent(event);
        return userDto;
    }

    private String savePhoto(MultipartFile photoFile) {
        String fileName = "user_" + UUID.randomUUID() + ".jpg";

        try {
            Files.copy(photoFile.getInputStream(), Paths.get(photoStoragePath, fileName), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ServiceValidationException("Could not save photo");
        }
        return photoStoragePath + fileName;
    }

    /**
     * Les étapes d'ajout de station à un compte client:
     * 1. Valider l'existence du customeraccount
     * 2. Valider le dto stationDto
     * 3. mettre à jour le customer account
     */
    @Transactional
    public StationDto addStation(StationDto dto,HttpServletRequest request) {
        Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
        dto.setActif(true);
        dto.setDateStatusChange(LocalDateTime.now());
        final ESSValidationResult stationValidationResult = getValidator().validateOnAddStation(dto);

        if (stationValidationResult.hasErrors()) {
            throw new ServiceValidationException(stationValidationResult.getMessage());
        }
        UserDto userController = dto.getControllerPts().getUserController();
        userController.setUserType(User.EnumUserType.CONTROLLER);
        UserDto savedUser = userService.create(userController);
        if (savedUser != null) {
            dto.getControllerPts().setUserController(savedUser);
        }
        StationDto createdStation = stationService.create(dto, true);
        keycloakService.createUser(userController);
        CustomerAccountDto customerAccountDto = checkedFindById(dto.getCustomerAccountId());
        if (customerAccountDto.isCardManager() && customerAccountDto.isExported()) {
            Optional<CMSupplierDto> supplierDto = customerAccountExport.exportedCustomerAccount(String.valueOf(customerAccountDto.getId()));
            if (supplierDto.isPresent()) {
                try {
                    customerAccountExport.salePointCreated(supplierDto.get().getId(), dto);
                } catch (Exception e) {
                    ExceptionHandlerUtil.handleException(e, CARD_MANAGER);
                }
            }
        }
        EntityActionEvent event = new EntityActionEvent(
                this,
                "Ajout station " +"'"+ dto.getName()+"'" +FOR_CUSTOMER_ACCOUNT+"'"+customerAccountDto.getName()+"'",
                (User)authentication.getPrincipal(),
                request
        );
        eventPublisher.publishEvent(event);
        return createdStation;
    }

    @Transactional
    public void updateCustomerAccountUser(Long customerAccountId, UserDto userDto,HttpServletRequest request) {
        Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
        final CustomerAccountDto customerAccountDto = checkedFindById(customerAccountId);
        final Optional<UserDto> foundUser = customerAccountDto.getAttachedUsers().stream().filter(dto -> userDto.getId().equals(dto.getId())).findFirst();
        if (foundUser.isEmpty())
            throw new EntityNotFoundException(USER_WITH_ID + userDto.getId() + CUSTOMER_ACCOUNT_NOT_FOUND + customerAccountId + ")");
        keycloakService.updateUser(foundUser.get().getUsername(), userDto);
        userService.update(userDto);
        EntityActionEvent event = new EntityActionEvent(
                this,
                "Modification utilsateur "+"'"+userDto.getUsername()+"'"+ FOR_CUSTOMER_ACCOUNT +"'"+ customerAccountDto.getName()+"'",
                (User)authentication.getPrincipal(),
                request
        );
        eventPublisher.publishEvent(event);
    }

    @Transactional
    public void activateCustomerAccountUser(Long customerAccountId, Long userId,HttpServletRequest request) {
        Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
        final CustomerAccountDto customerAccountDto = checkedFindById(customerAccountId);
        final Optional<UserDto> foundUser = customerAccountDto.getAttachedUsers().stream().filter(dto -> userId.equals(dto.getId())).findFirst();
        if (foundUser.isEmpty())
            throw new EntityNotFoundException(USER_WITH_ID + userId + CUSTOMER_ACCOUNT_NOT_FOUND + customerAccountId + ")");
        userService.activate(foundUser.get());
        EntityActionEvent event = new EntityActionEvent(
                this,
                "Activation de  l'utilisateur "+"'"+foundUser.get().getUsername()+"'"+" pour le compte client " +"'"+ customerAccountDto.getName()+"'",
                (User)authentication.getPrincipal(),
                request
        );
        eventPublisher.publishEvent(event);
    }

    @Transactional
    public void deactivateCustomerAccountUser(Long customerAccountId, Long userId,HttpServletRequest request) {
        Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
        final CustomerAccountDto customerAccountDto = checkedFindById(customerAccountId);
        final Optional<UserDto> foundUser = customerAccountDto.getAttachedUsers().stream().filter(dto -> userId.equals(dto.getId())).findFirst();
        if (foundUser.isEmpty())
            throw new EntityNotFoundException(USER_WITH_ID + userId + CUSTOMER_ACCOUNT_NOT_FOUND + customerAccountId + ")");
        userService.deactivate(foundUser.get());
        EntityActionEvent event = new EntityActionEvent(
                this,
                "Désactivation de l'utilisateur "+"'"+foundUser.get().getUsername()+"'"+" pour le compte client " +"'"+ customerAccountDto.getName()+"'",
                (User)authentication.getPrincipal(),
                request
        );
        eventPublisher.publishEvent(event);
    }

    // Method to retrieve all descendants for a given parent account and its children
    public List<CustomerAccountDto> getAllDescendants(Long parentId) {
        List<CustomerAccountDto> allDescendants = new ArrayList<>();
        CustomerAccountDto parentAccount = checkedFindById(parentId);
        if (parentAccount != null) {
            allDescendants.add(parentAccount);
            List<CustomerAccountDto> descendants = getDescendants(parentId);
            // Add descendants to the list
            allDescendants.addAll(descendants);
        }
        return allDescendants;
    }

    // Method to retrieve descendants for a given account
    private List<CustomerAccountDto> getDescendants(Long accountId) {
        List<CustomerAccountDto> descendants = new ArrayList<>();
        findDescendants(accountId, descendants);
        return descendants;
    }

    // Recursive method to find descendants
    private void findDescendants(Long accountId, List<CustomerAccountDto> descendants) {
        // Retrieve the current account
        CustomerAccountDto account = checkedFindById(accountId);

        if (account != null) {
            List<CustomerAccountDto> children = getDao().findCustomerAccountByParent(accountId).stream().toList();
            for (CustomerAccountDto child : children) {
                descendants.add(child);
                findDescendants(child.getId(), descendants);
            }
        }
    }

    public List<StationDto> findAllStations() {
        return stationService.findAll();
    }

    public List<CustomerAccountDto> findChildCustomerAccounts(User connectedUser) {
        Optional<CustomerAccountDto> customerAccountDto = getDao().findByMasterUser(connectedUser.getUsername());
        if (customerAccountDto.isPresent()){
            return new ArrayList<>(findChildren(customerAccountDto.get()));
        }else{
            throw new  ServiceValidationException("Customer Account not found");
        }
    }

    /**
     * Renvoie tous les comptes client enfants du compte client
     */
    public List<CustomerAccountDto> findChildren(CustomerAccountDto customerAccount) {
        List<CustomerAccountDto> customerAccountDtos = new ArrayList<>();
        customerAccountDtos.add(customerAccount);
        final List<CustomerAccountDto> children = getDao().findCustomerAccountByParent(customerAccount.getId());
        customerAccount.setStationsCount(stationService.countStations(customerAccount.getId()));
        if (CollectionUtils.isEmpty(children)) {
            return customerAccountDtos;
        }
        // Split children into two lists based on resaleRight
        List<CustomerAccountDto> noResaleRightChildren = new ArrayList<>();
        List<CustomerAccountDto> withResaleRightChildren = new ArrayList<>();

        for (CustomerAccountDto child : children) {
            if (child.isResaleRight()) {
                withResaleRightChildren.add(child);
            } else {
                noResaleRightChildren.add(child);
            }
        }

        // Recursively add children with no resaleRight first
        for (CustomerAccountDto child : noResaleRightChildren) {
            customerAccountDtos.addAll(findChildren(child));  // Recursively add children and their descendants
        }

        for (CustomerAccountDto child : withResaleRightChildren) {
            customerAccountDtos.addAll(findChildren(child));  // Recursively add children and their descendants
        }

        int stations = customerAccount.getStationsCount();
        for (CustomerAccountDto child : children) {
            stations += child.getStationsCount();
        }
        customerAccount.setStationsCount(stations);
        return customerAccountDtos;
    }

    public CustomerAccountDto getDetails(Long id) {
        CustomerAccountDto customerAccountDto = getDao().findById(id).orElseThrow(() -> new EntityNotFoundException("Customer Account with id: " + id + " not found"));
        UserDto masterUserDetails = keycloakService.getUserIdentity(customerAccountDto.getMasterUser().getUsername())
                .map(master -> UserDto.builder()
                        .userIdentifier(customerAccountDto.getMasterUser().getUserIdentifier())
                        .username(master.getUsername())
                        .email(master.getEmail())
                        .firstName(master.getFirstName())
                        .lastName(master.getLastName())
                        .phone(master.getAttributes().get("phone") != null ? master.getAttributes().get("phone").stream().findFirst().orElse("") : "")
                        .build())
                .orElseThrow(() -> new EntityNotFoundException("User with username: " + customerAccountDto.getMasterUser().getUsername() + " not found"));

        customerAccountDto.setMasterUser(masterUserDetails);

        return customerAccountDto;
    }

    public List<StationDto> findStationsByCustomerAccount(User user) {

        final List<CustomerAccountDto> childCustomerAccounts = findChildCustomerAccounts(user);

        return childCustomerAccounts.stream()
                .flatMap(ca -> listCustomerAccountStations(ca.getId()).stream())
                .toList();

    }

    @Transactional
    public List<CustomerAccountDto> findCustomerAccountToExport() {
        return getDao().findAllByExportedStatusAndReachedExportDate(false, true);
    }
    @Override
    public CustomerAccountDto activate(Long id){
        Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
        HttpServletRequest request=getCurrentHttpRequest();
        CustomerAccountDto activatedCustomer= dao.activate(id);
        EntityActionEvent event = new EntityActionEvent(
                this,
                "Activation du  compte client "+"'"+ activatedCustomer.getName()+"'",
                (User)authentication.getPrincipal(),
                request
        );
        eventPublisher.publishEvent(event);
        return activatedCustomer;
    }

    @Override
    public CustomerAccountDto deactivate(Long id){
        Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
        HttpServletRequest request=getCurrentHttpRequest();
        CustomerAccountDto activatedCustomer= dao.deactivate(id);
        EntityActionEvent event = new EntityActionEvent(
                this,
                "Désactivation du  compte client "+"'"+ activatedCustomer.getName()+"'",
                (User)authentication.getPrincipal(),
                request
        );
        eventPublisher.publishEvent(event);
        return activatedCustomer;
    }

    public UserDto findMasterUserWithCustomerAccountId(Long customerAccountId){
        return getDao().findMasterUserWithCustomerAccountId(customerAccountId);
    }
}
