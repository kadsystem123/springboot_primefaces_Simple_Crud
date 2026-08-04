package com.example.demo.controllerapi;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.demo.model.Employee;
import com.example.demo.repository.EmployeeRepository;


@CrossOrigin(origins = "http://192.168.1.17:3000/" )
@RestController
@RequestMapping("/api/v1/")
public class EmployeeController {

	@Autowired
	private EmployeeRepository employeeRepository;
	
	
	
	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/listemployees")
	public List<Employee> getAllEmployees(){
		    System.out.println(employeeRepository.count());
			return employeeRepository.findAll();
		
	}
	
	
	
	
	
	@PostMapping("/employees")
	public Employee createEmployee(@RequestBody Employee employee) {
		return employeeRepository.save(employee) ;
	}
	
	@GetMapping("/employees/{id}")
	public Optional<Employee> getEmployeeById(@PathVariable Long id) {
		Optional<Employee> employee = employeeRepository.findById(id);
		return employee;
	}
	
	@GetMapping("/supprime/{id}")
	public void supprimeById(@PathVariable Long id){
		employeeRepository.deleteById(id);
	}
	
	@GetMapping("/rechercher/{nom}")
	public List<Employee> getEmployeeByNom(@PathVariable String nom) {
		List<Employee> employee = employeeRepository.findByNom(nom);
		return employee;
	}
	
	@GetMapping("/rech/{nom}")
	public List<Employee> getEmployeeByNomLike(@PathVariable String nom) {
		List<Employee> employee = employeeRepository.findByPrenom(nom);
		return employee;
	}


	


	
	
	
	
	
}
