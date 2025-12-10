package com.homi.saas.web.controller;

import com.homi.saas.service.service.pdf.PdfService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 应用于 homi-boot
 *
 * @author 金华云 E-mail:jinhuayun001@ke.com
 * @version v1.0
 * {@code @date} 2025/4/26
 */

@Slf4j
@RequestMapping("/saas/test")
@RestController
@RequiredArgsConstructor
public class TestController {
    private final PdfService pdfService;

    @PostMapping(value = "/generate", consumes = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<byte[]> generate(@RequestBody String html) {
        byte[] pdfBytes = pdfService.generatePdf(html);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment().filename("contract.pdf").build());
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

}
