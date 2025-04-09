-- Tạo bảng BRAND
CREATE TABLE BRAND (
    BRANDID INT PRIMARY KEY,
    BRANDNAME VARCHAR(50) NOT NULL UNIQUE
);

-- Tạo bảng CART
CREATE TABLE CART (
    CARTID INT AUTO_INCREMENT PRIMARY KEY,
    TOTAL DECIMAL(19,2),
    USER_ID INT
);

-- Tạo bảng CART_DETAILS
CREATE TABLE CART_DETAILS (
    CART_DETAILSID INT AUTO_INCREMENT PRIMARY KEY,
    QUANTITY INT NOT NULL,
    TOTAL DECIMAL(19,2),
    CARTID INT,
    PRODUCT_ID INT
);

-- Tạo bảng CATEGORY
CREATE TABLE CATEGORY (
    CATEGORYID INT PRIMARY KEY,
    CATEGORYNAME VARCHAR(50) NOT NULL UNIQUE
);

-- Tạo bảng INVOICE
CREATE TABLE INVOICE (
    INVOICEID INT AUTO_INCREMENT PRIMARY KEY,
    ORDER_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    SHIP_ADDRESS VARCHAR(255),
    STATUS VARCHAR(255),
    TOTAL_PRICE DECIMAL(19,2),
    USER_ID INT NOT NULL
);

-- Tạo bảng INVOICE_DETAILS
CREATE TABLE INVOICE_DETAILS (
    INVOICE_DETAILS_ID INT AUTO_INCREMENT PRIMARY KEY,
    DISCOUNT DECIMAL(19,2),
    PRICE DECIMAL(19,2),
    QUANTITY INT NOT NULL,
    INVOICEID INT,
    PRODUCT_ID INT
);

-- Tạo bảng PRODUCTREVIEW
CREATE TABLE PRODUCTREVIEW (
    REVIEWID INT PRIMARY KEY,
    USERID INT NOT NULL,
    PRODUCTID INT NOT NULL,
    RATING INT NOT NULL,
    COMMENTS VARCHAR(500),
    REVIEWDATE DATE NOT NULL
);

-- Tạo bảng PRODUCTS
CREATE TABLE PRODUCTS (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    BRAND_NAME VARCHAR(255),
    CATEGORY VARCHAR(255),
    DESCRIPTION VARCHAR(255),
    DESIGNER VARCHAR(255),
    IMG_URL VARCHAR(255),
    PRODUCT_NAME VARCHAR(255),
    PRODUCT_PRICE DECIMAL(19,2),
    QUANTITY INT NOT NULL
);

-- Tạo bảng ROLES
CREATE TABLE ROLES (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    NAME VARCHAR(20)
);

-- Tạo bảng USER_ROLES
CREATE TABLE USER_ROLES (
    USER_ID INT NOT NULL,
    ROLE_ID INT NOT NULL,
    PRIMARY KEY (USER_ID, ROLE_ID)
);

-- Tạo bảng USERS
CREATE TABLE USERS (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    ADDRESS VARCHAR(255),
    BIRTH VARCHAR(255),
    EMAIL VARCHAR(50),
    GENDER VARCHAR(255),
    PASSWORD VARCHAR(120),
    PHONE VARCHAR(255),
    ROLE VARCHAR(255),
    USERNAME VARCHAR(20) UNIQUE
);

