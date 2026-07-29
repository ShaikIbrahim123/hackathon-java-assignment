package com.fulfilment.application.monolith.warehouses.domain.models;

public class WarehouseSearchCriteria {

    public String location;

    public Integer minCapacity;

    public Integer maxCapacity;

    public String sortBy = "createdAt";

    public String sortOrder = "asc";

    public Integer page = 0;

    public Integer pageSize = 10;
}