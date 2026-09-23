package com.training.SpringBootDemo.dto;

import java.util.List;

public class BookSearchCriteria {
    private String keyword;
    private String author;
    private Double minPrice;
    private Double maxPrice;
    private List<Integer> ids;
    private String sortBy;
    private String sortDir;

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public Double getMinPrice() { return minPrice; }
    public void setMinPrice(Double minPrice) { this.minPrice = minPrice; }

    public Double getMaxPrice() { return maxPrice; }
    public void setMaxPrice(Double maxPrice) { this.maxPrice = maxPrice; }

    public List<Integer> getIds() { return ids; }
    public void setIds(List<Integer> ids) { this.ids = ids; }

    public String getSortBy() { return sortBy; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy; }

    public String getSortDir() { return sortDir; }
    public void setSortDir(String sortDir) { this.sortDir = sortDir; }
}
