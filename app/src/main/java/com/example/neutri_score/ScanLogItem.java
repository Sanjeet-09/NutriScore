package com.example.neutri_score;

import java.io.Serializable;

public class ScanLogItem implements Serializable {
    private String name;
    private String grade;
    private String time;
    private String flag;
    private ProductModel productModel;

    public ScanLogItem(String name, String grade, String time, String flag) {
        this.name = name;
        this.grade = grade;
        this.time = time;
        this.flag = flag;
    }

    public ScanLogItem(String name, String grade, String time, String flag, ProductModel productModel) {
        this(name, grade, time, flag);
        this.productModel = productModel;
    }

    public String getName() { return name; }
    public String getGrade() { return grade; }
    public String getTime() { return time; }
    public String getFlag() { return flag; }
    public ProductModel getProductModel() { return productModel; }
}