-- Thêm ràng buộc khóa ngoại
ALTER TABLE CART ADD CONSTRAINT FK_CART_USER FOREIGN KEY (USER_ID) REFERENCES USERS(ID);
ALTER TABLE CART_DETAILS ADD CONSTRAINT FK_CART_DETAILS_CART FOREIGN KEY (CARTID) REFERENCES CART(CARTID);
ALTER TABLE CART_DETAILS ADD CONSTRAINT FK_CART_DETAILS_PRODUCT FOREIGN KEY (PRODUCT_ID) REFERENCES PRODUCTS(ID);
ALTER TABLE INVOICE ADD CONSTRAINT FK_INVOICE_USER FOREIGN KEY (USER_ID) REFERENCES USERS(ID);
ALTER TABLE INVOICE_DETAILS ADD CONSTRAINT FK_INVOICE_DETAILS_INVOICE FOREIGN KEY (INVOICEID) REFERENCES INVOICE(INVOICEID);
ALTER TABLE INVOICE_DETAILS ADD CONSTRAINT FK_INVOICE_DETAILS_PRODUCT FOREIGN KEY (PRODUCT_ID) REFERENCES PRODUCTS(ID);
ALTER TABLE USER_ROLES ADD CONSTRAINT FK_USER_ROLES_ROLE FOREIGN KEY (ROLE_ID) REFERENCES ROLES(ID);
ALTER TABLE USER_ROLES ADD CONSTRAINT FK_USER_ROLES_USER FOREIGN KEY (USER_ID) REFERENCES USERS(ID);

