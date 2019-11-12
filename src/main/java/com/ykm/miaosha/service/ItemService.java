package com.ykm.miaosha.service;

import com.ykm.miaosha.error.BusinessException;
import com.ykm.miaosha.service.dto.ItemDTO;

import java.util.List;

/**
 * @Author ykm
 * @Date 2019/10/21 16:02
 */
public interface ItemService {
    // 创建商品
    ItemDTO createItem(ItemDTO itemDTO) throws BusinessException;

    // 商品列表浏览
    List<ItemDTO> listItem();

    // 商品详情浏览
    ItemDTO getItemById(Integer id);

    // 库存扣减
    boolean decreaseStock(Integer itemId, Integer amount) throws BusinessException;

    // 商品销量增加
    void increaseSales(Integer itemId, Integer amount) throws BusinessException;
}
