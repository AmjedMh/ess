package com.teknokote.ess.controller.front.shifts;

import com.teknokote.ess.controller.EndPoints;
import com.teknokote.ess.core.service.shifts.PaymentMethodService;
import com.teknokote.ess.dto.shifts.PaymentMethodDto;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@CrossOrigin("*")
@AllArgsConstructor
@RequestMapping(EndPoints.PAYMENT_METHOD_ROOT)
public class PaymentMethodController {
    @Autowired
    private PaymentMethodService paymentMethodService;


    @PostMapping(EndPoints.ADD)
    public ResponseEntity<PaymentMethodDto> addPaymentMethod(@RequestBody PaymentMethodDto dto) {
        PaymentMethodDto savedPaymentMethod = paymentMethodService.create(dto);
        return new ResponseEntity<>(savedPaymentMethod, HttpStatus.CREATED);
    }

    @PutMapping(EndPoints.UPDATE)
    public ResponseEntity<PaymentMethodDto> updatePaymentMethod(@RequestBody PaymentMethodDto dto) {
        PaymentMethodDto savedPaymentMethod = paymentMethodService.update(dto);
        return new ResponseEntity<>(savedPaymentMethod, HttpStatus.CREATED);
    }

    @GetMapping(EndPoints.INFO)
    public ResponseEntity<PaymentMethodDto> getPaymentMethod(@PathVariable Long id) {
        PaymentMethodDto foundPaymentMethod = paymentMethodService.checkedFindById(id);
        return new ResponseEntity<>(foundPaymentMethod, HttpStatus.CREATED);
    }

    @PreAuthorize("isUserAttachedToParentOrCustomerAccountOf(#customerAccountId)")
    @GetMapping
    public List<PaymentMethodDto> listPaymentMethodByStation(@PathVariable Long customerAccountId,
                                                             @RequestParam Long stationId) {
        return paymentMethodService.findByStationId(stationId);
    }
}
