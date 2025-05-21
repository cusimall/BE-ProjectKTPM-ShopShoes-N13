package com.microservice.invoiceservice.service;

import com.microservice.invoiceservice.dto.InvoiceRequest;
import com.microservice.invoiceservice.dto.InvoiceItemRequest;
import com.microservice.invoiceservice.entity.Invoice;
import com.microservice.invoiceservice.entity.InvoiceItem;
import com.microservice.invoiceservice.repository.InvoiceRepository;
import com.microservice.invoiceservice.repository.InvoiceItemRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InvoiceService {
    private static final Logger log = LoggerFactory.getLogger(InvoiceService.class);
    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final EventPublisherService eventPublisherService;

    @Transactional
    public Invoice createInvoice(InvoiceRequest request) {
        log.info("Creating invoice for user: {} with address: {}", 
                request.getUserId(), request.getShipAddress());
        
        // Create new invoice
        Invoice invoice = new Invoice();
        invoice.setUserId(request.getUserId());
        invoice.setShipAddress(request.getShipAddress());
        invoice.setStatus(request.getStatus() != null ? request.getStatus() : "PENDING");
        invoice.setOrderDate(LocalDateTime.now());
        
        // Set total amount if provided
        if (request.getTotalAmount() != null) {
            invoice.setTotalAmount(request.getTotalAmount());
        }
        
        // Save invoice first to get ID
        invoice = invoiceRepository.save(invoice);
        log.info("Saved invoice - ID: {}, shipAddress: '{}'", invoice.getId(), invoice.getShipAddress());
        
        // Initialize invoice items list
        List<InvoiceItem> items = new ArrayList<>();
        
        // Create and set up invoice items if provided
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (Map<String, Object> itemData : request.getItems()) {
                try {
                    InvoiceItem item = new InvoiceItem();
                    item.setInvoice(invoice);
                    
                    // Extract item data
                    item.setProductId(Long.valueOf(itemData.get("productId").toString()));
                    item.setQuantity(Integer.valueOf(itemData.get("quantity").toString()));
                    
                    // Get price from item
                    if (itemData.containsKey("price")) {
                        String priceStr = itemData.get("price").toString();
                        item.setPrice(new BigDecimal(priceStr));
                    } else {
                        throw new IllegalArgumentException("Price is required for item");
                    }
                    
                    // Calculate subtotal
                    BigDecimal subtotal = item.getPrice()
                            .multiply(BigDecimal.valueOf(item.getQuantity()));
                    item.setSubtotal(subtotal);
                    
                    items.add(item);
                    log.info("Added item - productId: {}, quantity: {}, price: {}", 
                            item.getProductId(), item.getQuantity(), item.getPrice());
                } catch (Exception e) {
                    log.error("Error processing item: {}", e.getMessage(), e);
                    throw new IllegalArgumentException("Invalid item data: " + e.getMessage());
                }
            }
        }

        // Save items if not empty
        if (!items.isEmpty()) {
            invoiceItemRepository.saveAll(items);
            invoice.setItems(items);
            
            // Calculate total amount if not set
            if (invoice.getTotalAmount() == null) {
                BigDecimal totalAmount = items.stream()
                    .map(InvoiceItem::getSubtotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                invoice.setTotalAmount(totalAmount);
                invoiceRepository.save(invoice);
            }
            
            log.info("Saved {} items for invoice ID: {}", items.size(), invoice.getId());
        }
        
        return invoice;
    }
    
    @Transactional
    public Invoice createInvoiceFromCart(InvoiceRequest request) {
        log.info("Creating invoice from cart for user: {} with address: {}", 
                request.getUserId(), request.getShipAddress());
        
        // Validate request
        validateRequest(request);
        
        try {
            // Create new invoice
            Invoice invoice = new Invoice();
            invoice.setUserId(request.getUserId());
            invoice.setStatus(request.getStatus() != null ? request.getStatus() : "PENDING");
            invoice.setOrderDate(LocalDateTime.now());
            
            // Ensure shipAddress is properly set
            if (request.getShipAddress() == null || request.getShipAddress().trim().isEmpty()) {
                throw new IllegalArgumentException("Ship address cannot be empty");
            }
            invoice.setShipAddress(request.getShipAddress().trim());
            
            log.info("Created invoice entity with shipAddress: '{}'", invoice.getShipAddress());
            
            // Set total amount if provided
            if (request.getTotalAmount() != null) {
                invoice.setTotalAmount(request.getTotalAmount());
            }
            
            // Save invoice first to get ID
            invoice = invoiceRepository.save(invoice);
            log.info("Saved invoice - ID: {}, shipAddress: '{}', status: {}", 
                    invoice.getId(), invoice.getShipAddress(), invoice.getStatus());
            
            // Process cart items
            if (request.getItems() != null && !request.getItems().isEmpty()) {
                processCartItems(invoice, request.getItems());
            }
            
            // Verify the saved invoice
            Invoice savedInvoice = invoiceRepository.findById(invoice.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Invoice not found after saving"));
            
            log.info("Final invoice state - ID: {}, shipAddress: '{}'", 
                    savedInvoice.getId(), savedInvoice.getShipAddress());
            
            // Publish event after successful creation
            try {
                eventPublisherService.publishInvoiceCreated(savedInvoice);
                log.info("Published invoice created event for invoice ID: {}", savedInvoice.getId());
            } catch (Exception e) {
                log.error("Failed to publish invoice created event: {}", e.getMessage(), e);
            }
            
            return savedInvoice;
            
        } catch (Exception e) {
            log.error("Error creating invoice: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void validateRequest(InvoiceRequest request) {
        if (request.getUserId() == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        
        if (request.getShipAddress() == null || request.getShipAddress().trim().isEmpty()) {
            throw new IllegalArgumentException("Ship address cannot be empty");
        }
        
        log.info("Request validation passed - userId: {}, shipAddress: '{}'", 
                request.getUserId(), request.getShipAddress());
    }

    private void processCartItems(Invoice invoice, List<Map<String, Object>> items) {
        log.info("Processing {} cart items for invoice ID: {}", items.size(), invoice.getId());
        List<InvoiceItem> invoiceItems = new ArrayList<>();
        
        for (Map<String, Object> item : items) {
            try {
                InvoiceItem invoiceItem = createInvoiceItem(invoice, item);
                invoiceItems.add(invoiceItem);
            } catch (Exception e) {
                log.error("Error processing cart item: {}", e.getMessage(), e);
                throw new IllegalArgumentException("Invalid item data: " + e.getMessage());
            }
        }
        
        // Save all items
        invoiceItemRepository.saveAll(invoiceItems);
        invoice.setItems(invoiceItems);
        
        // Calculate total amount if not set
        if (invoice.getTotalAmount() == null) {
            BigDecimal totalAmount = invoiceItems.stream()
                    .map(InvoiceItem::getSubtotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            invoice.setTotalAmount(totalAmount);
            invoiceRepository.save(invoice);
        }
        
        log.info("Saved {} invoice items for invoice ID: {}", invoiceItems.size(), invoice.getId());
    }

    private InvoiceItem createInvoiceItem(Invoice invoice, Map<String, Object> itemData) {
        InvoiceItem invoiceItem = new InvoiceItem();
        invoiceItem.setInvoice(invoice);
        
        try {
            // Extract item data
            invoiceItem.setProductId(Long.valueOf(itemData.get("productId").toString()));
            invoiceItem.setQuantity(Integer.valueOf(itemData.get("quantity").toString()));
            
            // Get price from item
            if (itemData.containsKey("price")) {
                String priceStr = itemData.get("price").toString();
                invoiceItem.setPrice(new BigDecimal(priceStr));
            } else {
                throw new IllegalArgumentException("Price is required for item");
            }
            
            // Calculate subtotal
            BigDecimal subtotal = invoiceItem.getPrice()
                    .multiply(BigDecimal.valueOf(invoiceItem.getQuantity()));
            invoiceItem.setSubtotal(subtotal);
            
            return invoiceItem;
        } catch (Exception e) {
            log.error("Error creating invoice item: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Invalid item data: " + e.getMessage());
        }
    }

    public Invoice getInvoiceById(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Invoice not found with id: " + id));
    }

    public List<Invoice> getInvoicesByUserId(Long userId) {
        return invoiceRepository.findByUserId(userId);
    }

    public List<Invoice> getInvoicesByStatus(String status) {
        return invoiceRepository.findByStatus(status);
    }

    @Transactional
    public Invoice updateInvoiceStatus(Long id, String status, String transactionId) {
        Invoice invoice = getInvoiceById(id);
        invoice.setStatus(status);
        invoice.setTransactionId(transactionId);
        return invoiceRepository.save(invoice);
    }

    @Transactional
    public void deleteInvoice(Long id) {
        if (!invoiceRepository.existsById(id)) {
            throw new EntityNotFoundException("Invoice not found with id: " + id);
        }
        invoiceRepository.deleteById(id);
    }
} 