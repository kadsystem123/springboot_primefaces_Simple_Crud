package com.example.demo.model;



import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="employees")
public class Employee {

	@Id
	@GeneratedValue(
			strategy = GenerationType.IDENTITY)
	private long id ;
	@Column(name="nom")
	private String nom ;
	@Column(name="prenom")
	private String prenom ;
	@Column(name="emailId")
	private String emailId ;
	
	
	
	
	public Employee() {
		
	}
	
	
	public Employee(String nom, String prenom, String emailId) {
		this.nom = nom;
		this.prenom = prenom;
		this.emailId = emailId;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getNom() {
		return nom;
	}
	public void setNom(String nom) {
		this.nom = nom;
	}
	public String getPrenom() {
		return prenom;
	}
	public void setPrenom(String prenom) {
		this.prenom = prenom;
	}
	public String getEmailId() {
		return emailId;
	}
	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}
	
	
	
	
	
}
