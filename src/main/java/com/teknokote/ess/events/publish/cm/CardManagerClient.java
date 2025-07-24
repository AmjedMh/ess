package com.teknokote.ess.events.publish.cm;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@FeignClient(name = "Card-manager-client", url = "${cm.client.url}",configuration = CMFeignClientConfiguration.class)
public interface CardManagerClient
{
   @GetMapping("/supplier/true")
   List<CMSupplierDto> listSupplier();
   @PostMapping("/supplier/add")
   CMSupplierDto createSupplier(CMSupplierDto cmSupplierDto);
   @GetMapping("/supplier/reference/{reference}")
   Optional<CMSupplierDto> getSupplier(@PathVariable String reference);
   @PutMapping("/supplier/update")
   CMSupplierDto updateSupplier(CMSupplierDto cmSupplierDto);
   @PostMapping("/supplier/{supplierId}/salePoint/add")
   CMSalePointDto createSalePoint(@PathVariable Long supplierId,@RequestBody CMSalePointDto cmSalePointDto);
   @PostMapping("/supplier/{supplierId}/user/add")
   CMUser createUser(@PathVariable Long supplierId,@RequestBody CMUser cmUser);
   @PostMapping("/product/add")
   CMProduct createProduct(CMProduct cmProduct);
   @PutMapping("/supplier/{supplierId}/salePoint/update")
   CMSalePointDto updateSalePoint(@PathVariable Long supplierId,@RequestBody CMSalePointDto salePointDto);
   @PostMapping("/authorization/authorize")
   AuthorizationDto authorize(AuthorizationRequest authorizationRequest);
   @PutMapping("/supplier/{supplierId}/user/update")
   CMUser updateCMUser(@PathVariable Long supplierId,@RequestBody CMUser cmUser);
   @PostMapping("/transaction/add")
    CMTransactionDto saveTransaction(CMTransactionDto cmTransactionDto);
   @GetMapping("/authorization/find")
   AuthorizationDto getAuthorization(@RequestParam String ptsId,@RequestParam Long pump, @RequestParam  String tag);
   @PostMapping("/authorization/card/{cardId}/update")
    void updateCardStatus(@PathVariable Long cardId,@RequestParam Long authorizationId,@RequestParam Long transactionId,@RequestParam EnumCardStatus status);
}
