package com.lijingyao.microservice.coffee.item.service.manager;

import com.lijingyao.microservice.coffee.item.persistence.vo.AdditionalTasteVO;
import com.lijingyao.microservice.coffee.item.persistence.vo.CaffeineEnum;
import com.lijingyao.microservice.coffee.item.persistence.vo.MilkEnums;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Diff season,additional price's policy
 * <p>
 * Created by lijingyao on 2018/8/7 14:18.
 */
@Component
public class WinterPricePolicy implements PriceStrategy {

    private static final int ESPRESSO_PRICE = 5;
    private static final int MILK_PRICE = 3;
    private static final int DECAFE_PRICE = 2;


    @Override
    public long calculatePrice(AdditionalTasteVO tasteVO) {
        if (tasteVO == null) return 0l;
        final AtomicLong price = new AtomicLong(0l);
        if (tasteVO.getEspresso() > 0) {
            price.addAndGet(tasteVO.getEspresso() * ESPRESSO_PRICE);
        }
        if (tasteVO.getCoffeine() != null && tasteVO.getCoffeine().equals(CaffeineEnum.DE_CAFE)) {
            price.addAndGet(DECAFE_PRICE);
        }
        if (tasteVO.getMilk() != null && tasteVO.getMilk().equals(MilkEnums.MORE)) {
            price.addAndGet(MILK_PRICE);
        }
        return price.get();
    }

    @Override
    public boolean seasonMatch() {
        LocalDateTime localDateTime = LocalDateTime.now();
        if (3 <= localDateTime.getDayOfMonth() && localDateTime.getDayOfMonth() <= 9) {
            return false;
        }
        return true;
    }
}
