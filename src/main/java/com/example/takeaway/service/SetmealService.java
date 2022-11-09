package com.example.takeaway.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.takeaway.dto.SetmealDto;
import com.example.takeaway.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {

    /**
     * 新增套餐，同时保存套餐和菜品的关联关系
     * @param setmealDto
     */
    void saveWithDish(SetmealDto setmealDto);

    /**
     * 删除套餐，需删除关联菜品
     * @param ids
     */
    void removeWithDish(List<Long> ids);

    /**
     * 根据id来查询套餐信息和对应的菜品信息
     * @param id
     * @return
     */
    SetmealDto getByIdWithDish(Long id);

    /**
     * 更新套餐信息，同时更新对应的菜品信息
     * @param setmealDto
     */
    void updateWithDish(SetmealDto setmealDto);

}
