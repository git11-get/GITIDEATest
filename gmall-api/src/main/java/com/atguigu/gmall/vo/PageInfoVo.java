package com.atguigu.gmall.vo;


import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@ApiModel
@AllArgsConstructor //全参数构造器
@NoArgsConstructor  //无参数构造器
public class PageInfoVo implements Serializable {

    @ApiModelProperty("总记录数")
    private Long total;
    @ApiModelProperty("总页码数")
    private Long totalPage;
    @ApiModelProperty("每页显示的记录数")
    private Long pageSize;
    @ApiModelProperty("分页查询出来的数据")
    private List<? extends Object> list;
    @ApiModelProperty("当前页的页码")
    private Long pageNum;


    public static PageInfoVo getVo(IPage iPage,Long size){

        return  new PageInfoVo(iPage.getTotal(),iPage.getPages() ,size ,iPage.getRecords() , iPage.getCurrent());
    }
}