# Invoice Service API Documentation

## Base URL
```
http://localhost:8084/api/v1
```

## 1. Invoice APIs

### 1.1 Create New Invoice
- **URL**: `/invoices`
- **Method**: `POST`
- **Description**: Create a new invoice
- **Request Body**:
```json
{
    "userId": 1,
    "shipAddress": "123 Main Street, City",
    "invoiceDetails": [
        {
            "productId": 1,
            "quantity": 2,
            "discount": 0.1,
            "price": 100.00
        },
        {
            "productId": 2,
            "quantity": 1,
            "discount": 0.0,
            "price": 50.00
        }
    ]
}
```
- **Response**:
```json
{
    "invoiceId": 1,
    "userId": 1,
    "status": "pending",
    "orderDate": "2024-04-06T21:33:45.914",
    "shipAddress": "123 Main Street, City",
    "totalPrice": 230.00,
    "invoiceDetails": [
        {
            "invoiceDetailsId": 1,
            "productId": 1,
            "quantity": 2,
            "discount": 0.1,
            "price": 100.00
        },
        {
            "invoiceDetailsId": 2,
            "productId": 2,
            "quantity": 1,
            "discount": 0.0,
            "price": 50.00
        }
    ]
}
```

### 1.2 Get Invoice by ID
- **URL**: `/invoices/{invoiceId}`
- **Method**: `GET`
- **Description**: Get invoice details by ID
- **Response**:
```json
{
    "invoiceId": 1,
    "userId": 1,
    "status": "pending",
    "orderDate": "2024-04-06T21:33:45.914",
    "shipAddress": "123 Main Street, City",
    "totalPrice": 230.00,
    "invoiceDetails": [
        {
            "invoiceDetailsId": 1,
            "productId": 1,
            "quantity": 2,
            "discount": 0.1,
            "price": 100.00
        },
        {
            "invoiceDetailsId": 2,
            "productId": 2,
            "quantity": 1,
            "discount": 0.0,
            "price": 50.00
        }
    ]
}
```

### 1.3 Get Invoices by User ID
- **URL**: `/invoices/user/{userId}`
- **Method**: `GET`
- **Description**: Get all invoices for a specific user
- **Response**:
```json
[
    {
        "invoiceId": 1,
        "userId": 1,
        "status": "pending",
        "orderDate": "2024-04-06T21:33:45.914",
        "shipAddress": "123 Main Street, City",
        "totalPrice": 230.00,
        "invoiceDetails": [
            {
                "invoiceDetailsId": 1,
                "productId": 1,
                "quantity": 2,
                "discount": 0.1,
                "price": 100.00
            }
        ]
    }
]
```

### 1.4 Get Invoices by Status
- **URL**: `/invoices/status/{status}`
- **Method**: `GET`
- **Description**: Get all invoices with a specific status
- **Response**:
```json
[
    {
        "invoiceId": 1,
        "userId": 1,
        "status": "pending",
        "orderDate": "2024-04-06T21:33:45.914",
        "shipAddress": "123 Main Street, City",
        "totalPrice": 230.00,
        "invoiceDetails": [
            {
                "invoiceDetailsId": 1,
                "productId": 1,
                "quantity": 2,
                "discount": 0.1,
                "price": 100.00
            }
        ]
    }
]
```

### 1.5 Update Invoice Status
- **URL**: `/invoices/{invoiceId}/status`
- **Method**: `PUT`
- **Description**: Update the status of an invoice
- **Request Body**:
```json
{
    "status": "completed"
}
```
- **Response**:
```json
{
    "invoiceId": 1,
    "userId": 1,
    "status": "completed",
    "orderDate": "2024-04-06T21:33:45.914",
    "shipAddress": "123 Main Street, City",
    "totalPrice": 230.00,
    "invoiceDetails": [
        {
            "invoiceDetailsId": 1,
            "productId": 1,
            "quantity": 2,
            "discount": 0.1,
            "price": 100.00
        }
    ]
}
```

### 1.6 Delete Invoice
- **URL**: `/invoices/{invoiceId}`
- **Method**: `DELETE`
- **Description**: Delete an invoice by ID
- **Response**: 204 No Content




## 2. Invoice Details APIs

### 2.1 Get Invoice Details by Invoice ID
- **URL**: `/invoice-details/invoice/{invoiceId}`
- **Method**: `GET`
- **Description**: Get all invoice details for a specific invoice
- **Response**:
```json
[
    {
        "invoiceDetailsId": 1,
        "discount": 0.1,
        "price": 100.00,
        "quantity": 2,
        "productId": 1,
        "invoiceId": 1
    },
    {
        "invoiceDetailsId": 2,
        "discount": 0.0,
        "price": 50.00,
        "quantity": 1,
        "productId": 2,
        "invoiceId": 1
    }
]
```

### 2.2 Get Invoice Details by Product ID
- **URL**: `/invoice-details/product/{productId}`
- **Method**: `GET`
- **Description**: Get all invoice details for a specific product
- **Response**:
```json
[
    {
        "invoiceDetailsId": 1,
        "discount": 0.1,
        "price": 100.00,
        "quantity": 2,
        "productId": 1,
        "invoiceId": 1
    },
    {
        "invoiceDetailsId": 3,
        "discount": 0.0,
        "price": 100.00,
        "quantity": 1,
        "productId": 1,
        "invoiceId": 2
    }
]
```

### 2.3 Update Invoice Details
- **URL**: `/invoice-details/{id}`
- **Method**: `PUT`
- **Description**: Update specific invoice details
- **Request Body**:
```json
{
    "quantity": 3,
    "discount": 0.15,
    "price": 100.00,
    "productId": 1
}
```
- **Response**:
```json
{
    "invoiceDetailsId": 1,
    "discount": 0.15,
    "price": 100.00,
    "quantity": 3,
    "productId": 1,
    "invoiceId": 1
}
```

### 2.4 Delete Invoice Details
- **URL**: `/invoice-details/{id}`
- **Method**: `DELETE`
- **Description**: Delete specific invoice details
- **Response**: 
```json
{
    "message": "Invoice details deleted successfully!",
    "status": "SUCCESS",
    "data": null
}
```

``` 