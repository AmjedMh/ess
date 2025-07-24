package com.teknokote.ess.controller.front.fails;

import com.teknokote.ess.controller.EndPoints;
import com.teknokote.ess.controller.upload.pts.processing.UploadProcessorSwitcher;
import com.teknokote.ess.dto.SearchTransactionDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
@RequestMapping(EndPoints.FAILED_TRANSACTIONS)
public class FailedUploadsController
{
   @Autowired
   private UploadProcessorSwitcher uploadProcessorSwitcher;

   @PostMapping(EndPoints.PROCESS)
   public void processTransactions(@RequestBody SearchTransactionDto searchTransactionDto) {
      uploadProcessorSwitcher.processUploads(searchTransactionDto);
   }

}
