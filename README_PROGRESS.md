# Mini Supermarket - Tiến độ Phát triển

## Tổng quát

Hệ thống quản lý bán hàng và kho cho siêu thị mini, xây dựng bằng Java Swing với kiến trúc 3 tầng.

## Phần 2: Quản lý Master Data

### Tính năng đã hoàn thành

#### 1. Quản lý Danh mục (Category)

**Chức năng:**
- Thêm, sửa, xóa danh mục
- Tìm kiếm theo mã danh mục (category_code) và tên (category_name)
- Lọc theo trạng thái (ACTIVE/INACTIVE)
- Validate form dữ liệu đầu vào

**File liên quan:**
- Service: `bus/CategoryService.java`
- DAO: `dal/dao/CategoryDAO.java`
- DTO: `dto/Category.java`
- Dialog: `presentation/dialogs/CategoryDialog.java`
- Panel: `presentation/panels/CategoryPanel.java`

**Database:**
- Bảng: `category`
- Cột: category_id, category_code, category_name, description, status, created_at, updated_at

---

#### 2. Quản lý Thương hiệu (Brand)

**Chức năng:**
- Thêm, sửa, xóa thương hiệu
- Tìm kiếm theo tên thương hiệu
- Quản lý trạng thái (ACTIVE/INACTIVE)
- Hỗ trợ datetime tracking (created_at, updated_at)

**Đặc điểm:**
- Compatible với cấu trúc DB hiện tại
- DAO sử dụng helper method `hasColumn()` để xử lý cột tùy chọn
- Query INSERT/UPDATE chỉ sử dụng: brand_name, status
- Tự động set updated_at = CURRENT_TIMESTAMP khi update

**File liên quan:**
- Service: `bus/BrandService.java`
- DAO: `dal/dao/BrandDAO.java` (có helper `hasColumn()`)
- DTO: `dto/Brand.java`
- Dialog: `presentation/dialogs/BrandDialog.java` (tương tự CategoryDialog)

**Database:**
- Bảng: `brand`
- Cột: brand_id, brand_name, status, created_at, updated_at

**Fix lỗi:**
- Ban đầu BrandDAO cố gọi brand_code, description -> lỗi Column not found
- Sửa: INSERT chỉ ghi (brand_name, status), UPDATE chỉ cập (brand_name, status, updated_at)
- Thêm helper `hasColumn()` để guard khi mapping ResultSet

---

#### 3. Quản lý Sản phẩm (Product)

**Chức năng:**
- Thêm, sửa, xóa sản phẩm
- Mã sản phẩm (barcode) là unique key, bắt buộc nhập
- Liên kết bắt buộc với Danh mục
- Liên kết tùy chọn với Thương hiệu
- Quản lý tồn kho (stock_qty, min_stock)
- Quản lý giá (import_price, sale_price)
- Tìm kiếm bằng barcode hoặc tên sản phẩm
- Lọc theo danh mục, thương hiệu, trạng thái

**File liên quan:**
- Service: `bus/ProductService.java`
- DAO: `dal/dao/ProductDAO.java` (JOIN với category, brand)
- DTO: `dto/Product.java` (thêm field imagePath tạm)
- Dialog: `presentation/dialogs/ProductDialog.java`
- Panel: `presentation/panels/ProductPanel.java`

**Database:**
- Bảng: `product`
- Cột: product_id, barcode (unique), product_name, category_id (FK), brand_id (FK, nullable), unit, import_price, sale_price, stock_qty, min_stock, status, created_at, updated_at

**ProductDialog cải tiến:**
- Size: 850x650 (từ 750x600)
- GridBagLayout 3 cột:
  - Cột 0: Mã SP, Tên SP, Brand, Đơn vị, Danh mục, Trạng thái, Giá nhập, Giá bán, Tồn kho, Tồn tối thiểu
  - Cột 1-2: Image panel (chiếm 3 row)
- Scroll support: wrap mainPanel trong JScrollPane
- Brand combo: load danh sách brand từ DB, hiển thị tên, lưu brand_id

---

#### 4. Quản lý Ảnh Sản phẩm (ProductImage)

