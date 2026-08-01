package com.example.demo.repository;

 import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.model.Employee;




@Repository
public interface EmployeeRepository extends JpaRepository<Employee,Long> {

	List<Employee> findByNom(String nom);
	
	@Query(value="SELECT e.id,e.nom,e.prenom,e.email_id FROM employees e ", 
			nativeQuery = true)
	List<Employee> findallemployees();
	
	@Query(value="SELECT e.id,e.nom,e.prenom,e.email_id FROM employees e WHERE e.nom like %?1%", 
			nativeQuery = true)
	List<Employee> findByPrenom(String nom);

	@Modifying
    @Transactional
	@Query(value = "UPDATE employees SET email_id = :email,"
			+ "nom = :nom,"
			+ "prenom = :prenom  WHERE id = :id", nativeQuery = true)
int updateEmployeeEmail(@Param("nom") String nom,@Param("prenom") String prenom, 
		@Param("email") String email,@Param("id") Long id);
	

    @Transactional
	@Modifying
	@Query(value = "insert into  employees (nom,prenom,email_id) VALUES  (:nom,:prenom,:email)", nativeQuery = true)
int  insertEmployee(@Param("nom") String nom,@Param("prenom") String prenom, @Param("email") String email);

}
