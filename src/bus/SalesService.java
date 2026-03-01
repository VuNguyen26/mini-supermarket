package bus;

import dal.DBConnection;
import dal.dao.InventoryLotDAO;
import dal.dao.SalesInvoiceDAO;
import dal.dao.SalesInvoiceDetailDAO;
import dal.dao.PaymentDAO;
import dto.InventoryLot;
import dto.SalesInvoice;
import dto.SalesInvoiceDetail;
import dto.Payment;

import java.sql.Connection;
import java.sql.Timestamp;
import java.sql.SQLException;
import java.util.List;

public class SalesService {
    private final SalesInvoiceDAO invoiceDAO = new SalesInvoiceDAO();
    private final SalesInvoiceDetailDAO detailDAO = new SalesInvoiceDetailDAO();
    private final InventoryLotDAO lotDAO = new InventoryLotDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();
    
    public List<SalesInvoice> getAllInvoices() throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            return invoiceDAO.getAllInvoices(conn);
        }
    }

    public List<SalesInvoice> searchInvoices(Integer invId, String customerName,
                                            Timestamp fromCreated, Timestamp toCreated) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            return invoiceDAO.searchInvoices(invId, customerName, fromCreated, toCreated, conn);
        }
    }

    public SalesInvoice getInvoiceById(int invId) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            return invoiceDAO.getInvoiceById(invId, conn);
        }
    }

    public List<SalesInvoiceDetail> getInvoiceDetailsByInvId(int invId) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            return detailDAO.getDetailsByInvoiceId(invId, conn);
        }
    }

    public boolean processSale(SalesInvoice invoice, List<SalesInvoiceDetail> requestDetails, Payment payment) throws SQLException {
        Connection conn = DBConnection.getConnection();
        
        if (conn == null) {
            // System.out.println("Cant connect to Database!");
            return false;
        }

        try {
            conn.setAutoCommit(false);
            // System.out.println("START TRANSACTION");
            int newInvoiceId = invoiceDAO.createInvoice(invoice, conn);
            // System.out.println("Created new Sales Invoice with ID" + newInvoiceId);
            invoice.setInvId(newInvoiceId);
            if (newInvoiceId == -1) {
                throw new SQLException("Cant create new Sales Invoice record!");
            }

            for (SalesInvoiceDetail itemReq : requestDetails) {
                int productId = itemReq.getProductId();
                int qtyNeeded = itemReq.getQty();
                double unitPrice = itemReq.getUnitPrice();
                // System.out.println("Proccessing Product ID" + productId + " | Needed: " + qtyNeeded);
                List<InventoryLot> availableLots = lotDAO.getAvailableLotsByProductForUpdate(productId, conn);
                // System.out.println("Found " + availableLots.size() + " available lots for Product ID " + productId);

                int qtyCollected = 0; 

                for (InventoryLot lot : availableLots) {
                    if (qtyCollected >= qtyNeeded) break; 
                    int qtyInLot = lot.getQtyRemaining();
                    int qtyToTake = Math.min(qtyInLot, qtyNeeded - qtyCollected); 
                    // System.out.println(" Take " + qtyToTake + " from lotId " + lot.getLotId());
                    boolean ok = lotDAO.decreaseQtyRemaining(lot.getLotId(), qtyToTake, conn);
                    if (!ok) {
                        throw new SQLException("Concurrent update detected for lotId=" + lot.getLotId() + ". Please retry.");
                    }
                    SalesInvoiceDetail dbDetail = new SalesInvoiceDetail();
                    dbDetail.setInvId(newInvoiceId);
                    dbDetail.setProductId(productId);
                    dbDetail.setLotId(lot.getLotId()); 
                    dbDetail.setQty(qtyToTake);
                    dbDetail.setUnitPrice(unitPrice);
                    dbDetail.setLineTotal(qtyToTake * unitPrice);
                    detailDAO.createDetail(dbDetail, conn);
                    qtyCollected += qtyToTake;
                }

                if (qtyCollected < qtyNeeded) {
                    // System.out.println("Error: Not enough inventory for Product ID " + productId + ". Needed: " + qtyNeeded + " but only have " + qtyCollected);
                    throw new SQLException("Product ID" + productId + " out of stock. Required " + qtyNeeded + ", Available: " + qtyCollected + ")");
                }
            }

            payment.setInvId(newInvoiceId);
            paymentDAO.createPayment(payment, conn);
            conn.commit(); 
            // System.out.println("COMMIT TRANSACTION | SAVED TO DATABASE");
            return true;

        } catch (SQLException e) {
            try {
                conn.rollback(); 
                // System.err.println("TRANSACTION FAILED -> ROLLED BACK");
                System.err.println("Error reason: " + e.getMessage());
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace(); 
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}