-- Chèn dữ liệu vào bảng PRODUCTS
INSERT INTO PRODUCTS (BRAND_NAME, CATEGORY, DESCRIPTION, DESIGNER, IMG_URL, PRODUCT_NAME, PRODUCT_PRICE, QUANTITY) VALUES
('Air Jordan', 'basketball', 'Nike Air Jordan 1 Retro High OG Shadow 2018 là phiên bản tái phát hành từ bản gốc năm 1985. Đôi giày có phần trên là chất liệu da màu đen pha xám, với đế trắng và mặt đế đen. Đặc biệt, trên đôi giày có logo OG Nike Air.', 'Peter Moore', 'https://image.goat.com/750/attachments/product_template_pictures/images/011/119/994/original/218099_00.png.png', 'Air Jordan 1 Retro High OG Shadow 2018', 2000000, 20),
('Air Jordan', 'basketball', 'Nike Air Jordan 1 Retro High OG Shadow 2018 là phiên bản tái phát hành từ bản gốc năm 1985. Đôi giày có phần trên là chất liệu da màu đen pha xám, với đế trắng và mặt đế đen.', 'Tinker Hatfield', 'https://image.goat.com/750/attachments/product_template_pictures/images/008/654/900/original/52015_00.png.png', 'Air Jordan 4 Retro OG GS Bred 2019', 3000000, 18),
('Air Jordan', 'basketball', 'Air Jordan 11 Retro Space Jam phiên bản retro năm 2016 này đã trở thành một trong những lần ra mắt thành công nhất của Nike tính đến thời điểm đó.', 'Tinker Hatfield', 'https://image.goat.com/750/attachments/product_template_pictures/images/008/654/900/original/52015_00.png.png', 'Air Jordan 11 Retro Space Jam 2016', 2500000, 16),
('Air Jordan', 'basketball', 'Năm 1996 là năm mà đội Chicago Bulls kết thúc mùa giải thường lệ với kỷ lục 72 chiến thắng, Michael đã mang đôi giày Jordan 11 trong chuỗi trận thắng đó, và phiên bản phát hành vào năm 2017 nhằm tôn vinh đội hình 96 bất khả chiến bại.', 'Tinker Hatfield', 'https://image.goat.com/750/attachments/product_template_pictures/images/008/870/353/original/235806_00.png.png', 'Air Jordan 11 Retro Win Like 96', 3500000, 16),
('Air Jordan', 'basketball', 'Air Jordan 11 Retro Legend Blue 2014 lấy cảm hứng từ Jordan 11 Columbia năm 1996 được Jordan mặc lần đầu trong trận đấu NBA All-Star năm 1996.', 'Tinker Hatfield', 'https://image.goat.com/750/attachments/product_template_pictures/images/010/223/048/original/13607_00.png.png', 'Air Jordan 11 Retro Legend Blue 2014', 4500000, 16),
('Nike', 'lifestyle', 'Cuộc thi On Air năm 2018, người chiến thắng Gwang Shin đã ra mắt giày thể thao Air Max 97 On Air: Neon để bày tỏ sự ngưỡng mộ đối với thành phố Seoul của mình.', 'Gwang Shin', 'https://image.goat.com/750/attachments/product_template_pictures/images/020/627/570/original/491891_00.png.png', 'Air Max 97 On Air: Neon Seoul', 1500000, 15),
('Nike', 'lifestyle', 'Off-White x Air Max 90 ‘Black’ mang đến sự pha trộn độc đáo giữa các chất liệu kết hợp phần đế bằng ripstop phủ lớp nubuck cùng thiết kế da lộn', 'Jerry Lorenz', 'https://image.goat.com/750/attachments/product_template_pictures/images/012/750/761/original/351623_00.png.png', 'OFF-WHITE x Air Max 90 Black', 7500000, 15),
('Adidas', 'lifestyle', 'Đôi giày thể thao này có phần trên màu trắng và xám trung tính, dòng chữ SPLY-350 màu đỏ ở mặt sau. Giày cũng đi kèm với một miếng dán ở gót chân, lớp lót bên trong tông màu xanh lam.', 'Kanye West', 'https://image.goat.com/750/attachments/product_template_pictures/images/021/147/972/original/504187_00.png.png', 'Yeezy Boost 700 V2 Vanta', 5500000, 15),
('Adidas', 'lifestyle', 'Đôi giày Yeezy Boost 350 V2 Beluga 2.0 có sọc xám mờ ở hai bên thay vì sọc cam sáng như trên phiên bản ban đầu của giày thể thao Beluga. Ngoài ra, nó còn được trang bị tab kéo gót với đường khâu màu cam và chữ SPLY-350 màu cam ngược ở hai bên.', 'Kanye West', 'https://image.goat.com/750/attachments/product_template_pictures/images/008/654/534/original/152982_00.png.png', 'Yeezy Boost 350 V2 Beluga 2.0', 4599000, 16),
('Adidas', 'lifestyle', 'Đôi giày Yeezy Boost 350 V2 Sesame mang một bảng màu tinh tế phối hợp hoàn hảo với thiết kế tối giản của đôi giày. Phần trên Primeknit thoáng khí giữ nguyên tab gót và chi tiết may trung tâm đặc trưng, nhưng không còn chữ SPLY-350 được phản chiếu', 'Kanye West', 'https://image.goat.com/750/attachments/product_template_pictures/images/014/507/851/original/195483_00.png.png', 'Yeezy Boost 350 V2 Sesame', 5600000, 16),
('Nike', 'running', 'Air Max 97 Triple White có phần trên bằng da trắng và lưới với điểm nhấn màu Wolf Grey. Ra mắt vào tháng 8 năm 2017, đôi giày là một trong những thiết kế của bộ sưu tập Air Max.', 'Christian Tresser', 'https://image.goat.com/750/attachments/product_template_pictures/images/021/321/832/original/503571_00.png.png', 'Air Max 97 Triple White', 5600000, 16),
('Nike', 'running', 'Ra mắt vào tháng 3 năm 2018, Air Max 270 White lấy cảm hứng từ cả Air Max 180 và Air Max 93. Phần trên bằng lưới trắng được nhấn nhá bằng những điểm màu xám trên vòng kéo gót cùng logo Swoosh trên đầu ngón chân và bên hông', 'Christian Tresser', 'https://image.goat.com/750/attachments/product_template_pictures/images/010/634/133/original/303217_00.png.png', 'Air Max 270', 1999999, 16),
('Adidas', 'running', 'Thiết kế được trang bị phần trên bằng chất liệu Primeknit đặc trưng của adidas, đôi giày phong cách này có màu vàng sáng năng động, chạy dọc từ dây buộc giày, vòng kéo gót đến phần đế cao su với công nghệ Boost.', 'Kanye West', 'https://image.goat.com/750/attachments/product_template_pictures/images/016/928/118/original/155573_00.png.png', 'Yeezy Boost 350 V2 Semi Frozen Yellow', 7800000, 16),
('Converse', 'lifestyle', 'Phiên bản Comme des Garçons x Chuck Taylor All Star Hi này có phần trên bằng vải màu kem nhạt, logo tim CDG màu đỏ ở các bên hông, dải đối lập màu đen trên gót, đầu giày màu trắng và đế giữa bằng cao su.', 'Marquis Mills', 'https://image.goat.com/750/attachments/product_template_pictures/images/015/298/767/original/77243_00.png.png', 'Comme des Garçons x Chuck Taylor All Star Hi Milk', 5600000, 16),
('Converse', 'lifestyle', 'Phiên bản Artist Series của Converse Chuck 70, với phần trên bằng vải màu kem nhạt được in họa tiết gốc từ Wyatt Navarro. Phần viền đặc trưng của dáng giày được tăng cao và được trang trí với các dải tương phản màu xanh và cam.', 'Marquis Mills', 'https://image.goat.com/750/attachments/product_template_pictures/images/018/552/840/original/476518_00.png.png', 'Tyler, The Creator x Foot Locker x Chuck 70 Artist Series', 1000000, 20);