**Tính năng chính:**
- Hỗ trợ NHIỀU ảnh cho một sản phẩm
- Giao diện scroll list để quản lý danh sách ảnh
- Mỗi ảnh có:
  - Thumbnail 70x70px preview
  - Tên file
  - Checkbox chọn ảnh chính (is_primary)
  - Button Xóa

**Quy trình:**
1. Click "+ Thêm ảnh" mở JFileChooser
2. Filter file: JPG, JPEG, PNG, GIF
3. Preview ảnh trong thumbnail
4. Ảnh đầu tiên tự động là ảnh chính
5. Có thể chọn ảnh khác làm ảnh chính bằng checkbox
6. Xóa ảnh bằng button Xóa
7. Khi lưu sản phẩm:
   - New product: lưu product trước, sau đó lưu các ảnh mới vào DB
   - Edit product: lưu các ảnh mới, update is_primary nếu thay đổi

**File liên quan:**
- DAO: `dal/dao/ProductImageDAO.java`
- DTO: `dto/ProductImage.java` (field: imageUrl, isPrimary)
- Dialog: ProductDialog (phần tạo image panel)

**Database:**
- Bảng: `product_image`
- Cột: image_id, product_id (FK), image_url (lưu đường dẫn URL hoặc local path), is_primary (boolean), created_at
- Index: idx_product (product_id)

**ProductImageDAO:**
- `findByProductId()` - Lấy tất cả ảnh của sản phẩm
- `findPrimaryImage()` - Lấy ảnh chính
- `insert()` - Thêm ảnh mới
- `setPrimaryImage()` - Set ảnh chính (transaction)
- `delete()` - Xóa 1 ảnh
- `deleteByProductId()` - Xóa tất cả ảnh sản phẩm
- `mapResultSet()` - Map kết quả

**ProductImage DTO:**
- Getter/Setter: getImageUrl(), setImageUrl() (không phải imagePath)
- Constructor

**ProductDialog - Image Panel UI:**
- Scroll list (JScrollPane 250x200)
- Mỗi item là JPanel BorderLayout:
  - WEST: Thumbnail
  - CENTER: File name + checkbox ảnh chính
  - EAST: Button Xóa
- GridBagLayout: image panel ở gridx=1, gridy=0, gridheight=3, fill=BOTH

**Fix lỗi:**
- Ban đầu schema file dùng image_path, nhưng DB thực tế dùng image_url
- Sửa tất cả reference: getImagePath() -> getImageUrl(), setImagePath() -> setImageUrl()
- INSERT/SELECT dùng cột image_url

---

#### 5. Quản lý Nhà cung cấp (Supplier)

**Chức năng:**
- Thêm, sửa, xóa nhà cung cấp
- Quản lý thông tin: tên, mã, liên hệ (phone, email)
- Hỗ trợ địa chỉ chi tiết

**File liên quan:**
- Service: `bus/SupplierService.java`
- DAO: `dal/dao/SupplierDAO.java`
- DTO: `dto/Supplier.java`
- Dialog: `presentation/dialogs/SupplierDialog.java`

**Database:**
- Bảng: `supplier`
- Cột: supplier_id, supplier_code, supplier_name, contact_person, phone, email, address, status, created_at, updated_at

---

#### 6. Quản lý Khách hàng (Customer)

**Chức năng:**
- Thêm, sửa, xóa khách hàng
- Quản lý thông tin: tên, SĐT, email, địa chỉ
- Theo dõi lịch sử mua hàng
- Chuẩn bị cho chương trình khách hàng thân thiết

**File liên quan:**
- Service: `bus/CustomerService.java`
- DAO: `dal/dao/CustomerDAO.java`
- DTO: `dto/Customer.java`
- Dialog: `presentation/dialogs/CustomerDialog.java`

**Database:**
- Bảng: `customer`
- Cột: customer_id, customer_code, customer_name, phone, email, address, date_of_birth, status, created_at, updated_at

---

### Cải tiến kỹ thuật

