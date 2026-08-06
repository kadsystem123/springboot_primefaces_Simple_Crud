package com.example.demo.controller;


import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Locale;

@Named("languageBean")
@SessionScoped
public class LanguageBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private Locale locale = new Locale("ar"); // اللغة الافتراضية
    private String dir = "rtl";               // الاتجاه الافتراضي

    public Locale getLocale() {
        return locale;
    }

    public String getLanguage() {
        return locale.getLanguage();
    }

    public String getDir() {
        return dir;
    }

    // دالة تغيير اللغة
    public void changeLanguage(String lang) {
        if ("ar".equals(lang)) {
            this.locale = new Locale("ar");
            this.dir = "rtl";
        } else if ("fr".equals(lang)) {
            this.locale = new Locale("fr");
            this.dir = "ltr";
        } else {
            this.locale = new Locale("en");
            this.dir = "ltr";
        }
    }
}