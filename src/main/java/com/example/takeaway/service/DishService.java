package com.example.takeaway.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.takeaway.dto.DishDto;
import com.example.takeaway.entity.Dish;

public interface DishService extends IService<Dish> {
    void saveWithFlavor(DishDto dishDto);
    DishDto getByIdWithFlavor(Long id);

    void updateWithFlavor(DishDto dishDto);
}
