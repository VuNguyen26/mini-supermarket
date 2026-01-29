package bus;

import dal.dao.PromotionDAO;
import dal.dao.PromotionProductDAO;
import dto.Promotion;
import dto.PromotionProduct;

import java.math.BigDecimal;
import java.util.List;

public class PromotionService {
    private final PromotionDAO promotionDAO = new PromotionDAO();
    private final PromotionProductDAO promotionProductDAO = new PromotionProductDAO();

    public List<Promotion> getAll(String searchTxt){
        return promotionDAO.findAll(searchTxt);
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

        return promotionDAO.insert(promo);
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

        return promotionDAO.update(promo);
    }

    public void delete(int promoId){
        if (promoId <= 0) {
            throw new IllegalArgumentException("Id không hợp lệ");
        }

        promotionDAO.delete(promoId);
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