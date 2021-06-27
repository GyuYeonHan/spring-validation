package hello.itemservice.web.validation;

import hello.itemservice.domain.item.Item;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class ItemValidator implements Validator {

    private static final int ITEM_MIN_PRICE = 1000;
    private static final int ITEM_MAX_PRICE = 1000000;
    private static final int ITEM_MAX_QUANTITY = 9999;
    private static final int ITEM_MIN_TOTAL_PRICE = 10000;

    @Override
    public boolean supports(Class<?> clazz) {
        return Item.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Item item = (Item) target;

        //검증 로직
        if (!StringUtils.hasText(item.getItemName())) {
            errors.rejectValue("itemName", "required");
        }
        if (item.getPrice() == null || item.getPrice() < ITEM_MIN_PRICE || item.getPrice() > ITEM_MAX_PRICE) {
            errors.rejectValue("price", "range", new Object[]{ITEM_MIN_PRICE, ITEM_MAX_PRICE}, null);
        }
        if (item.getQuantity() == null || item.getQuantity() >= ITEM_MAX_QUANTITY) {
            errors.rejectValue("quantity", "max", new Object[]{ITEM_MAX_QUANTITY}, null);
        }

        //특정 필드가 아닌 복합 룰 검증
        if (item.getPrice() != null && item.getQuantity() != null) {
            int resultPrice = item.getPrice() * item.getQuantity();
            if (resultPrice < ITEM_MIN_TOTAL_PRICE) {
                errors.reject("totalPriceMin", new Object[]{ITEM_MIN_TOTAL_PRICE, resultPrice}, null);
            }
        }
    }
}