-- Chèn dữ liệu vào bảng BRAND
INSERT INTO BRAND (BRANDID, BRANDNAME) VALUES
(9, 'Air Jordan'),
(10, 'Converse'),
(2, 'adidas'),
(4, 'adlv'),
(5, 'balenciaga'),
(7, 'drew'),
(8, 'essential'),
(6, 'mlb'),
(1, 'nike'),
(3, 'puma');

-- Chèn dữ liệu vào bảng CATEGORY
INSERT INTO CATEGORY (CATEGORYID, CATEGORYNAME) VALUES
(3, 'basketball'),
(1, 'lifestyle'),
(2, 'running');

-- Chèn dữ liệu vào bảng ROLES
INSERT INTO ROLES (NAME) VALUES
('ROLE_USER'),
('ROLE_ADMIN');

-- Trigger cập nhật số lượng sản phẩm khi có thay đổi trong chi tiết hóa đơn
DELIMITER //
CREATE TRIGGER trg_update_product_quantity
AFTER INSERT ON INVOICE_DETAILS
FOR EACH ROW
BEGIN
    UPDATE PRODUCTS
    SET QUANTITY = QUANTITY - NEW.QUANTITY
    WHERE ID = NEW.PRODUCT_ID;
END;
//
DELIMITER ;

-- Trigger kiểm tra số lượng sản phẩm trong kho khi tạo hóa đơn
DELIMITER //
CREATE TRIGGER check_product_quantity
BEFORE INSERT ON INVOICE_DETAILS
FOR EACH ROW
BEGIN
    DECLARE v_available_quantity INT;
    SELECT QUANTITY INTO v_available_quantity
    FROM PRODUCTS
    WHERE ID = NEW.PRODUCT_ID;

    IF NEW.QUANTITY > v_available_quantity THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Số lượng sản phẩm không đủ trong kho';
    END IF;
END;
//
DELIMITER ;

-- Trigger ràng buộc brand name
DELIMITER //
CREATE TRIGGER trg_validate_brandname
BEFORE INSERT ON PRODUCTS
FOR EACH ROW
BEGIN
    DECLARE brand_exists INT;
    SELECT COUNT(*) INTO brand_exists FROM BRAND WHERE UPPER(BRANDNAME) = UPPER(NEW.BRAND_NAME);
    IF brand_exists = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Brandname không hợp lệ';
    END IF;
END;
//
DELIMITER ;

-- Trigger ràng buộc Category
DELIMITER //
CREATE TRIGGER trg_validate_category
BEFORE INSERT ON PRODUCTS
FOR EACH ROW
BEGIN
    DECLARE category_exists INT;
    SELECT COUNT(*) INTO category_exists FROM CATEGORY WHERE CATEGORYNAME = NEW.Category;
    IF category_exists = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Category không hợp lệ';
    END IF;
END;
//
DELIMITER ;

-- Trigger tính tiền hóa đơn
DELIMITER //
CREATE TRIGGER Trg_INVOICE_VALUE
AFTER INSERT ON INVOICE_DETAILS
FOR EACH ROW
BEGIN
    DECLARE invoice_value DECIMAL(19,2);
    SELECT SUM(QUANTITY * PRICE) INTO invoice_value
    FROM INVOICE_DETAILS
    WHERE INVOICEID = NEW.INVOICEID;

    UPDATE INVOICE
    SET TOTAL_PRICE = invoice_value
    WHERE INVOICEID = NEW.INVOICEID;
