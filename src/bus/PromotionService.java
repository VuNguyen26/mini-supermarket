package bus;

import dal.dao.PromotionDAO;
import dal.dao.PromotionProductDAO;
import dto.Promotion;
import dto.PromotionProduct;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class PromotionService {
    public interface PromotionChangeListener {
        void onPromotionChanged();
    }

    private static final List<PromotionChangeListener> LISTENERS = new CopyOnWriteArrayList<>();

    private final PromotionDAO promotionDAO = new PromotionDAO();
    private final PromotionProductDAO promotionProductDAO = new PromotionProductDAO();

    public static void addPromotionChangeListener(PromotionChangeListener listener) {
        if (listener != null) {
            LISTENERS.add(listener);
        }
    }

    public static void removePromotionChangeListener(PromotionChangeListener listener) {
        LISTENERS.remove(listener);
    }

    private static void notifyPromotionChanged() {
        for (PromotionChangeListener listener : LISTENERS) {
            try {
                listener.onPromotionChanged();
            } catch (Exception ignored) {
            }
        }
    }

    public List<Promotion> getAll(String searchTxt){
        return promotionDAO.findAll(searchTxt);
    }

    public List<Promotion> getApplicablePromotions(double orderAmount) {
        return getApplicablePromotions(orderAmount, null);
    }

    public List<Promotion> getApplicablePromotions(double orderAmount, List<Integer> productIds) {
        List<Promotion> all = promotionDAO.findAll("");
        List<Promotion> result = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        BigDecimal amount = BigDecimal.valueOf(Math.max(0, orderAmount));
        Set<Integer> productIdSet = new HashSet<>();
        if (productIds != null) {
            for (Integer productId : productIds) {
                if (productId != null && productId > 0) {
                    productIdSet.add(productId);
                }
            }
        }

        for (Promotion promotion : all) {
            if (promotion == null) {
                continue;
            }

            String status = promotion.getStatus();
            if (status == null || !"ACTIVE".equalsIgnoreCase(status.trim())) {
                continue;
            }

            LocalDateTime startAt = promotion.getStartAt();
            if (startAt != null && startAt.isAfter(now)) {
                continue;
            }

            LocalDateTime endAt = promotion.getEndAt();
            if (endAt != null && endAt.isBefore(now)) {
                continue;
            }

            BigDecimal minOrderAmount = promotion.getMinOrderAmount();
            // Với POS lọc theo sản phẩm (productIds có dữ liệu), ưu tiên rule theo sản phẩm.
            // Không chặn bởi min_order_amount để mã theo sản phẩm vẫn áp được.
            if (minOrderAmount != null && amount.compareTo(minOrderAmount) < 0 && productIdSet.isEmpty()) {
                continue;
            }

            if (!productIdSet.isEmpty()) {
                List<PromotionProduct> promotionProducts = promotionProductDAO.findProductByPromotionId(promotion.getPromoId());
                Set<Integer> promotionProductIds = new HashSet<>();
                for (PromotionProduct promotionProduct : promotionProducts) {
                    if (promotionProduct != null && promotionProduct.getProductId() > 0) {
                        promotionProductIds.add(promotionProduct.getProductId());
                    }
                }

                // Chỉ cần giỏ có ít nhất 1 sản phẩm thuộc danh sách khuyến mãi
                boolean hasAnyMatchedProduct = false;
                for (Integer productId : productIdSet) {
                    if (promotionProductIds.contains(productId)) {
                        hasAnyMatchedProduct = true;
                        break;
                    }
                }
                if (!hasAnyMatchedProduct) {
                    continue;
                }
            }

            result.add(promotion);
        }

        return result;
    }

    public Promotion getById(int promoId){
        if (promoId <= 0) {
            throw new IllegalArgumentException("Mã khuyến mãi không hợp lệ");
        }

        return promotionDAO.findById(promoId);
    }

    public List<PromotionProduct> getByPromotionId(int promoId){
        if (promoId <= 0) {
            throw new IllegalArgumentException("Mã khuyến mãi không hợp lệ");
        }

        return promotionProductDAO.findProductByPromotionId(promoId);
    }
    
    public int add(Promotion promo){
        if (promo == null)
            throw new IllegalArgumentException("Promotion is null");

        if (promo.getPromoCode() == null || promo.getPromoCode().isBlank())
            throw new IllegalArgumentException("Promo code is required");

        if (promo.getStartAt() != null && promo.getEndAt() != null && promo.getStartAt().isAfter(promo.getEndAt()))
            throw new IllegalArgumentException("Start date must be before end date");

        if (promo.getMinOrderAmount().compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Min Order Amount must be greater than 0");

        int id = promotionDAO.insert(promo);
        if (id > 0) {
            notifyPromotionChanged();
        }
        return id;
    }

    public boolean update(Promotion promo) {
        if (promo == null)
            throw new IllegalArgumentException("Promotion is null");

        if (promo.getPromoCode() == null || promo.getPromoCode().isBlank())
            throw new IllegalArgumentException("Promo code is required");

        if (promo.getType() == null)
            throw new IllegalArgumentException("Promotion type is required");

        if (promo.getMinOrderAmount() == null)
            throw new IllegalArgumentException("Min order amount is required");

        if (promo.getMinOrderAmount().compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Min order must be greater than 0");

        if (promo.getStartAt() != null && promo.getEndAt() != null && promo.getStartAt().isAfter(promo.getEndAt()))
            throw new IllegalArgumentException("Start date must be before end date");

        boolean updated = promotionDAO.update(promo);
        if (updated) {
            notifyPromotionChanged();
        }
        return updated;
    }

    public void delete(int promoId){
        if (promoId <= 0) {
            throw new IllegalArgumentException("Id không hợp lệ");
        }

        promotionDAO.delete(promoId);
        notifyPromotionChanged();
    }

    public int addProduct(PromotionProduct pp){
        if(pp.getPromoId() <= 0){
            throw new IllegalArgumentException("Promotion Id is not valid!");
        }

        if(pp.getProductId() <= 0){
            throw new IllegalArgumentException("Product Id is not valid!");
        }

        return promotionProductDAO.insert(pp);
    }

    public boolean updateProduct(PromotionProduct pp){

        if(pp.getPpId() <= 0){
            throw new IllegalArgumentException("Promotion_product Id is not valid!");
        }

        return promotionProductDAO.update(pp);
    }

    public void deleteProduct(int ppId){

        if(ppId <= 0){
            throw new IllegalArgumentException("Promotion_product Id is not valid!");
        }

        promotionProductDAO.delete(ppId);
    }

    public PromotionProduct getPPById(int ppId){

        if(ppId <= 0){
            throw new IllegalArgumentException("Promotion_product Id is not valid!");
        }

        return promotionProductDAO.findById(ppId);
    }

}