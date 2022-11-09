package com.example.takeaway.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.takeaway.common.R;
import com.example.takeaway.dto.SetmealDto;
import com.example.takeaway.entity.Category;
import com.example.takeaway.entity.Setmeal;
import com.example.takeaway.entity.SetmealDish;
import com.example.takeaway.service.CategoryService;
import com.example.takeaway.service.SetmealDishService;
import com.example.takeaway.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/setmeal")
public class SetmealController {

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增套餐
     * @param setmealDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto){
        log.info(setmealDto.toString());
        setmealService.saveWithDish(setmealDto);
        return R.success("新增成功");
    }

    /**
     * 套餐分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        Page<Setmeal> pageinfo = new Page<>(page, pageSize);
        Page<SetmealDto> dtoPage = new Page<>(page, pageSize);

        //构造条件，模糊查询，更新时间排序
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(name != null,Setmeal::getName,name);
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        setmealService.page(pageinfo,queryWrapper);

        BeanUtils.copyProperties(pageinfo,dtoPage,"records");
        List<Setmeal> records = pageinfo.getRecords();

        List<SetmealDto> list = records.stream().map((item)->{
            SetmealDto setmealDto = new SetmealDto();

            BeanUtils.copyProperties(item,setmealDto);
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);

            if (category != null){
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
            }
            return setmealDto;

        }).collect(Collectors.toList());
        dtoPage.setRecords(list);

        return R.success(dtoPage);
    }

    /**
     * 删除套餐
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){
        log.info("ids:{}",ids);
        setmealService.removeWithDish(ids);
        return R.success("删除成功");
    }

    /**
     * 根据id来查询套餐信息和对应的菜品信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<SetmealDto> get(@PathVariable Long id){
        SetmealDto setmealDto = setmealService.getByIdWithDish(id);
        return R.success(setmealDto);
    }

    /**
     * 根据id来查询套餐信息和对应的菜品信息
     * @param setmealDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto){
//        log.info(setmealDto.toString());
//        setmealService.updateWithDish(setmealDto);
//        return R.success("修改套餐成功");
        if (setmealDto==null){
            return R.error("请求异常");
        }
        if (setmealDto.getSetmealDishes()==null){
            return R.error("套餐没有菜品，请添加");
        }
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        //获取到套餐的id
        Long setmealId = setmealDto.getId();
        //构造关联菜品的条件查询
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        //根据套餐id在关联菜品中查询数据，注意这里所做的查询是在数据库中进行查询的
        queryWrapper.eq(SetmealDish::getSetmealId,setmealId);
        //关联菜品先移除掉原始数据库中的数据
        setmealDishService.remove(queryWrapper);
        //为setmeal_dish表填充相关的属性
        //这里我们需要为关联菜品的表前面的字段填充套餐的id
        for(SetmealDish setmealDish:setmealDishes)
        {
            setmealDish.setSetmealId(setmealId);//填充属性值
        }
        //批量把setmealDish保存到setmeal_dish表
        //这里我们保存了我们提交过来的关联菜品数据
        setmealDishService.saveBatch(setmealDishes);//保存套餐关联菜品
        //这里我们正常更新套餐
        setmealService.updateById(setmealDto);//保存套餐
        return R.success("套餐修改成功");
    }

    /**
     * 修改套餐的售卖状态
     *
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> statusWithIds(@PathVariable("status") Integer status, @RequestParam List<Long> ids) {
        //构造一个条件构造器
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ids != null,Setmeal::getId,ids);
        queryWrapper.orderByDesc(Setmeal::getPrice);
        //根据条件进行批量查询
        List<Setmeal> list = setmealService.list(queryWrapper);
        for (Setmeal setmeal : list) {
            if (list != null) {
                //把浏览器传入的status参数复制给套餐
                setmeal.setStatus(status);
                setmealService.updateById(setmeal);
            }
        }
        return R.success("售卖状态修改成功");
    }

    /**
     * 根据条件查询套餐数据
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal){
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId() != null,Setmeal::getCategoryId,setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus() != null,Setmeal::getStatus,setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        List<Setmeal> list = setmealService.list(queryWrapper);

        return R.success(list);
    }

}