END;
//
DELIMITER ;

-- Trigger kiểm tra ngày sinh khách hàng
DELIMITER //
CREATE TRIGGER Trg_birth_user
BEFORE INSERT ON USERS
FOR EACH ROW
BEGIN
    DECLARE birthdate DATE;
    SET birthdate = STR_TO_DATE(NEW.BIRTH, '%d/%m/%Y');

    IF birthdate >= CURDATE() THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Ngày sinh không hợp lệ. Vui lòng kiểm tra lại.';
    END IF;
END;
//
DELIMITER ;

-- Trigger ngày tạo hóa đơn bằng ngày hiện tại
DELIMITER //
CREATE TRIGGER TRG_ORDER_DATE
BEFORE INSERT ON INVOICE
FOR EACH ROW
BEGIN
    SET NEW.ORDER_DATE = NOW();
END;
//
DELIMITER ;

-- Trigger khách hàng phải mua sản phẩm mới được đánh giá
DELIMITER //
CREATE TRIGGER trg_check_product_purchase
BEFORE INSERT ON ProductReview
FOR EACH ROW
BEGIN
    DECLARE v_count INT;
    SELECT COUNT(*) INTO v_count
    FROM INVOICE_DETAILS
    WHERE USERID = NEW.USERID
        AND PRODUCT_ID = NEW.PRODUCTID;

    IF v_count = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Khách hàng phải mua sản phẩm trước khi đánh giá.';
    END IF;
END;
//
DELIMITER ;

-- Stored Procedure lấy thông tin sản phẩm
DELIMITER //

CREATE PROCEDURE GetProductDetails(IN p_product_id INT)
BEGIN
    -- Khai báo các biến để lưu trữ thông tin sản phẩm
    DECLARE v_id INT;
    DECLARE v_brand_name VARCHAR(255);
    DECLARE v_category VARCHAR(255);
    DECLARE v_description VARCHAR(255);
    DECLARE v_designer VARCHAR(255);
    DECLARE v_img_url VARCHAR(255);
    DECLARE v_product_name VARCHAR(255);
    DECLARE v_product_price DECIMAL(19,2);
    DECLARE v_quantity INT;

    -- Lấy thông tin sản phẩm từ bảng PRODUCTS
    SELECT 
        ID, BRAND_NAME, CATEGORY, DESCRIPTION, DESIGNER, 
        IMG_URL, PRODUCT_NAME, PRODUCT_PRICE, QUANTITY
    INTO 
        v_id, v_brand_name, v_category, v_description, v_designer, 
        v_img_url, v_product_name, v_product_price, v_quantity
    FROM PRODUCTS
    WHERE ID = p_product_id;

    -- In thông tin chi tiết của sản phẩm
    SELECT 
        CONCAT('Product ID: ', v_id) AS ProductID,
        CONCAT('Product Name: ', v_product_name) AS ProductName,
        CONCAT('Category: ', v_category) AS Category,
        CONCAT('Price: ', v_product_price) AS Price,
        CONCAT('Image URL: ', v_img_url) AS ImageURL,
        CONCAT('Description: ', v_description) AS Description,
        CONCAT('Quantity: ', v_quantity) AS Quantity,
        CONCAT('Brand Name: ', v_brand_name) AS BrandName,
        CONCAT('Designer: ', v_designer) AS Designer;
END //

DELIMITER ;

-- Stored Procedure thêm vào giỏ hàng
DELIMITER //

