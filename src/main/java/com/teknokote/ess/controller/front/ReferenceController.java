package com.teknokote.ess.controller.front;

import com.teknokote.ess.controller.EndPoints;
import com.teknokote.ess.core.service.impl.CountryService;
import com.teknokote.ess.dto.CountryDto;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@CrossOrigin("*")
@RequestMapping(EndPoints.REFERENCE_DATA_ROOT)
public class ReferenceController {

    @Autowired
    private CountryService countryService;

    @GetMapping(EndPoints.COUNTRIES)
    public List<CountryDto> listCountries() {
        List<CountryDto> countries = countryService.findAll();

        // Sort countries by name in alphabetical order
        countries.sort((c1, c2) -> c1.getName().compareToIgnoreCase(c2.getName()));

        return countries;
    }
}
