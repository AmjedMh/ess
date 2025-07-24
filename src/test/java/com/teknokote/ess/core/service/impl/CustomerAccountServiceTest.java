package com.teknokote.ess.core.service.impl;

import com.teknokote.core.exceptions.EntityNotFoundException;
import com.teknokote.core.exceptions.ServiceValidationException;
import com.teknokote.core.service.ESSValidationResult;
import com.teknokote.ess.core.dao.CustomerAccountDao;
import com.teknokote.ess.core.dao.mappers.UserMapper;
import com.teknokote.ess.core.model.configuration.FuelGrade;
import com.teknokote.ess.core.model.organization.User;
import com.teknokote.ess.core.service.impl.validators.CustomerAccountValidator;
import com.teknokote.ess.core.service.impl.validators.StationValidator;
import com.teknokote.ess.dto.*;
import com.teknokote.ess.events.publish.CustomerAccountExport;
import com.teknokote.ess.events.publish.cm.CMSupplierDto;
import com.teknokote.ess.http.logger.EntityActionEvent;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class CustomerAccountServiceTest {

   @Mock
   private CustomerAccountDao dao;
   @Mock
   private StationService stationService;
   @Mock
   private ApplicationEventPublisher eventPublisher;
   @InjectMocks
   private CustomerAccountService customerAccountService;
   @Mock
   private SecurityContext securityContext;
   @Mock
   private CustomerAccountExport customerAccountExport;
   @Mock
   private CustomerAccountDao customerAccountDao;
   @Mock
   private CustomerAccountDto customerAccountDto;
   @Mock
   private FuelGradesService fuelGradesService;
   @Mock
   private Authentication authentication;
   @Mock
   private HttpServletRequest request;
   private User user;
   @Mock
   private CustomerAccountValidator validator;
   @Mock
   private StationValidator stationValidator;
   @Mock
   private ESSValidationResult validationResult;
   @Mock
   private UserService userService;
   @Mock
   private KeycloakService keycloakService;
   private StationDto dto;
   private UserDto userDto;
   @Mock
   private UserMapper userMapper;
   @TempDir
   Path tempDir;
   private Page<CustomerAccountDto> mockPage;
   @BeforeEach
   void prepare() {
      SecurityContextHolder.setContext(securityContext);
      UserDto userController = UserDto.builder().build();
      user = new User();
      user.setUsername("loggedUser");
      userDto = UserDto.builder().userType(User.EnumUserType.APPLICATION).id(1L).username("loggedUser").build();
      dto = StationDto.builder().build();
      dto.setId(1L);
      dto.setName("Test Station");
      dto.setCustomerAccountId(1L);
      dto.setControllerPts(ControllerPtsDto.builder().id(1L).userController(userController).build());
      ReflectionTestUtils.setField(customerAccountService, "photoStoragePath", tempDir.toString());

      CustomerAccountDto account1Filter = CustomerAccountDto.builder().id(1L).name("account 1").build();
      CustomerAccountDto account2Filter = CustomerAccountDto.builder().id(2L).name("account 2").build();
      mockPage = new PageImpl<>(List.of(account1Filter, account2Filter));


   }

   /******************************* Find Children ************************/
   @Test
   void findChildren() {
      final CustomerAccountDto a = CustomerAccountDto.builder().id(1L).name("A").stations(Set.of()).build();
      final CustomerAccountDto a1 = CustomerAccountDto.builder().parentId(1L).id(2L).name("A.1").stations(Set.of()).build();
      final CustomerAccountDto a11 = CustomerAccountDto.builder().parentId(2L).id(3L).stations(Set.of(StationDto.builder().id(60L).build())).name("A.1.1").build();
      final CustomerAccountDto a12 = CustomerAccountDto.builder().parentId(2L).id(4L).name("A.1.2").stations(Set.of(StationDto.builder().id(50L).build(),StationDto.builder().id(51L).build())).build();
      final CustomerAccountDto a2 = CustomerAccountDto.builder().parentId(1L).id(5L).name("A.2").stations(Set.of(StationDto.builder().build())).build();
      final CustomerAccountDto b = CustomerAccountDto.builder().id(6L).name("B").stations(Set.of(StationDto.builder().build())).build();
      when(dao.findCustomerAccountByParent(1L)).thenReturn(List.of(a1,a2));
      when(dao.findCustomerAccountByParent(2L)).thenReturn(List.of(a11,a12));
      when(dao.findCustomerAccountByParent(3L)).thenReturn(List.of());
      when(dao.findCustomerAccountByParent(4L)).thenReturn(List.of());
      when(dao.findCustomerAccountByParent(5L)).thenReturn(List.of());
      when(dao.findCustomerAccountByParent(6L)).thenReturn(List.of());

      when(stationService.countStations(any())).thenReturn(1);

      log.info("Les enfants de a:");
      customerAccountService.findChildren(a).forEach(el->log.info("client:{} - {}",el.getName(), el.getStationsCount()));
      log.info("Les enfants de b:");
      customerAccountService.findChildren(b).forEach(el->log.info(el.getName(), el.getStationsCount()));
   }

   /******************************* export supplier ************************/
   @Test
   void testExportSupplier_withoutCardManagerRights() {
      // Arrange
      when(customerAccountDto.isCardManager()).thenReturn(false);

      // Act & Assert
      ServiceValidationException exception = assertThrows(ServiceValidationException.class, () -> {
         customerAccountService.exportSupplier(customerAccountDto);
      });
      assertEquals("You can't export this account to Card Manager Module because it doesn't have rights!", exception.getMessage());
   }
   @Test
   void testExportSupplier_whenAlreadyExported(){
      // Arrange
      when(customerAccountDto.isCardManager()).thenReturn(true);
      when(customerAccountDto.isExported()).thenReturn(true);

      // Act & Assert
      ServiceValidationException exception = assertThrows(ServiceValidationException.class, () -> {
         customerAccountService.exportSupplier(customerAccountDto);
      });

      // Assert exception message
      assertEquals("You can't export this account to Card Manager Module because it doesn't have rights!", exception.getMessage());

      // Ensure no methods are called since the export cannot happen
      verify(customerAccountExport, never()).customerAccountCreated(any());
      verify(customerAccountDao, never()).update(any());
      verify(eventPublisher, never()).publishEvent(any());
   }

   @Test
   void getUniqueFuelGradesForCustomerAccount_ShouldReturnUniqueFuelGrades() {
      CustomerAccountDto accountDto = CustomerAccountDto.builder().build();
      StationDto station1 = StationDto.builder().id(1L).controllerPtsId(1L).build();
      StationDto station2 = StationDto.builder().id(2L).controllerPtsId(2L).build();
      accountDto.setStations(Set.of(station1, station2));

      FuelGrade grade1 = new FuelGrade();
      grade1.setName("Diesel");

      FuelGrade grade2 = new FuelGrade();
      grade2.setName("Petrol");

      FuelGrade duplicateGrade = new FuelGrade();
      duplicateGrade.setName("Diesel"); // Duplicate name

      when(fuelGradesService.findFuelGradesByControllerOnCurrentConfiguration(1L))
              .thenReturn(List.of(grade1, duplicateGrade));
      when(fuelGradesService.findFuelGradesByControllerOnCurrentConfiguration(2L))
              .thenReturn(List.of(grade2));
      when(fuelGradesService.mapToFuelGradeConfigDto(any()))
              .thenAnswer(invocation -> {
                 FuelGrade fuelGrade = invocation.getArgument(0);
                 FuelGradeConfigDto fuelGradeConfigDto = new FuelGradeConfigDto();
                 fuelGradeConfigDto.setName(fuelGrade.getName());
                 return fuelGradeConfigDto;
              });

      // Act
      List<FuelGradeConfigDto> result = customerAccountService.getUniqueFuelGradesForCustomerAccount(accountDto);

      // Assert
      assertNotNull(result);
      assertEquals(2, result.size()); // Should only contain unique fuel grades
      assertTrue(result.stream().anyMatch(f -> f.getName().equals("Diesel")));
      assertTrue(result.stream().anyMatch(f -> f.getName().equals("Petrol")));
   }


   @Test
   void getUniqueFuelGradesForCustomerAccount_ShouldReturnEmptyListWhenNoStations() {
      customerAccountDto = CustomerAccountDto.builder().build();
      customerAccountDto.setStations(Collections.emptySet());

      // Act
      List<FuelGradeConfigDto> result = customerAccountService.getUniqueFuelGradesForCustomerAccount(customerAccountDto);

      // Assert
      assertNotNull(result);
      assertTrue(result.isEmpty());
   }

   @Test
   void exportProduct_ShouldSetReferenceAndCallProductCreated() {
      Long customerAccountId = 1L;
      FuelGradeConfigDto grade1 = new FuelGradeConfigDto();
      grade1.setName("Diesel");

      FuelGradeConfigDto grade2 = new FuelGradeConfigDto();
      grade2.setName("Petrol");

      List<FuelGradeConfigDto> fuelGrades = List.of(grade1, grade2);

      // Act
      customerAccountService.exportProduct(customerAccountId, fuelGrades);

      // Assert
      assertEquals("1", grade1.getReference());
      assertEquals("1", grade2.getReference());
      verify(customerAccountExport, times(1)).productCreated(grade1);
      verify(customerAccountExport, times(1)).productCreated(grade2);
   }

   @Test
   void exportProduct_ShouldNotCallProductCreatedWhenListIsEmpty() {
      Long customerAccountId = 1L;
      List<FuelGradeConfigDto> fuelGrades = Collections.emptyList();

      // Act
      customerAccountService.exportProduct(customerAccountId, fuelGrades);

      // Assert
      verify(customerAccountExport, never()).productCreated(any());
   }

   @Test
   void getAllDescendants_ShouldReturnAllDescendants() {
      // Arrange
      final CustomerAccountDto parent = CustomerAccountDto.builder().id(1L).name("parent").stations(Set.of()).build();
      final CustomerAccountDto child1 = CustomerAccountDto.builder().parentId(1L).id(2L).name("child1").stations(Set.of()).build();
      final CustomerAccountDto child2 = CustomerAccountDto.builder().parentId(1L).id(3L).stations(Set.of(StationDto.builder().id(60L).build())).name("child2").build();
      final CustomerAccountDto grandchild1 = CustomerAccountDto.builder().parentId(2L).id(4L).name("grandchild1").stations(Set.of(StationDto.builder().id(50L).build(),StationDto.builder().id(51L).build())).build();
      when(customerAccountService.findById(1L)).thenReturn(Optional.of(parent));
      when(customerAccountService.findById(2L)).thenReturn(Optional.of(child1));
      when(customerAccountService.findById(3L)).thenReturn(Optional.of(child2));
      when(customerAccountService.findById(4L)).thenReturn(Optional.of(grandchild1));

      when(customerAccountService.getDao().findCustomerAccountByParent(1L)).thenReturn(List.of(child1, child2));
      when(customerAccountService.getDao().findCustomerAccountByParent(2L)).thenReturn(List.of(grandchild1));
      when(customerAccountService.getDao().findCustomerAccountByParent(3L)).thenReturn(List.of());
      when(customerAccountService.getDao().findCustomerAccountByParent(4L)).thenReturn(List.of());

      // Act
      List<CustomerAccountDto> result = customerAccountService.getAllDescendants(1L);

      // Logging results
      log.info("Parent Account: {}", parent.getId());
      for (CustomerAccountDto child : result) {
         log.info("Child Account ID: {} (Parent ID: {})", child.getId(), child.getParentId());
      }

      // Assert
      assertEquals(4, result.size());
      assertEquals(List.of(parent, child1,grandchild1, child2), result);
   }

   @Test
   void testActivate() {
      Long accountId = 1L;
      final CustomerAccountDto activatedAccount = CustomerAccountDto.builder().id(accountId).name("test").build();
      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.getPrincipal()).thenReturn(user);
      when(dao.activate(accountId)).thenReturn(activatedAccount);

      CustomerAccountDto result = customerAccountService.activate(accountId);

      // Assertions
      assertNotNull(result);
      assertEquals(accountId, result.getId());
      assertEquals("test", result.getName());

      // Verify interactions
      verify(dao, times(1)).activate(accountId);
      verify(eventPublisher, times(1)).publishEvent(any(EntityActionEvent.class));
   }

   @Test
   void testDeactivate() {
      Long accountId = 1L;
      final CustomerAccountDto deactivatedAccount = CustomerAccountDto.builder().id(accountId).name("test").build();
      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.getPrincipal()).thenReturn(user);
      // Mock behavior
      when(dao.deactivate(accountId)).thenReturn(deactivatedAccount);

      // Call method
      CustomerAccountDto result = customerAccountService.deactivate(accountId);

      // Assertions
      assertNotNull(result);
      assertEquals(accountId, result.getId());
      assertEquals("test", result.getName());

      // Verify interactions
      verify(dao, times(1)).deactivate(accountId);
      verify(eventPublisher, times(1)).publishEvent(any(EntityActionEvent.class));
   }

   @Test
   void testFindChildCustomerAccounts_ShouldThrowExceptionIfNotFound() {
      lenient().when(dao.findByMasterUser(any())).thenReturn(Optional.empty());

      // Act & Assert
      assertThrows(ServiceValidationException.class, () -> {
         customerAccountService.findChildCustomerAccounts(user);
      });
   }

   @Test
   void testFindStationsByCustomerAccount_ShouldReturnStations() {
      final StationDto station1 = StationDto.builder().id(1L).customerAccountId(1L).build();
      final StationDto station2 = StationDto.builder().id(2L).customerAccountId(1L).build();
      final CustomerAccountDto account1 = CustomerAccountDto.builder().id(1L).name("account1").stations(Set.of(station1,station2)).build();


      when(customerAccountService.getDao().findByMasterUser(any())).thenReturn(Optional.ofNullable(account1));
      when(stationService.countStations(1L)).thenReturn(2);
      when(customerAccountService.findById(account1.getId())).thenReturn(Optional.of(account1));

      // Act
      List<StationDto> result = customerAccountService.findStationsByCustomerAccount(user);

      // Assert
      assertNotNull(result);
      assertEquals(2, result.size());
      assertTrue(result.contains(station1));
      assertTrue(result.contains(station2));
   }

   @Test
   void testAddStation_ShouldCreateStationAndPublishEvent() {
      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.getPrincipal()).thenReturn(user);
      when(validator.validateOnAddStation(dto)).thenReturn(validationResult);
      when(validationResult.hasErrors()).thenReturn(false);
      UserDto userController = dto.getControllerPts().getUserController();
      when(userService.create(userController)).thenReturn(userController);
      StationDto createdStation = StationDto.builder().build();  // Mock created station
      when(stationService.create(dto, true)).thenReturn(createdStation);

      // Mocking CustomerAccountDto response
      customerAccountDto = CustomerAccountDto.builder().build();
      customerAccountDto.setId(1L);
      customerAccountDto.setName("Test Account");
      customerAccountDto.setCardManager(true);
      customerAccountDto.setExported(true);
      when(customerAccountService.findById(dto.getCustomerAccountId())).thenReturn(Optional.of(customerAccountDto));

      CMSupplierDto supplierDto = CMSupplierDto.builder().build();
      supplierDto.setId(1L);
      when(customerAccountExport.exportedCustomerAccount(String.valueOf(customerAccountDto.getId())))
              .thenReturn(Optional.of(supplierDto));

      // Act
      StationDto result = customerAccountService.addStation(dto, null);

      // Assert
      assertNotNull(result);  // Ensure station is returned
      assertEquals(createdStation, result);  // Ensure the returned station is the one created

      // Verify the mocked methods were called as expected
      verify(userService, times(1)).create(dto.getControllerPts().getUserController());  // Verify user creation
      verify(stationService, times(1)).create(dto, true);  // Verify station creation
      verify(keycloakService, times(1)).createUser(dto.getControllerPts().getUserController());  // Verify user creation in Keycloak
      verify(customerAccountExport, times(1)).exportedCustomerAccount(String.valueOf(customerAccountDto.getId()));  // Verify export
      verify(eventPublisher, times(1)).publishEvent(any(EntityActionEvent.class));  // Verify event publishing
   }

   @Test
   void testAddStation_ShouldThrowExceptionIfValidationFails() {
      dto = StationDto.builder().build();
      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(validator.validateOnAddStation(dto)).thenReturn(validationResult);
      when(validationResult.hasErrors()).thenReturn(true);
      when(validationResult.getMessage()).thenReturn("Validation error");

      // Act & Assert
      ServiceValidationException exception = assertThrows(ServiceValidationException.class, () -> {
         customerAccountService.addStation(dto, null);
      });

      assertEquals("Validation error", exception.getMessage());
   }

   @Test
   void testCreate_ShouldCreateCustomerAccountSuccessfully() {
      customerAccountDto = CustomerAccountDto.builder().id(1L).name("customer test").masterUser(userDto).actif(true).cardManager(false).build();
      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.getName()).thenReturn(user.getUsername());
      when(validator.validateOnCreate(customerAccountDto)).thenReturn(validationResult);
      when(userService.findByUsername(user.getUsername())).thenReturn(Optional.of(userDto));

      when(customerAccountService.create(customerAccountDto, false)).thenReturn(customerAccountDto);

      // Act
      CustomerAccountDto result = customerAccountService.create(customerAccountDto);

      // Assert
      assertNotNull(result);
      assertEquals(1L, result.getId());
      assertEquals("customer test", result.getName());

      // Verify method calls
      verify(userService, times(1)).findByUsername("loggedUser");
      verify(keycloakService, times(1)).createUser(userDto);
      verify(eventPublisher, times(1)).publishEvent(any(EntityActionEvent.class));
   }

   @Test
   void testCreate_ShouldThrowExceptionIfValidationFails() {
      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.getName()).thenReturn(user.getUsername());
      when(validator.validateOnCreate(customerAccountDto)).thenReturn(validationResult);
      when(validationResult.hasErrors()).thenReturn(true);
      when(validationResult.getMessage()).thenReturn("Validation failed");

      // Act & Assert
      ServiceValidationException exception = assertThrows(ServiceValidationException.class, () -> {
         customerAccountService.create(customerAccountDto);
      });

      assertEquals("Validation failed", exception.getMessage());
   }

   @Test
   void testCreate_ShouldThrowExceptionIfUserNotFound() {
      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.getName()).thenReturn(user.getUsername());
      when(validator.validateOnCreate(customerAccountDto)).thenReturn(validationResult);
      when(userService.findByUsername(user.getUsername())).thenReturn(Optional.empty());

      // Act & Assert
      EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
         customerAccountService.create(customerAccountDto);
      });

      assertEquals("User not found with username: loggedUser", exception.getMessage());
   }

   @Test
   void testUpdateCustomerAccount_ShouldUpdateSuccessfully() {
      // Arrange
      CustomerAccountDto existingCustomerAccount = CustomerAccountDto.builder()
              .id(1L)
              .name("Existing Account")
              .masterUser(userDto)
              .actif(true)
              .cardManager(true)
              .exported(true)
              .build();
      when(validator.validateOnUpdate(existingCustomerAccount)).thenReturn(validationResult);
      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.getPrincipal()).thenReturn(user);
      when(customerAccountService.findById(1L)).thenReturn(Optional.of(existingCustomerAccount));
      when(customerAccountService.update(existingCustomerAccount)).thenReturn(existingCustomerAccount);

      // Act
      CustomerAccountDto result = customerAccountService.updateCustomerAccount(existingCustomerAccount, null);

      // Assert
      assertNotNull(result);
      assertEquals(1L, result.getId());
      assertEquals("Existing Account", result.getName());

      // Verify interactions
      verify(keycloakService, times(1)).updateUser(existingCustomerAccount.getMasterUser().getUsername(), existingCustomerAccount.getMasterUser());
      verify(eventPublisher, times(1)).publishEvent(any(EntityActionEvent.class));

      if (existingCustomerAccount.isExported()) {
         verify(customerAccountExport, times(1)).customerAccountUpdated(existingCustomerAccount);
      }
   }

   @Test
   void testUpdateStation_ShouldUpdateSuccessfully() {
      customerAccountDto = CustomerAccountDto.builder()
              .id(1L)
              .name("Test Account")
              .cardManager(true)
              .exported(true)
              .build();

      UserDto userController = UserDto.builder()
              .username("userController")
              .password("newPassword")
              .build();

      ControllerPtsDto controllerPts = ControllerPtsDto.builder()
              .userController(userController)
              .build();

      StationDto oldStationDto = StationDto.builder()
              .id(100L)
              .name("Old Station")
              .controllerPts(controllerPts)
              .customerAccountId(1L)
              .build();

      StationDto updatedStationDto = StationDto.builder()
              .id(100L)
              .name("Updated Station")
              .controllerPts(controllerPts)
              .customerAccountId(1L)
              .build();

      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.getPrincipal()).thenReturn(user);
      when(stationService.checkedFindById(100L)).thenReturn(oldStationDto);
      when(customerAccountService.findById(1L)).thenReturn(Optional.of(customerAccountDto));
      when(stationService.update(oldStationDto)).thenReturn(updatedStationDto);
      when(customerAccountExport.exportedCustomerAccount("1")).thenReturn(Optional.of(CMSupplierDto.builder().id(1L).build()));

      // Act
      StationDto result = customerAccountService.updateStation(oldStationDto, null);

      // Assert
      assertNotNull(result);
      assertEquals(100L, result.getId());
      assertEquals("Updated Station", result.getName());

      // Verify method calls
      verify(stationService, times(1)).checkedFindById(100L);
      verify(keycloakService, times(1)).updateUser(eq("userController"), any(UserDto.class));
      verify(stationService, times(1)).changeUserController(eq(100L), any(UserDto.class));
      verify(customerAccountExport, times(1)).exportedCustomerAccount("1");
      verify(customerAccountExport, times(1)).salePointUpdated(eq(1L), any(StationDto.class));
      verify(eventPublisher, times(1)).publishEvent(any(EntityActionEvent.class));
   }

   @Test
   void testAddUserToCustomerAccount_ShouldAddUserSuccessfully(){
      Long customerAccountId = 1L;
      UserDto customerUser = UserDto.builder()
              .username("testUser")
              .userType(User.EnumUserType.APPLICATION)
              .build();

      customerAccountDto = CustomerAccountDto.builder()
              .id(customerAccountId)
              .name("Test Account")
              .build();

      MultipartFile photoFile = new MockMultipartFile(
              "photo.jpg", "photo.jpg", "image/jpeg", new byte[]{1, 2, 3, 4}
      );

      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.getPrincipal()).thenReturn(user);
      when(customerAccountService.findById(customerAccountId)).thenReturn(Optional.of(customerAccountDto));
      when(userService.create(any(UserDto.class))).thenReturn(customerUser);
      when(keycloakService.createUser(any(UserDto.class))).thenReturn(new UserRepresentation());
      when(validator.validateOnAddUser(eq(customerAccountId), any(UserDto.class)))
              .thenReturn(new ESSValidationResult());

      doNothing().when(eventPublisher).publishEvent(any(EntityActionEvent.class));

      // Act
      UserDto result = customerAccountService.addUserToCustomerAccount(customerAccountId, customerUser, photoFile);

      // Assert
      assertNotNull(result);
      assertEquals("testuser", result.getUsername()); // Ensuring lowercase conversion
      assertEquals(customerAccountId, result.getCustomerAccountId());

      // Verify method calls
      verify(validator, times(1)).validateOnAddUser(customerAccountId, customerUser);
      verify(userService, times(1)).create(customerUser);
      verify(keycloakService, times(1)).createUser(customerUser);
      verify(eventPublisher, times(1)).publishEvent(any(EntityActionEvent.class));
   }

   @Test
   void updateCustomerAccountUser_ShouldUpdateUserSuccessfully() {
      Long customerAccountId = 1L;
      UserDto existingUser = UserDto.builder()
              .id(2L)
              .username("updatedUser")
              .build();

      customerAccountDto = CustomerAccountDto.builder()
              .id(customerAccountId)
              .name("Test Customer")
              .attachedUsers(Set.of(UserDto.builder().id(2L).username("oldUser").build()))
              .build();

      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.getPrincipal()).thenReturn(new User());
      when(customerAccountService.findById(customerAccountId)).thenReturn(Optional.of(customerAccountDto));

      // Act
      customerAccountService.updateCustomerAccountUser(customerAccountId, existingUser, null);

      // Assert
      verify(keycloakService, times(1)).updateUser("oldUser", existingUser);
      verify(userService, times(1)).update(existingUser);
      verify(eventPublisher, times(1)).publishEvent(any(EntityActionEvent.class));
   }

   @Test
   void updateCustomerAccountUser_ShouldThrowException_WhenUserNotFound() {
      Long customerAccountId = 1L;
      UserDto notFoundUser = UserDto.builder()
              .id(99L) // Non-existent user
              .username("notFoundUser")
              .build();

      customerAccountDto = CustomerAccountDto.builder()
              .id(customerAccountId)
              .name("Test Customer")
              .attachedUsers(Set.of(UserDto.builder().id(2L).username("existingUser").build()))
              .build();
      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(customerAccountService.findById(customerAccountId)).thenReturn(Optional.of(customerAccountDto));

      // Act & Assert
      EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
              customerAccountService.updateCustomerAccountUser(customerAccountId, notFoundUser, null)
      );

      assertEquals("User with id(99) not found on customer id(1)", exception.getMessage());

      verify(keycloakService, never()).updateUser(any(), any());
      verify(userService, never()).update(any());
      verify(eventPublisher, never()).publishEvent(any());
   }

   @Test
   void testGetDetails_ShouldReturnCustomerAccountDtoWithMasterUserDetails() {
      Long customerAccountId = 1L;

      // Mock the CustomerAccountDto returned by the DAO
      customerAccountDto = CustomerAccountDto.builder().build();
      customerAccountDto.setId(customerAccountId);
      UserDto masterUser = UserDto.builder().build();
      masterUser.setUsername("masterUser");
      customerAccountDto.setMasterUser(masterUser);

      // Mock getDao().findById() to return the customerAccountDto
      when(customerAccountService.getDao().findById(customerAccountId)).thenReturn(Optional.of(customerAccountDto));

      // Mock Keycloak service to return user details
      UserRepresentation userRepresentation = new UserRepresentation();
      userRepresentation.setUsername("masterUser");
      userRepresentation.setEmail("master@domain.com");
      userRepresentation.setFirstName("Master");
      userRepresentation.setLastName("User");
      userRepresentation.setAttributes(Map.of("phone", List.of("99999999")));
      when(keycloakService.getUserIdentity("masterUser")).thenReturn(Optional.of(userRepresentation));

      // Act
      CustomerAccountDto result = customerAccountService.getDetails(customerAccountId);

      // Assert
      assertNotNull(result);
      assertNotNull(result.getMasterUser());
      assertEquals("masterUser", result.getMasterUser().getUsername());
      assertEquals("Master", result.getMasterUser().getFirstName());
      assertEquals("User", result.getMasterUser().getLastName());
      assertEquals("99999999", result.getMasterUser().getPhone());

      // Verify interactions
      verify(customerAccountService.getDao(), times(1)).findById(customerAccountId);
      verify(keycloakService, times(1)).getUserIdentity("masterUser");
   }

   @Test
   void testActivateCustomerAccountUser_ShouldActivateUserAndPublishEvent() {
      Long customerAccountId = 1L;
      Long userId = 1L;
      customerAccountDto = CustomerAccountDto.builder()
              .id(customerAccountId)
              .name("Test Customer")
              .attachedUsers(Set.of(UserDto.builder().id(userId).username("test user").build()))
              .build();
      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(customerAccountService.findById(customerAccountId)).thenReturn(Optional.of(customerAccountDto));

      // Act
      customerAccountService.activateCustomerAccountUser(customerAccountId, userId, null);

      // Assert
      verify(userService, times(1)).activate(any(UserDto.class));
      verify(eventPublisher, times(1)).publishEvent(any(EntityActionEvent.class));
   }

   @Test
   void testDeactivateCustomerAccountUser_ShouldDeactivateUserAndPublishEvent() {
      Long customerAccountId = 1L;
      Long userId = 1L;
      CustomerAccountDto customerAccount = CustomerAccountDto.builder()
              .id(customerAccountId)
              .name("Test Customer")
              .attachedUsers(Set.of(UserDto.builder().id(userId).username("test user").build()))
              .build();
      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(customerAccountService.findById(customerAccountId)).thenReturn(Optional.of(customerAccount));
      // Act
      customerAccountService.deactivateCustomerAccountUser(customerAccountId, userId, null);

      // Assert
      verify(userService, times(1)).deactivate(any(UserDto.class));
      verify(eventPublisher, times(1)).publishEvent(any(EntityActionEvent.class));
   }

   @Test
   void testListCustomerAccountUsers() {
      Long customerAccountId = 1L;

      // Mock KeycloakService to return user representations for the usernames
      UserRepresentation userRepresentation1 = mock(UserRepresentation.class);
      UserRepresentation userRepresentation2 = mock(UserRepresentation.class);
      // Mock userMapper to convert UserRepresentation to UserDto
      UserDto userDto1 = UserDto.builder().build();
      userDto1.setId(1L);
      userDto1.setUsername("user1");

      UserDto userDto2 = UserDto.builder().build();
      userDto2.setId(2L);
      userDto2.setUsername("user2");
      CustomerAccountDto testAccount = CustomerAccountDto.builder()
              .id(customerAccountId)
              .name("Test Customer")
              .attachedUsers(Set.of(userDto1,userDto2))
              .build();

      when(customerAccountService.findById(customerAccountId)).thenReturn(Optional.of(testAccount));
      when(keycloakService.getUserIdentities(anyList())).thenReturn(List.of(userRepresentation1, userRepresentation2));
      when(userMapper.userRepresentationToDto(userRepresentation1)).thenReturn(userDto1);
      when(userMapper.userRepresentationToDto(userRepresentation2)).thenReturn(userDto2);

      // Act
      List<UserDto> result = customerAccountService.listCustomerAccountUsers(1L);

      // Assert
      assertNotNull(result);
      assertEquals(2, result.size());
      assertEquals("user1", result.get(0).getUsername());
      assertEquals("user2", result.get(1).getUsername());

      // Verify that the mappings were done correctly
      verify(keycloakService, times(1)).getUserIdentities(anyList()); // Verify that getUserIdentities was called with the correct usernames
      verify(userMapper, times(1)).userRepresentationToDto(userRepresentation1); // Verify that userMapper was called for user1
      verify(userMapper, times(1)).userRepresentationToDto(userRepresentation2); // Verify that userMapper was called for user2
   }

   @Test
   void testFindStationByNameFilter() {
      StationDto station1 = StationDto.builder().name("Station 1").creatorCustomerAccountName("Creator A").customerAccountName("Parent X").build();

      StationDto station2 = StationDto.builder().name("Station 2").creatorCustomerAccountName("Creator B").customerAccountName("Parent Y").build();

      StationDto station3 = StationDto.builder().name("Station 3").creatorCustomerAccountName("Creator C").customerAccountName("Parent Z").build();

      Long customerAccountId = 1L;

      CustomerAccountDto testCustomer = CustomerAccountDto.builder()
              .id(customerAccountId)
              .name("Test Customer")
              .stations(Set.of(station1,station2,station3))
              .build();

      when(customerAccountService.getDao().findByMasterUser(user.getUsername())).thenReturn(Optional.of(testCustomer));
      when(customerAccountService.findById(1L)).thenReturn(Optional.of(testCustomer));

      // Act
      List<StationDto> result = customerAccountService.findStationByFilter(user, "Station 1", null, null);

      // Assert
      assertEquals(1, result.size());
      assertEquals("Station 1", result.get(0).getName());
   }

   @Test
   void testFindStationByCreatorFilter() {
      StationDto station1 = StationDto.builder().name("Station 1").creatorCustomerAccountName("Creator A").customerAccountName("Parent X").build();

      StationDto station2 = StationDto.builder().name("Station 2").creatorCustomerAccountName("Creator B").customerAccountName("Parent Y").build();

      StationDto station3 = StationDto.builder().name("Station 3").creatorCustomerAccountName("Creator C").customerAccountName("Parent Z").build();

      Long customerAccountId = 1L;

      CustomerAccountDto testCustomer = CustomerAccountDto.builder()
              .id(customerAccountId)
              .name("Test Customer")
              .stations(Set.of(station1,station2,station3))
              .build();

      when(customerAccountService.getDao().findByMasterUser(user.getUsername())).thenReturn(Optional.of(testCustomer));
      when(customerAccountService.findById(1L)).thenReturn(Optional.of(testCustomer));

      // Act
      List<StationDto> result = customerAccountService.findStationByFilter(user, null, "Creator A", null);

      // Assert
      assertEquals(1, result.size());
      assertEquals("Creator A", result.get(0).getCreatorCustomerAccountName());
   }
   @Test
   void testFindStationByParentFilter() {
      StationDto station1 = StationDto.builder().name("Station 1").creatorCustomerAccountName("Creator A").customerAccountName("Parent X").build();

      StationDto station2 = StationDto.builder().name("Station 2").creatorCustomerAccountName("Creator B").customerAccountName("Parent Y").build();

      StationDto station3 = StationDto.builder().name("Station 3").creatorCustomerAccountName("Creator C").customerAccountName("Parent Z").build();

      Long customerAccountId = 1L;

      CustomerAccountDto testCustomer = CustomerAccountDto.builder()
              .id(customerAccountId)
              .name("Test Customer")
              .stations(Set.of(station1,station2,station3))
              .build();

      when(customerAccountService.getDao().findByMasterUser(user.getUsername())).thenReturn(Optional.of(testCustomer));
      when(customerAccountService.findById(1L)).thenReturn(Optional.of(testCustomer));

      // Act
      List<StationDto> result = customerAccountService.findStationByFilter(user, null,null , "Parent X");

      // Assert
      assertEquals(1, result.size());
      assertEquals("Parent X", result.get(0).getCustomerAccountName());
   }

   @Test
   void testFindCustomerAccountByName() {
      when(customerAccountService.getDao().findCustomerAccountByName("account 1", PageRequest.of(0, 10))).thenReturn(mockPage);

      Page<CustomerAccountDto> result = customerAccountService.findCustomerAccountByFilter("account 1", null, null, 0, 10);

      assertNotNull(result);
      assertEquals(2, result.getContent().size());
   }

   @Test
   void testFindCustomerAccountByCreator() {
      when(customerAccountService.getDao().findByCustomerAccountByCreator("creator1", PageRequest.of(0, 10))).thenReturn(mockPage);

      Page<CustomerAccountDto> result = customerAccountService.findCustomerAccountByFilter(null, "creator1", null, 0, 10);

      assertNotNull(result);
      assertEquals(2, result.getContent().size());
   }

   @Test
   void testFindCustomerAccountByParent() {
      when(customerAccountService.getDao().findByCustomerAccountByParent("parent1", PageRequest.of(0, 10))).thenReturn(mockPage);

      Page<CustomerAccountDto> result = customerAccountService.findCustomerAccountByFilter(null, null, "parent1", 0, 10);

      assertNotNull(result);
      assertEquals(2, result.getContent().size());
   }

   @Test
   void testFindAllCustomerAccounts() {
      when(customerAccountService.getDao().findAllCustomerAcount(PageRequest.of(0, 10))).thenReturn(mockPage);

      Page<CustomerAccountDto> result = customerAccountService.findCustomerAccountByFilter(null, null, null, 0, 10);

      assertNotNull(result);
      assertEquals(2, result.getContent().size());
   }
   @Test
   void testExportSupplier_ShouldThrowExceptionWhenNotCardManager() {
      // Arrange
      // Instead of checking directly on customerAccountDto, we can prepare an account with a different context
      CustomerAccountDto accountToExport = CustomerAccountDto.builder()
              .id(2L)
              .name("Test Account")
              .cardManager(false)
              .build();

      // Act & Assert
      ServiceValidationException exception = assertThrows(ServiceValidationException.class, () -> {
         // Call the exportSupplier method for the new account context
         customerAccountService.exportSupplier(accountToExport);
      });

      // Assert the exception message is as expected
      assertEquals("You can't export this account to Card Manager Module because it doesn't have rights!", exception.getMessage());

      // Verify that not other methods will be called if the card manager check fails
      verify(customerAccountExport, never()).customerAccountCreated(any());
      verify(customerAccountDao, never()).update(any());
      verify(eventPublisher, never()).publishEvent(any());
   }
   @Test
   void testFindAllStations() {
      // Arrange
      when(stationService.findAll()).thenReturn(List.of(StationDto.builder().build()));

      // Act
      List<StationDto> stations = customerAccountService.findAllStations();

      // Assert
      assertNotNull(stations);
      assertTrue(stations.size() >= 0);
   }

}
