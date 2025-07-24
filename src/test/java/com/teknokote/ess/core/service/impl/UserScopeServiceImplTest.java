package com.teknokote.ess.core.service.impl;

import com.teknokote.ess.core.dao.UserScopeDao;
import com.teknokote.ess.core.model.organization.EnumFunctionalScope;
import com.teknokote.ess.dto.UserDto;
import com.teknokote.ess.dto.UserScopeDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserScopeServiceImplTest {

    @InjectMocks
    private UserScopeServiceImpl userScopeService;

    @Mock
    private UserScopeDao userScopeDao;

    private UserScopeDto sampleUserScopeDto;

    @BeforeEach
    void setUp() {
        sampleUserScopeDto = UserScopeDto.builder()
                .id(1L)
                .relatedUser(UserDto.of("Sample User Scope"))
                .build();
    }

    @Test
    void findByFunctionalScopeAndUser_ShouldReturnUserScopes_WhenValidScopeAndUserIdProvided() {
        // Given
        EnumFunctionalScope functionalScope = EnumFunctionalScope.GLOBAL;
        Long userId = 1L;
        when(userScopeDao.findByFunctionalScopeAndUser(functionalScope, userId))
                .thenReturn(Arrays.asList(sampleUserScopeDto));

        // When
        List<UserScopeDto> result = userScopeService.findByFunctionalScopeAndUser(functionalScope, userId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(sampleUserScopeDto.getId(), result.get(0).getId());
        assertEquals(sampleUserScopeDto.getRelatedUser(), result.get(0).getRelatedUser());
    }

    @Test
    void findByFunctionalScopeAndUser_ShouldReturnEmptyList_WhenNoScopesFound() {
        // Given
        EnumFunctionalScope functionalScope = EnumFunctionalScope.GLOBAL;
        Long userId = 1L;
        when(userScopeDao.findByFunctionalScopeAndUser(functionalScope, userId))
                .thenReturn(Arrays.asList());

        // When
        List<UserScopeDto> result = userScopeService.findByFunctionalScopeAndUser(functionalScope, userId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findByFunctionalScopeAndUser_ShouldThrowException_WhenDaoCallFails() {
        // Given
        EnumFunctionalScope functionalScope = EnumFunctionalScope.GLOBAL;
        Long userId = 1L;
        when(userScopeDao.findByFunctionalScopeAndUser(functionalScope, userId))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> userScopeService.findByFunctionalScopeAndUser(functionalScope, userId));
    }
}