CREATE PROCEDURE AddToCart (
    IN p_UserID INT,
    IN p_ProductID INT,
    IN p_Quantity INT
)
BEGIN
    DECLARE v_CartID INT;
    DECLARE v_Total DECIMAL(19,2);

    -- Lấy mã giỏ hàng của người dùng
    SELECT CARTID INTO v_CartID
    FROM CART
    WHERE USER_ID = p_UserID
    LIMIT 1;
    
    -- Lấy giá sản phẩm từ bảng PRODUCTS
    SELECT PRODUCT_PRICE INTO v_Total
    FROM PRODUCTS
    WHERE ID = p_ProductID
    LIMIT 1;

    -- Tính toán tổng giá trị
    SET v_Total = v_Total * p_Quantity;

    -- Thêm sản phẩm vào giỏ hàng  
    INSERT INTO CART_DETAILS (QUANTITY, TOTAL, CARTID, PRODUCT_ID)
    VALUES (p_Quantity, v_Total, v_CartID, p_ProductID);

    -- Cập nhật tổng trị giá của giỏ hàng
    UPDATE CART
    SET TOTAL = (
        SELECT SUM(CD.QUANTITY * P.PRODUCT_PRICE)
        FROM CART_DETAILS CD
        INNER JOIN PRODUCTS P ON CD.PRODUCT_ID = P.ID
        WHERE CD.CARTID = v_CartID
    )
    WHERE CARTID = v_CartID;
    
    -- In thông tin sản phẩm đã thêm vào giỏ hàng
    SELECT 'Sản phẩm đã được thêm vào giỏ hàng.' AS Message;
    SELECT CONCAT('Mã sản phẩm: ', p_ProductID) AS Product_Info;
    SELECT CONCAT('Số lượng: ', p_Quantity) AS Quantity_Info;
    SELECT CONCAT('Giỏ hàng ID: ', v_CartID) AS Cart_Info;
END;
//
DELIMITER ;


-- Stored Procedure tạo hóa đơn
DELIMITER //

CREATE PROCEDURE CreateInvoice(IN p_UserID INT)
BEGIN
    DECLARE v_CartID INT;
    DECLARE v_InvoiceID INT;
    DECLARE v_ProductID INT;
    DECLARE v_Quantity INT;
    DECLARE v_Price DECIMAL(19,2);

    -- Khai báo biến để kiểm tra xem có dữ liệu trong giỏ hàng không
    DECLARE no_more_rows BOOLEAN DEFAULT FALSE;

    -- Khai báo CURSOR để duyệt qua các sản phẩm trong giỏ hàng
    DECLARE cart_cursor CURSOR FOR
        SELECT CD.PRODUCT_ID, CD.QUANTITY, P.PRODUCT_PRICE
        FROM CART_DETAILS CD
        INNER JOIN PRODUCTS P ON CD.PRODUCT_ID = P.ID
        WHERE CD.CARTID = v_CartID;

    -- Khai báo handler để xử lý khi không còn dữ liệu trong CURSOR
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET no_more_rows = TRUE;

    -- Tạo một hóa đơn mới
    INSERT INTO INVOICE (USER_ID, STATUS)
    VALUES (p_UserID, 'notpaid');
    SET v_InvoiceID = LAST_INSERT_ID();

    -- Lấy mã giỏ hàng của người dùng
    SELECT CARTID INTO v_CartID FROM CART WHERE USER_ID = p_UserID;

    -- Mở CURSOR để duyệt qua các sản phẩm trong giỏ hàng
    OPEN cart_cursor;

    -- Bắt đầu vòng lặp để xử lý từng sản phẩm
    read_loop: LOOP
        -- Lấy dữ liệu từ CURSOR
        FETCH cart_cursor INTO v_ProductID, v_Quantity, v_Price;

        -- Kiểm tra xem còn dữ liệu không
        IF no_more_rows THEN
            LEAVE read_loop;
        END IF;

        -- Tính toán giá tiền
        SET v_Price = v_Quantity * v_Price;

        -- Tạo chi tiết hóa đơn
        INSERT INTO INVOICE_DETAILS (INVOICEID, PRODUCT_ID, QUANTITY, PRICE)
        VALUES (v_InvoiceID, v_ProductID, v_Quantity, v_Price);
    END LOOP;

    -- Đóng CURSOR
    CLOSE cart_cursor;

    -- Xóa giỏ hàng
    CALL ClearCartItems(v_CartID);

    -- Hiển thị thông báo thành công
    SELECT CONCAT('Hóa đơn đã được tạo. Mã hóa đơn: ', v_InvoiceID) AS Message;
