package com.example.takeaway.dto;

import com.example.takeaway.entity.Setmeal;
import com.example.takeaway.entity.SetmealDish;
import lombok.Data;

import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}