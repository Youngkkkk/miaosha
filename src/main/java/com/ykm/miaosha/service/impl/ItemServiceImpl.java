package com.ykm.miaosha.service.impl;

import com.ykm.miaosha.dataobject.ItemDO;
import com.ykm.miaosha.dataobject.ItemStockDO;
import com.ykm.miaosha.error.BusinessException;
import com.ykm.miaosha.error.EmBusinessError;
import com.ykm.miaosha.mapper.ItemDOMapper;
import com.ykm.miaosha.mapper.ItemStockDOMapper;
import com.ykm.miaosha.service.ItemService;
import com.ykm.miaosha.service.PromoService;
import com.ykm.miaosha.service.dto.ItemDTO;
import com.ykm.miaosha.service.dto.PromoDTO;
import com.ykm.miaosha.validator.ValidationResult;
import com.ykm.miaosha.validator.ValidatorImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Author ykm
 * @Date 2019/10/21 16:06
 */
@Service
public class ItemServiceImpl implements ItemService {
    @Autowired
    private ItemDOMapper itemDOMapper;
    @Autowired
    private ItemStockDOMapper itemStockDOMapper;
    @Autowired
    private PromoService promoService;
    @Autowired
    private ValidatorImpl validator;

    private ItemDO convertItemDOFromItemDTO(ItemDTO itemDTO) {
        if (itemDTO == null) {
            return null;
        }
        ItemDO itemDO = new ItemDO();
        BeanUtils.copyProperties(itemDTO, itemDO);
        itemDO.setPrice(itemDTO.getPrice().doubleValue());
        return itemDO;
    }

    private ItemStockDO convertItemStockDOFromItemDTO(ItemDTO itemDTO) {
        if (itemDTO == null) {
            return null;
        }
        ItemStockDO itemStockDO = new ItemStockDO();
        itemStockDO.setItemId(itemDTO.getId());
        itemStockDO.setStock(itemDTO.getStock());
        return itemStockDO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ItemDTO createItem(ItemDTO itemDTO) throws BusinessException {
        // 校验入参
        ValidationResult result = validator.validate(itemDTO);
        if (result.isHasErrors()) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, result.getErrMsg());
        }
        ItemDO itemDO = convertItemDOFromItemDTO(itemDTO);
        itemDOMapper.insertSelective(itemDO);
        itemDTO.setId(itemDO.getId());
        ItemStockDO itemStockDO = convertItemStockDOFromItemDTO(itemDTO);
        itemStockDOMapper.insertSelective(itemStockDO);
        // 返回创建的对象
        return this.getItemById(itemDTO.getId());
    }

    @Override
    public List<ItemDTO> listItem() {
        List<ItemDO> itemDOList = itemDOMapper.listItem();
        List<ItemDTO> itemDTOList = itemDOList.stream().map(itemDO -> {
            ItemStockDO itemStockDO = itemStockDOMapper.selectByItemId(itemDO.getId());
            ItemDTO itemDTO = convertModelFromDataObject(itemDO, itemStockDO);
            return itemDTO;
        }).collect(Collectors.toList());
        return itemDTOList;
    }

    @Override
    public ItemDTO getItemById(Integer id) {
        ItemDO itemDO = itemDOMapper.selectByPrimaryKey(id);
        if (itemDO == null) {
            return null;
        }
        // 操作获取库存数量
        ItemStockDO itemStockDO = itemStockDOMapper.selectByItemId(itemDO.getId());
        // 模型转化
        ItemDTO itemDTO = convertModelFromDataObject(itemDO, itemStockDO);
        // 获取活动商品信息
        PromoDTO promoDTO = promoService.getPromoByItemId(itemDTO.getId());
        if (promoDTO != null && promoDTO.getStatus().intValue() != 3) {
            itemDTO.setPromoDTO(promoDTO);
        }
        return itemDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean decreaseStock(Integer itemId, Integer amount) throws BusinessException {
        int row = itemStockDOMapper.decreaseStock(itemId, amount);
        return row > 0;
    }

    private ItemDTO convertModelFromDataObject(ItemDO itemDO, ItemStockDO itemStockDO) {
        ItemDTO itemDTO = new ItemDTO();
        BeanUtils.copyProperties(itemDO, itemDTO);
        itemDTO.setPrice(new BigDecimal(itemDO.getPrice()));
        itemDTO.setStock(itemStockDO.getStock());

        return itemDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void increaseSales(Integer itemId, Integer amount) throws BusinessException {
        itemDOMapper.increaseSales(itemId, amount);
    }
}
