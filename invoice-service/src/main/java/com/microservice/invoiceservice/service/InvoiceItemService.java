package com.microservice.invoiceservice.service;

import com.microservice.invoiceservice.dto.InvoiceItemRequest;
import com.microservice.invoiceservice.dto.InvoiceItemResponse;
import com.microservice.invoiceservice.entity.Invoice;
import com.microservice.invoiceservice.entity.InvoiceItem;
import com.microservice.invoiceservice.repository.InvoiceItemRepository;
import com.microservice.invoiceservice.repository.InvoiceRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceItemService {
    private final InvoiceItemRepository invoiceItemRepository;
    private final InvoiceRepository invoiceRepository;

    public List<InvoiceItemResponse> getInvoiceItemsByInvoiceId(Long invoiceId) {
        log.info("Fetching invoice items for invoice ID: {}", invoiceId);
        try {
            List<InvoiceItem> items = invoiceItemRepository.findByInvoice_Id(invoiceId);
            return items.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching invoice items for invoice ID {}: {}", invoiceId, e.getMessage());
            throw new RuntimeException("Failed to fetch invoice items: " + e.getMessage());
        }
    }

    public List<InvoiceItemResponse> getInvoiceItemsByProductId(Long productId) {
        log.info("Fetching invoice items for product ID: {}", productId);
        try {
            List<InvoiceItem> items = invoiceItemRepository.findByProductId(productId);
            return items.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching invoice items for product ID {}: {}", productId, e.getMessage());
            throw new RuntimeException("Failed to fetch invoice items: " + e.getMessage());
        }
    }

    @Transactional
    public InvoiceItemResponse updateInvoiceItem(Long id, InvoiceItemRequest request) {
        log.info("Updating invoice item ID: {} with request: {}", id, request);
        try {
            InvoiceItem item = invoiceItemRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Invoice item not found with id: " + id));

            // Validate request
            validateInvoiceItemRequest(request);

            // Update item
            item.setQuantity(request.getQuantity());
            item.setPrice(request.getPrice());
            item.setProductId(request.getProductId());
            
            // Calculate subtotal
            BigDecimal subtotal = request.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));
            item.setSubtotal(subtotal);
            
            InvoiceItem updatedItem = invoiceItemRepository.save(item);
            log.info("Successfully updated invoice item ID: {}", id);
            
            // Update invoice total
            updateInvoiceTotal(updatedItem.getInvoice().getId());
            
            return convertToResponse(updatedItem);
        } catch (EntityNotFoundException e) {
            log.error("Invoice item not found with ID {}: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error updating invoice item ID {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to update invoice item: " + e.getMessage());
        }
    }

    @Transactional
    public void deleteInvoiceItem(Long id) {
        log.info("Deleting invoice item ID: {}", id);
        try {
            InvoiceItem item = invoiceItemRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Invoice item not found with id: " + id));
            
            Long invoiceId = item.getInvoice().getId();
            invoiceItemRepository.deleteById(id);
            log.info("Successfully deleted invoice item ID: {}", id);
            
            // Update invoice total
            updateInvoiceTotal(invoiceId);
        } catch (EntityNotFoundException e) {
            log.error("Invoice item not found with ID {}: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error deleting invoice item ID {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to delete invoice item: " + e.getMessage());
        }
    }
    
    private void updateInvoiceTotal(Long invoiceId) {
        log.info("Updating total amount for invoice ID: {}", invoiceId);
        try {
            Invoice invoice = invoiceRepository.findById(invoiceId)
                    .orElseThrow(() -> new EntityNotFoundException("Invoice not found with id: " + invoiceId));
            
            List<InvoiceItem> items = invoiceItemRepository.findByInvoice_Id(invoiceId);
            BigDecimal totalAmount = items.stream()
                    .map(InvoiceItem::getSubtotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
            invoice.setTotalAmount(totalAmount);
            invoiceRepository.save(invoice);
            log.info("Successfully updated total amount for invoice ID: {}", invoiceId);
        } catch (Exception e) {
            log.error("Error updating total amount for invoice ID {}: {}", invoiceId, e.getMessage());
            throw new RuntimeException("Failed to update invoice total: " + e.getMessage());
        }
    }

    private InvoiceItemResponse convertToResponse(InvoiceItem item) {
        return new InvoiceItemResponse(
                item.getId(),
                item.getPrice(),
                item.getQuantity(),
                item.getSubtotal(),
                item.getProductId(),
                item.getInvoice().getId()
        );
    }

    private void validateInvoiceItemRequest(InvoiceItemRequest request) {
        if (request.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        if (request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than 0");
        }
        if (request.getProductId() == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
    }
} 