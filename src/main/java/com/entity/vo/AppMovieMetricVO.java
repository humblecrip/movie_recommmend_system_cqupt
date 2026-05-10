package com.entity.vo;

import java.io.Serializable;

/**
 * 后台图表聚合项。
 */
public class AppMovieMetricVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String label;
    private Long value;
    private Long extraValue;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }

    public Long getExtraValue() {
        return extraValue;
    }

    public void setExtraValue(Long extraValue) {
        this.extraValue = extraValue;
    }
}
