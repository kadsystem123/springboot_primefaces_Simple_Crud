package com.example.demo.controller;

import java.io.Serializable;
import java.util.List;

import org.springframework.stereotype.Component;

import com.example.demo.model.Employee;
import com.example.demo.serviceprimefaces.ProductService;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Component
@Named("beanindex") 
@ViewScoped
public class HomeBean implements Serializable{

	private List<Employee> lista; 
	private List<Employee> filteredCustomers ;
	private Employee selectEmployee,newEmployee  ;

    @Inject
    private ProductService productService;
    
    @PostConstruct
    public void init() {
    	lista = productService.findAllProducts() ;
    	newEmployee = new Employee() ;
    }

    public void updating(){
		productService.updateAndReturn(selectEmployee.getNom(),selectEmployee.getPrenom()
				,selectEmployee.getEmailId(),selectEmployee.getId()) ;
		FacesContext.getCurrentInstance().addMessage("sticky-key",
				new FacesMessage(FacesMessage.SEVERITY_WARN, "Mise a jour", selectEmployee.getEmailId()));
	}
	

	public List<Employee> getLista() {
		return lista ;
	}

	public void setLista(List<Employee> lista) {
		this.lista = lista;
	}


	public Employee getSelectEmployee() {
		return selectEmployee;
	}


	public void setSelectEmployee(Employee selectEmployee) {
		this.selectEmployee = selectEmployee;
	}
    

	public void insert(){
		try{
			productService.insertemp(newEmployee.getNom(), newEmployee.getPrenom(),
					newEmployee.getEmailId());
			lista = productService.findAllProducts() ;
			newEmployee = new Employee();
			
			FacesContext.getCurrentInstance().addMessage("sticky-key",
					new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Success", "Employee ajouter avec success"));
		}catch(Exception e){
			System.out.println("erreur :"+e.getMessage());
		}
		
	}
	
	public void delete() {
		productService.delete(selectEmployee.getId());
		FacesContext.getCurrentInstance().addMessage("sticky-key",
				new FacesMessage(FacesMessage.SEVERITY_FATAL,
				"Suppression", "ajouter avec success"));
		lista = productService.findAllProducts() ;
	}
    
    
	public List<Employee> getFilteredCustomers() {
		return filteredCustomers;
	}





	public void setFilteredCustomers(List<Employee> filteredCustomers) {
		this.filteredCustomers = filteredCustomers;
	}

    public Employee getNewEmployee() {
        return newEmployee;
    }

    public void setNewEmployee(Employee newEmployee) {
        this.newEmployee = newEmployee;
    }

	

    
}