-- Create databases
CREATE DATABASE IF NOT EXISTS `db-auth` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `db-product` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `db-cart` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `db-invoice` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `db-payment` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Use the auth database
USE `db-auth`;

-- Create default roles if needed
CREATE TABLE IF NOT EXISTS `roles` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Insert default roles if table is empty
INSERT INTO `roles` (`id`, `name`) 
SELECT 1, 'ROLE_USER' FROM DUAL WHERE NOT EXISTS (SELECT * FROM `roles` WHERE `id` = 1)
UNION ALL
SELECT 2, 'ROLE_ADMIN' FROM DUAL WHERE NOT EXISTS (SELECT * FROM `roles` WHERE `id` = 2);

-- Use the product database
USE `db-product`;

-- Create products table
CREATE TABLE IF NOT EXISTS `products` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `brand_name` varchar(255) NOT NULL,
  `category` varchar(50) NOT NULL,
  `description` text,
  `designer` varchar(255),
  `img_url` varchar(500),
  `product_name` varchar(255) NOT NULL,
  `product_price` decimal(15,2) NOT NULL,
  `quantity` int NOT NULL DEFAULT 0,
  `reorder_level` int NOT NULL DEFAULT 5,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Insert sample products
INSERT INTO products 
(brand_name, category, description, designer, img_url, product_name, product_price, quantity, reorder_level, created_at, updated_at) VALUES
('Air Jordan', 'basketball', 'Nike Air Jordan 1 Retro High OG Shadow 2018 là phiên bản tái phát hành từ bản gốc năm 1985. Đôi giày có phần trên là chất liệu da màu đen pha xám, với đế trắng và mặt đế đen. Đặc biệt, trên đôi giày có logo OG Nike Air.', 'Peter Moore', 'https://image.goat.com/750/attachments/product_template_pictures/images/011/119/994/original/218099_00.png.png', 'Air Jordan 1 Retro High OG Shadow 2018', 2000000, 20, 5, NOW(), NOW()),
('Air Jordan', 'basketball', 'Nike Air Jordan 1 Retro High OG Shadow 2018 là phiên bản tái phát hành từ bản gốc năm 1985. Đôi giày có phần trên là chất liệu da màu đen pha xám, với đế trắng và mặt đế đen.', 'Tinker Hatfield', 'https://image.goat.com/750/attachments/product_template_pictures/images/008/654/900/original/52015_00.png.png', 'Air Jordan 4 Retro OG GS Bred 2019', 3000000, 18, 5, NOW(), NOW()),
('Air Jordan', 'basketball', 'Air Jordan 11 Retro Space Jam phiên bản retro năm 2016 này đã trở thành một trong những lần ra mắt thành công nhất của Nike tính đến thời điểm đó.', 'Tinker Hatfield', 'https://image.goat.com/750/attachments/product_template_pictures/images/008/654/900/original/52015_00.png.png', 'Air Jordan 11 Retro Space Jam 2016', 2500000, 16, 5, NOW(), NOW()),
('Air Jordan', 'basketball', 'Năm 1996 là năm mà đội Chicago Bulls kết thúc mùa giải thường lệ với kỷ lục 72 chiến thắng, Michael đã mang đôi giày Jordan 11 trong chuỗi trận thắng đó, và phiên bản phát hành vào năm 2017 nhằm tôn vinh đội hình 96 bất khả chiến bại.', 'Tinker Hatfield', 'https://image.goat.com/750/attachments/product_template_pictures/images/008/870/353/original/235806_00.png.png', 'Air Jordan 11 Retro Win Like 96', 3500000, 16, 5, NOW(), NOW()),
('Air Jordan', 'basketball', 'Air Jordan 11 Retro Legend Blue 2014 lấy cảm hứng từ Jordan 11 Columbia năm 1996 được Jordan mặc lần đầu trong trận đấu NBA All-Star năm 1996.', 'Tinker Hatfield', 'https://image.goat.com/750/attachments/product_template_pictures/images/010/223/048/original/13607_00.png.png', 'Air Jordan 11 Retro Legend Blue 2014', 4500000, 16, 5, NOW(), NOW()),
('Nike', 'lifestyle', 'Cuộc thi On Air năm 2018, người chiến thắng Gwang Shin đã ra mắt giày thể thao Air Max 97 On Air: Neon để bày tỏ sự ngưỡng mộ đối với thành phố Seoul của mình.', 'Gwang Shin', 'https://image.goat.com/750/attachments/product_template_pictures/images/020/627/570/original/491891_00.png.png', 'Air Max 97 On Air: Neon Seoul', 1500000, 15, 5, NOW(), NOW()),
('Nike', 'lifestyle', 'Off-White x Air Max 90 "Black" mang đến sự pha trộn độc đáo giữa các chất liệu kết hợp phần đế bằng ripstop phủ lớp nubuck cùng thiết kế da lộn', 'Jerry Lorenz', 'https://image.goat.com/750/attachments/product_template_pictures/images/012/750/761/original/351623_00.png.png', 'OFF-WHITE x Air Max 90 Black', 7500000, 15, 5, NOW(), NOW()),
('Adidas', 'lifestyle', 'Đôi giày thể thao này có phần trên màu trắng và xám trung tính, dòng chữ SPLY-350 màu đỏ ở mặt sau. Giày cũng đi kèm với một miếng dán ở gót chân, lớp lót bên trong tông màu xanh lam.', 'Kanye West', 'https://image.goat.com/750/attachments/product_template_pictures/images/021/147/972/original/504187_00.png.png', 'Yeezy Boost 700 V2 Vanta', 5500000, 15, 5, NOW(), NOW()),
('Adidas', 'lifestyle', 'Đôi giày Yeezy Boost 350 V2 Beluga 2.0 có sọc xám mờ ở hai bên thay vì sọc cam sáng như trên phiên bản ban đầu của giày thể thao Beluga. Ngoài ra, nó còn được trang bị tab kéo gót với đường khâu màu cam và chữ SPLY-350 màu cam ngược ở hai bên.', 'Kanye West', 'https://image.goat.com/750/attachments/product_template_pictures/images/008/654/534/original/152982_00.png.png', 'Yeezy Boost 350 V2 Beluga 2.0', 4599000, 16, 5, NOW(), NOW()),
('Adidas', 'lifestyle', 'Đôi giày Yeezy Boost 350 V2 Sesame mang một bảng màu tinh tế phối hợp hoàn hảo với thiết kế tối giản của đôi giày. Phần trên Primeknit thoáng khí giữ nguyên tab gót và chi tiết may trung tâm đặc trưng, nhưng không còn chữ SPLY-350 được phản chiếu', 'Kanye West', 'https://image.goat.com/750/attachments/product_template_pictures/images/014/507/851/original/195483_00.png.png', 'Yeezy Boost 350 V2 Sesame', 5600000, 16, 5, NOW(), NOW()),
('Nike', 'running', 'Air Max 97 Triple White có phần trên bằng da trắng và lưới với điểm nhấn màu Wolf Grey. Ra mắt vào tháng 8 năm 2017, đôi giày là một trong những thiết kế của bộ sưu tập Air Max.', 'Christian Tresser', 'https://image.goat.com/750/attachments/product_template_pictures/images/021/321/832/original/503571_00.png.png', 'Air Max 97 Triple White', 5600000, 16, 5, NOW(), NOW()),
('Nike', 'running', 'Ra mắt vào tháng 3 năm 2018, Air Max 270 White lấy cảm hứng từ cả Air Max 180 và Air Max 93. Phần trên bằng lưới trắng được nhấn nhá bằng những điểm màu xám trên vòng kéo gót cùng logo Swoosh trên đầu ngón chân và bên hông', 'Christian Tresser', 'https://image.goat.com/750/attachments/product_template_pictures/images/010/634/133/original/303217_00.png.png', 'Air Max 270', 1999999, 16, 5, NOW(), NOW()),
('Adidas', 'running', 'Thiết kế được trang bị phần trên bằng chất liệu Primeknit đặc trưng của adidas, đôi giày phong cách này có màu vàng sáng năng động, chạy dọc từ dây buộc giày, vòng kéo gót đến phần đế cao su với công nghệ Boost.', 'Kanye West', 'https://image.goat.com/750/attachments/product_template_pictures/images/016/928/118/original/155573_00.png.png', 'Yeezy Boost 350 V2 Semi Frozen Yellow', 7800000, 16, 5, NOW(), NOW()),
('Converse', 'lifestyle', 'Phiên bản Comme des Garçons x Chuck Taylor All Star Hi này có phần trên bằng vải màu kem nhạt, logo tim CDG màu đỏ ở các bên hông, dải đối lập màu đen trên gót, đầu giày màu trắng và đế giữa bằng cao su.', 'Marquis Mills', 'https://image.goat.com/750/attachments/product_template_pictures/images/015/298/767/original/77243_00.png.png', 'Comme des Garçons x Chuck Taylor All Star Hi Milk', 5600000, 16, 5, NOW(), NOW()),
('Converse', 'lifestyle', 'Phiên bản Artist Series của Converse Chuck 70, với phần trên bằng vải màu kem nhạt được in họa tiết gốc từ Wyatt Navarro. Phần viền đặc trưng của dáng giày được tăng cao và được trang trí với các dải tương phản màu xanh và cam.', 'Marquis Mills', 'https://image.goat.com/750/attachments/product_template_pictures/images/018/552/840/original/476518_00.png.png', 'Tyler, The Creator x Foot Locker x Chuck 70 Artist Series', 1000000, 20, 5, NOW(), NOW());

-- Grant privileges
GRANT ALL PRIVILEGES ON `db-auth`.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON `db-product`.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON `db-cart`.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON `db-invoice`.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON `db-payment`.* TO 'root'@'%';

FLUSH PRIVILEGES;