END //

DELIMITER ;

-- Stored Procedure xóa giỏ hàng
DELIMITER //
CREATE PROCEDURE ClearCartItems(IN p_CartID INT)
BEGIN
    DELETE FROM CART_DETAILS WHERE CARTID = p_CartID;
END;
//
DELIMITER ;

-- Stored Procedure lấy chi tiết hóa đơn
DELIMITER //

CREATE PROCEDURE GetInvoice(IN p_InvoiceID INT)
BEGIN
    DECLARE v_ProductID INT;
    DECLARE v_Quantity INT;
    DECLARE v_Price DECIMAL(19,2);
    DECLARE done INT DEFAULT FALSE;
    
    -- Cursor để lấy thông tin sản phẩm trong hóa đơn
    DECLARE cur CURSOR FOR 
        SELECT PRODUCT_ID, QUANTITY, PRICE 
        FROM INVOICE_DETAILS 
        WHERE INVOICEID = p_InvoiceID;
    
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    -- Lấy thông tin hóa đơn
    SELECT INVOICEID, USER_ID, STATUS, TOTAL_PRICE 
    FROM INVOICE 
    WHERE INVOICEID = p_InvoiceID;

    SELECT 'List product in Invoice:';

    -- Mở cursor
    OPEN cur;

    read_loop: LOOP
        FETCH cur INTO v_ProductID, v_Quantity, v_Price;
        IF done THEN
            LEAVE read_loop;
        END IF;
        
        -- Hiển thị từng sản phẩm trong hóa đơn
        SELECT '------------------------';
        SELECT CONCAT('Product ID: ', v_ProductID);
        SELECT CONCAT('Quantity: ', v_Quantity);
        SELECT CONCAT('Price: ', v_Price);
    END LOOP;

    -- Đóng cursor
    CLOSE cur;
END;
//
DELIMITER ;

-- Stored Procedure lấy các hóa đơn của user
DELIMITER //

CREATE PROCEDURE GetUserInvoices(IN p_UserID INT)
BEGIN
    DECLARE v_InvoiceID INT;
    DECLARE v_Status VARCHAR(255);
    DECLARE v_TotalPrice DECIMAL(19,2);
    DECLARE done INT DEFAULT FALSE;
    
    -- Cursor để lấy danh sách hóa đơn của user
    DECLARE cur CURSOR FOR 
        SELECT INVOICEID, STATUS, TOTAL_PRICE 
        FROM INVOICE 
        WHERE USER_ID = p_UserID;

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    -- In thông tin user
    SELECT CONCAT('User ID: ', p_UserID);
    SELECT 'List Invoice of User: ';
    
    -- Mở cursor
    OPEN cur;

    read_loop: LOOP
        FETCH cur INTO v_InvoiceID, v_Status, v_TotalPrice;
        IF done THEN
            LEAVE read_loop;
        END IF;
        
        -- Hiển thị từng hóa đơn
        SELECT '------------------------';
        SELECT CONCAT('InvoiceID: ', v_InvoiceID);
        SELECT CONCAT('Status: ', v_Status);
        SELECT CONCAT('Total Price: ', v_TotalPrice);
    END LOOP;

    -- Đóng cursor
    CLOSE cur;
END;
//
DELIMITER ;


-- Stored Procedure lấy giỏ hàng
DELIMITER //

CREATE PROCEDURE GetCartDetails(IN p_UserID INT)
BEGIN
    DECLARE v_CartDetailID INT;
    DECLARE v_ProductID INT;
    DECLARE v_Quantity INT;
    DECLARE v_Total DECIMAL(19,2);
    DECLARE done INT DEFAULT FALSE;

    -- Cursor để lấy thông tin giỏ hàng
    DECLARE cur CURSOR FOR 
        SELECT CD.CART_DETAILSID, CD.PRODUCT_ID, CD.QUANTITY, CD.TOTAL 
        FROM CART_DETAILS CD
        INNER JOIN CART C ON CD.CARTID = C.CARTID
        WHERE C.USER_ID = p_UserID;

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    -- Mở cursor
    OPEN cur;

    read_loop: LOOP
        FETCH cur INTO v_CartDetailID, v_ProductID, v_Quantity, v_Total;
        IF done THEN
            LEAVE read_loop;
        END IF;
        
        -- Hiển thị từng sản phẩm trong giỏ hàng
        SELECT '------------------------';
        SELECT CONCAT('Cart Detail ID: ', v_CartDetailID);
        SELECT CONCAT('Product ID: ', v_ProductID);
        SELECT CONCAT('Quantity: ', v_Quantity);
        SELECT CONCAT('Total: ', v_Total);
    END LOOP;

    -- Đóng cursor
    CLOSE cur;
