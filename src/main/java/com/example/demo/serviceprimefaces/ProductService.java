package com.example.demo.serviceprimefaces;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.demo.model.Employee;
import com.example.demo.repository.EmployeeRepository;

@Service
public class ProductService {
	@Autowired
    private EmployeeRepository productRepository;

    public List<Employee> findAllProducts() {
        return productRepository.findAll(Sort.by(Sort.Direction.ASC, "id")); 
    }


public int updateAndReturn(String nom, String prenom,String newEmail,Long id) {
    return productRepository.updateEmployeeEmail(nom, prenom,newEmail,id);
}

public int  insertemp(String nom,String prenom,String email){
 return productRepository.insertEmployee(nom, prenom, email);
}

public void delete(Long id) {
	productRepository.deleteById(id); 
}

}
