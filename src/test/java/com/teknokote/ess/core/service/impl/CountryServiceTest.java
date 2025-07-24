package com.teknokote.ess.core.service.impl;

import com.teknokote.ess.core.dao.CountryDao;
import com.teknokote.ess.core.service.impl.validators.CountryValidator;
import com.teknokote.ess.dto.CountryDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class CountryServiceTest {

    @InjectMocks
    private CountryService countryService;

    @Mock
    private CountryDao countryDao;

    @Mock
    private CountryValidator countryValidator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testPrepareListCache() {
        // Arrange
        CountryDto countryA = CountryDto.builder().id(1L).name("Australia").build();
        CountryDto countryB = CountryDto.builder().id(2L).name("Germany").build();
        CountryDto countryC = CountryDto.builder().id(3L).name("Brazil").build();

        List<CountryDto> countryList = Arrays.asList(countryB, countryA, countryC);
        when(countryDao.findAll()).thenReturn(countryList);

        // Act
        countryService.prepareListCache();

        // Assert
        List<CountryDto> cachedCountries = countryService.findAll();
        assertNotNull(cachedCountries);
        assertEquals(3, cachedCountries.size());
        assertEquals("Australia", cachedCountries.get(0).getName()); // Australia should be first in sorted order
        assertEquals("Brazil", cachedCountries.get(1).getName());
        assertEquals("Germany", cachedCountries.get(2).getName());
    }

    @Test
    void testFindAllReturnsCachedCountries() {
        // Arrange
        CountryDto countryA = CountryDto.builder().id(1L).name("Canada").build();

        List<CountryDto> countryList = Arrays.asList(countryA);
        when(countryDao.findAll()).thenReturn(countryList);

        // Act
        countryService.prepareListCache(); // Populate the cache

        // Act
        List<CountryDto> countries = countryService.findAll();

        // Assert
        assertNotNull(countries);
        assertFalse(countries.isEmpty());
        assertEquals(1, countries.size());
        assertEquals("Canada", countries.get(0).getName());
    }

    @Test
    void testFindAllWhenCacheIsEmpty() {
        // Arrange
        when(countryDao.findAll()).thenReturn(new ArrayList<>());

        // Act
        countryService.prepareListCache();

        // Assert
        List<CountryDto> countries = countryService.findAll();
        assertNotNull(countries);
        assertTrue(countries.isEmpty()); // Should be empty
    }

    @Test
    void testPrepareListCacheWhenDaoThrowsException() {
        // Arrange
        when(countryDao.findAll()).thenThrow(new RuntimeException("Database Error"));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            countryService.prepareListCache();
        });

        assertEquals("Database Error", exception.getMessage());
    }
}