END;
//
DELIMITER ;


-- Stored Procedure lấy top sản phẩm bán chạy
DELIMITER //

CREATE PROCEDURE GetTopSellingProducts(IN p_TopCount INT)
BEGIN
    DECLARE v_ProductID INT;
    DECLARE v_ProductName VARCHAR(255);
    DECLARE v_TotalSold INT;
    DECLARE done INT DEFAULT FALSE;

    -- Cursor để lấy top sản phẩm bán chạy
    DECLARE cur CURSOR FOR 
        SELECT P.ID, P.PRODUCT_NAME, SUM(ID.QUANTITY) AS TOTAL_SOLD
        FROM INVOICE_DETAILS ID
        INNER JOIN PRODUCTS P ON ID.PRODUCT_ID = P.ID
        GROUP BY P.ID, P.PRODUCT_NAME
        ORDER BY TOTAL_SOLD DESC
        LIMIT p_TopCount;

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    -- Mở cursor
    OPEN cur;

    read_loop: LOOP
        FETCH cur INTO v_ProductID, v_ProductName, v_TotalSold;
        IF done THEN
            LEAVE read_loop;
        END IF;
        
        -- Hiển thị thông tin sản phẩm
        SELECT CONCAT('Product ID: ', v_ProductID, ', Product Name: ', v_ProductName, ', Total Sold: ', v_TotalSold);
    END LOOP;

    -- Đóng cursor
    CLOSE cur;
END;
//
DELIMITER ;



-- Stored Procedure lấy top sản phẩm bán chạy theo tháng
DELIMITER //

CREATE PROCEDURE GetTopSellingProductsByMonth(IN p_Month INT, IN p_Year INT, IN p_TopCount INT)
BEGIN
    DECLARE v_ProductID INT;
    DECLARE v_ProductName VARCHAR(255);
    DECLARE v_TotalSold INT;
    DECLARE done INT DEFAULT FALSE;

    -- Cursor để lấy danh sách top sản phẩm bán chạy trong tháng
    DECLARE cur CURSOR FOR 
        SELECT P.ID, P.PRODUCT_NAME, SUM(ID.QUANTITY) AS TOTAL_SOLD
        FROM INVOICE_DETAILS ID
        INNER JOIN PRODUCTS P ON ID.PRODUCT_ID = P.ID
        INNER JOIN INVOICE  ON ID.INVOICEID = INVOICE.INVOICEID
        WHERE MONTH(INVOICE.ORDER_DATE) = p_Month
          AND YEAR(INVOICE.ORDER_DATE) = p_Year
        GROUP BY P.ID, P.PRODUCT_NAME
        ORDER BY TOTAL_SOLD DESC
        LIMIT p_TopCount;

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    -- In thông báo
    SELECT CONCAT('Top ', p_TopCount, ' product of ', p_Month, '/', p_Year, ':');

    -- Mở cursor
    OPEN cur;

    read_loop: LOOP
        FETCH cur INTO v_ProductID, v_ProductName, v_TotalSold;
        IF done THEN
            LEAVE read_loop;
        END IF;
        
        -- Hiển thị thông tin sản phẩm
        SELECT CONCAT('Product ID: ', v_ProductID, ', Product Name: ', v_ProductName, ', Total Sold: ', v_TotalSold);
    END LOOP;

    -- Đóng cursor
    CLOSE cur;
END;
//
DELIMITER ;
