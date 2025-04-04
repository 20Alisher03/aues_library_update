package com.myapp.bookstore.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table; // Добавляем импорт

@Entity
@Table(name = "translations") // Указываем правильное имя таблицы
public class Translation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String key;
    private String language;
    private String value;

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}