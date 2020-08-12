package com.lpl.modules.system.domain;

import com.lpl.base.BaseEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * @author lpl
 * 数据字典详情实体类
 */
@Getter
@Setter
@Entity
@Table(name = "sys_dict_detail")
public class DictDetail extends BaseEntity {

    @Id
    @Column(name = "detail_id")
    @NotNull(groups = Update.class)
    @ApiModelProperty(value = "字典详情ID", hidden = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "dict_id")
    @ManyToOne(fetch = FetchType.LAZY)
    @ApiModelProperty(value = "字典", hidden = true)
    private Dict dict;

    @ApiModelProperty(value = "字典标签")
    private String label;

    @ApiModelProperty(value = "字典值")
    private String value;

    @ApiModelProperty(value = "排序")
    private Integer dictSort = 999;
}
