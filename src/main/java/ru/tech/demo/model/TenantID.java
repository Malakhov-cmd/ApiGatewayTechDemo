package ru.tech.demo.model;

public enum TenantID {
    COMMON_GROUP("common_group"),;

    private String name;

    TenantID(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
