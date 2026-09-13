package dev.docmind.service;

import org.springframework.stereotype.Service;

@Service
public class HealthService {

    public String getStatus(){
        return "Application is running";
    }
}
