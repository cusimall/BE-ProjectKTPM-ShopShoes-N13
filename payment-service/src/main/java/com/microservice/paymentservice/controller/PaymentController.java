package com.microservice.paymentservice.controller;

import com.microservice.paymentservice.config.VNPayConfig;
import com.microservice.paymentservice.dto.PaymentRequest;
import com.microservice.paymentservice.dto.PaymentResponse;
import com.microservice.paymentservice.dto.PaymentTransaction;
import com.microservice.paymentservice.dto.TransactionStatusRequest;
import com.microservice.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.HttpHeaders;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final VNPayConfig vnPayConfig;
    private final WebClient.Builder webClientBuilder;
    private final PaymentService paymentService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PaymentResponse> createPayment(
            @Valid @RequestBody PaymentRequest paymentRequest,
            HttpServletRequest request) throws UnsupportedEncodingException {

        log.info("Creating payment for invoice: {}", paymentRequest.getInvoiceId());

        // Convert amount to VNPay format (x100, no decimal)
        long amount = Math.round(paymentRequest.getAmount().doubleValue() * 100);
        String vnpTxnRef = vnPayConfig.getRandomNumber(8);

        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", vnPayConfig.getVersion());
        vnpParams.put("vnp_Command", vnPayConfig.getCOMMAND());
        vnpParams.put("vnp_TmnCode", vnPayConfig.getTmnCode());
        vnpParams.put("vnp_Amount", String.valueOf(amount));
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_BankCode", paymentRequest.getBankCode() != null ? paymentRequest.getBankCode() : "NCB");
        vnpParams.put("vnp_TxnRef", vnpTxnRef);
        vnpParams.put("vnp_OrderInfo", paymentRequest.getDescription() != null ? 
                paymentRequest.getDescription() : 
                "Thanh toan don hang:" + paymentRequest.getInvoiceId());
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Locale", paymentRequest.getLanguage() != null ? paymentRequest.getLanguage() : "vn");

        // Add extra data to track the invoice
        String returnUrl = paymentRequest.getReturnUrl() != null ? 
                paymentRequest.getReturnUrl() : 
                vnPayConfig.getReturnUrl();
        vnpParams.put("vnp_ReturnUrl", returnUrl + "?invoiceId=" + paymentRequest.getInvoiceId());

        // Set time
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnpCreateDate = formatter.format(cld.getTime());
        vnpParams.put("vnp_CreateDate", vnpCreateDate);

        // Set expire time
        cld.add(Calendar.MINUTE, 15);
        String vnpExpireDate = formatter.format(cld.getTime());
        vnpParams.put("vnp_ExpireDate", vnpExpireDate);

        // Build hash data and query string
        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();

        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnpParams.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                // Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));

                // Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));

                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }

        String queryUrl = query.toString();
        String vnpSecureHash = vnPayConfig.hmacSHA512(vnPayConfig.getHashSecret(), hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnpSecureHash;
        String paymentUrl = vnPayConfig.getPayUrl() + "?" + queryUrl;

        // Store transaction info for later validation
        PaymentTransaction transaction = paymentService.storeTransaction(paymentRequest.getInvoiceId(), vnpTxnRef);

        // Build response
        PaymentResponse response = PaymentResponse.builder()
                .status("OK")
                .message("Successfully created payment URL")
                .paymentUrl(paymentUrl)
                .transactionId(vnpTxnRef)
                .invoiceId(paymentRequest.getInvoiceId())
                .amount(paymentRequest.getAmount())
                .transactionDate(LocalDateTime.now())
                .bankCode(paymentRequest.getBankCode())
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/callback")
    public ResponseEntity<PaymentResponse> paymentCallback(
            @RequestParam Map<String, String> params) {

        log.info("Received payment callback with params: {}", params);

        // Extract response code
        String vnpResponseCode = params.get("vnp_ResponseCode");
        String vnpTransactionStatus = params.get("vnp_TransactionStatus");
        String vnpTxnRef = params.get("vnp_TxnRef");
        String vnpAmount = params.get("vnp_Amount");
        Long invoiceId = Long.valueOf(params.get("invoiceId"));

        // Verify the transaction
        boolean isValid = paymentService.validateTransaction(params);

        if (!isValid) {
            log.error("Invalid transaction signature for invoice {}", invoiceId);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(PaymentResponse.builder()
                            .status("ERROR")
                            .message("Invalid transaction signature")
                            .invoiceId(invoiceId)
                            .transactionId(vnpTxnRef)
                            .errorCode("INVALID_SIGNATURE")
                            .build());
        }

        // Check if payment successful
        boolean isSuccessful = "00".equals(vnpResponseCode) && "00".equals(vnpTransactionStatus);

        // Update invoice status
        PaymentTransaction transaction = paymentService.updateInvoiceStatus(invoiceId, isSuccessful);

        // Build response
        PaymentResponse response = PaymentResponse.builder()
                .status(isSuccessful ? "SUCCESS" : "FAILED")
                .message(isSuccessful ? "Payment processed successfully" : "Payment failed")
                .invoiceId(invoiceId)
                .transactionId(vnpTxnRef)
                .transactionDate(transaction != null ? transaction.getCompletedAt() : LocalDateTime.now())
                .errorCode(isSuccessful ? null : vnpResponseCode)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{invoiceId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<PaymentResponse> getPaymentStatus(@PathVariable Long invoiceId) {
        PaymentTransaction transaction = paymentService.getPaymentStatus(invoiceId);
        
        PaymentResponse response = PaymentResponse.builder()
                .status(transaction.getStatus())
                .message("Payment status retrieved successfully")
                .invoiceId(transaction.getInvoiceId())
                .transactionId(transaction.getProviderTransactionId())
                .amount(transaction.getAmount())
                .transactionDate(transaction.getUpdatedAt())
                .bankCode(transaction.getBankCode())
                .build();
                
        return ResponseEntity.ok(response);
    }
}