#### Layout & UI
- ProductDialog: Từ 750x600 -> 850x650 để có chỗ hiển thị image panel
- GridBagLayout: 3 cột chính, image panel chiếm gridheight=3
- JScrollPane: Scroll list ảnh sản phẩm, wrap mainPanel để scroll form dài
- Responsive: Tất cả component dùng MaximumSize, PreferredSize hợp lý

#### DAO Pattern
- CRUD đầy đủ: findAll, findById, insert, update, delete
- Search: findByName/findByCode
- Filter: filter() với điều kiện động
- MapResultSet: Map ResultSet -> DTO
- Exception handling: RuntimeException với message chi tiết

#### Service Layer
- Business logic: Validation, transform, coordination
- Service methods: getAll(), create(), update(), delete(), search()
- Exception propagation

#### Helper Methods
- BrandDAO.hasColumn(): Kiểm tra cột tồn tại trước khi read
- ProductDialog.createImageItem(): Render mỗi ảnh trong list
- ProductDialog.refreshImageList(): Update lại giao diện list

---

### Bug fixes trong quá trình phát triển

1. **BrandDAO Column not found**
   - Lỗi: INSERT vào brand_code, description nhưng cột không tồn tại
   - Fix: Chỉ ghi (brand_name, status) vào DB

2. **ProductImageDAO Column not found**
   - Lỗi: Gọi cột image_path nhưng DB dùng image_url
   - Fix: Đổi tất cả reference từ imagePath -> imageUrl

3. **BrandDAO findByName**
   - Ban đầu: SELECT chỉ 2 cột (brand_id, brand_name)
   - Fix: SELECT * để full mapping

4. **ProductDialog layout**
   - Lỗi: Image panel không hiển thị
   - Fix: Set gridheight=3, fill=BOTH, size tăng lên 850x650

5. **ProductDialog Brand field duplicate**
   - Lỗi: Thêm 2 lần field Brand ở cùng row
   - Fix: Xóa dòng trùng lặp, sắp xếp rows đúng

6. **ProductDialog scroll**
   - Lỗi: Form dài không scroll được
   - Fix: Wrap mainPanel trong JScrollPane

---

### Cấu trúc code tuân theo

- **Package Structure:**
  - `app/`: Entry point
  - `presentation/`: UI (dialogs, panels, menu)
  - `bus/`: Business logic
  - `dal/`: Database access (DAO)
  - `dto/`: Data transfer objects
  - `util/`: Utilities

- **Naming Convention:**
  - DAO: `{Entity}DAO`
  - Service: `{Entity}Service`
  - DTO: `{Entity}`
  - Dialog: `{Entity}Dialog`
  - Panel: `{Entity}Panel`

- **Database Naming:**
  - Table: snake_case (product, product_image)
  - Column: snake_case (product_id, brand_name)
  - FK: {entity}_id
  - Index: idx_{field}

---

### Tiếp theo

#### Phần 3: Quản lý Nhập kho
- GoodsReceipt (phiếu nhập)
- GoodsReceiptDetail (chi tiết)
- InventoryLot (lô hàng)

#### Phần 4: Quản lý Kho
- StockAdjustment (điều chỉnh tồn)
- Low stock alerts
- Batch tracking

#### Phần 5: Quản lý Bán hàng
- SalesInvoice
- SalesInvoiceDetail
- Payment (thanh toán)
- Promotion (khuyến mãi)

#### Phần 6: Báo cáo
- Revenue reports
- Product statistics
- Inventory reports

---

## Công nghệ

- Java SE 21
- Java Swing (UI)
- JDBC (Database)
- MySQL 8.0+
- GridBagLayout, BoxLayout
- ImageIO (Xử lý ảnh)

---

## Chạy chương trình

1. Import schema: `mysql -u root -p < database_schema.sql`
2. Update DBConnection.java (URL, USER, PASSWORD)
3. Run Main.java

Tài khoản mặc định: admin / admin123

---

## Ghi chú

- Tất cả UI không sử dụng icon/emoji, chỉ text và button style
- Scroll support cho form dài
- GridBagLayout tương thích với dynamic content
- DAO có error handling chi tiết
- Validate form trước lưu DB
- Support file ảnh: JPG, JPEG, PNG, GIF
