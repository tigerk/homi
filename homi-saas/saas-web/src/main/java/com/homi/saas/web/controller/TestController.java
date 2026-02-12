package com.homi.saas.web.controller;

import com.homi.common.lib.utils.ConvertHtml2PdfUtils;
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
 * @author tigerk
 * @version v1.0
 * {@code @date} 2025/4/26
 */

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/saas/test")
public class TestController {

    @PostMapping(value = "/generate", consumes = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<byte[]> generate(@RequestBody String html) {
        byte[] pdfBytes = ConvertHtml2PdfUtils.generatePdf(html);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment().filename("contract.pdf").build());
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

}
