package com.example.demo.controller;


import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component("templateBean")
@RequestScope
public class TemplateBean {

    public String getCurrentDate() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
}
