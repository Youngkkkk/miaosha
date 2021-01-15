package com.ykm.miaosha.controller;

import com.ykm.miaosha.controller.viewobject.ItemVO;
import com.ykm.miaosha.dataobject.ItemDO;
import com.ykm.miaosha.error.BusinessException;
import com.ykm.miaosha.response.CommonReturnType;
import com.ykm.miaosha.service.ItemService;
import com.ykm.miaosha.service.dto.ItemDTO;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.SimpleFormatter;
import java.util.stream.Collectors;

/**
 * @Author ykm
 * @Date 2019/10/21 17:32
 */
@RestController
@RequestMapping("/item")
@CrossOrigin(allowCredentials = "true", allowedHeaders = "*")
public class ItemController {

    @Autowired
    private ItemService itemService;

    // 创建商品
    @RequestMapping(value = "/create", method = {RequestMethod.POST})
    public CommonReturnType createItem(ItemDTO itemDTO) throws BusinessException {
        ItemDTO item = itemService.createItem(itemDTO);
        ItemVO itemVO = convertVOFromDTO(item);
        return CommonReturnType.create(itemVO);
    }

    //商品详情页浏览
    @RequestMapping(value = "/get", method = {RequestMethod.GET})
    public CommonReturnType getItem(@RequestParam(name = "id") Integer id) {
        ItemDTO itemDTO = itemService.getItemById(id);

        ItemVO itemVO = convertVOFromDTO(itemDTO);

        return CommonReturnType.create(itemVO);

    }

    // 商品列表页面浏览
    @RequestMapping(value = "/list", method = {RequestMethod.GET})
    public CommonReturnType listItem() {
        List<ItemDTO> itemDTOList = itemService.listItem();

        List<ItemVO> itemVOList = itemDTOList.stream().map(itemDTO -> {
            ItemVO itemVO = convertVOFromDTO(itemDTO);
            return itemVO;
        }).collect(Collectors.toList());
        return CommonReturnType.create(itemVOList);
    }

    private ItemVO convertVOFromDTO(ItemDTO itemDTO) {
        if (itemDTO == null) {
            return null;
        }
        ItemVO itemVO = new ItemVO();
        BeanUtils.copyProperties(itemDTO, itemVO);
        if (itemDTO.getPromoDTO() != null) {
            itemVO.setPromoStatus(itemDTO.getPromoDTO().getStatus());
            itemVO.setPromoId(itemDTO.getPromoDTO().getId());
            itemVO.setPromoPrice(itemDTO.getPromoDTO().getPromoItemPrice());
            itemVO.setStartDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(itemDTO.getPromoDTO().getStartDate()));
        } else {
            itemVO.setPromoStatus(0);
        }
        return itemVO;
    }

    @KafkaListener(topics = "sun", groupId = "phoenix-web1")
    public void listen(ConsumerRecord record) {
        System.out.println("record.topic() = " + record.topic());
        System.out.println("record.offset() = " + record.offset());
        System.out.println("record.value() = " + record.value());
    }

    @Autowired
    private KafkaTemplate<String,Object> kafkaTemplate;
    @RequestMapping("message/send")
    public String send(String msg){
        try {
            SendResult<String, Object> sun = kafkaTemplate.send("sun", msg).get();//使用kafka模板发送信息
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        System.out.println(kafkaTemplate.getDefaultTopic());
        return "success";
    }

}
