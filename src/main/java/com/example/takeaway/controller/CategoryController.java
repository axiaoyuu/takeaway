package com.example.takeaway.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.takeaway.common.R;
import com.example.takeaway.entity.Category;
import com.example.takeaway.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增套餐分类
     * @param category
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody Category category){
        log.info("category:{}",category);
        categoryService.save(category);
        return R.success("新增分类成功");
    }

    /**
     * 套餐分页查询
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize){
        log.info("套餐分页：{}",page);
        Page<Category> categoryPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(Category ::getSort);
        categoryService.page(categoryPage,queryWrapper);
        return R.success(categoryPage);
    }

    /**
     * 删除套餐分类
     * @param id
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam("id") Long id){
        categoryService.removeById(id);
        return R.success("删除成功");
    }

    /**
     * 修改套餐类型信息
     * @param category
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody Category category){
        log.info("修改套餐：{}",category);
        categoryService.updateById(category);
        return R.success("修改成功");
    }

    /**
     * 条件查询分页列表
     * @param category
     * @return
     */
    @GetMapping("/list")
    public R<List<Category>> list(Category category){
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper();
        //添加查询条件
        queryWrapper.eq(category.getType() != null,Category::getType,category.getType());
        //添加排序条件  使用两个排序条件,如果sort相同的情况下就使用更新时间进行排序
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
        List<Category> list = categoryService.list(queryWrapper);

        return R.success(list);
    }
}
