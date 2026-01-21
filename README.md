# mini-supermarket
### Need scheme update (new feature):
- New table: bankconfig
    - Columns: bank_id, account_number, account_name, template
    - Purpose: To store banking details for QR generation or transfer payments
### Bug fix: data type inconsistency
- Issue: The payment_method in sales_invoice is missing the EWALLET option found in the payment table
- Fix: Synchronize sales_invoice.payment_method to match payment.method
- Updated Definition: enum('CASH', 'CARD', 'TRANSFER', 'EWALLET') NOT NULL DEFAULT 'CASH'