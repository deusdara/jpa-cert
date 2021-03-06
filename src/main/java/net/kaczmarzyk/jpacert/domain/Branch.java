/**
 * This file is part of jpa-cert application.
 *
 * Jpa-cert is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Jpa-cert is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with jpa-cert; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package net.kaczmarzyk.jpacert.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;


@Entity
public class Branch {

	@Id @GeneratedValue
	private Long id;
	
	@ElementCollection
	@CollectionTable(
		name="branch_addresses",
		joinColumns={@JoinColumn(name="company_id")}
	)
	@AttributeOverrides({
		@AttributeOverride(name="zip", column=@Column(name="postal_code"))
	})
	private Collection<Address> addresses;
	
	@ManyToMany
	@MapKey(name="id")
	@JoinTable(joinColumns=@JoinColumn(name="branch_id"),
			inverseJoinColumns=@JoinColumn(name="employee_id")
	)
	private Map<Long, Employee> employees;
	
	@ManyToOne
	private Company company;
	
	
	Branch() {
	}
	
	public Branch(Address address, Address... addresses) {
		this.addresses = new ArrayList<>();
		this.addresses.add(address);
		if (addresses != null) {
			this.addresses.addAll(Arrays.asList(addresses));
		}
	}
	
	public Collection<Address> getAddress() {
		return addresses;
	}

	public void addAddress(Address address) {
		if (addresses == null) {
			addresses = new ArrayList<>();
		}
		addresses.add(address);
	}
	
	public void addEmployee(Employee e) {
		if (employees == null) {
			employees = new HashMap<>();
		}
		if (e.getId() == null) {
			throw new IllegalArgumentException("employee must have an id!");
		}
		employees.put(e.getId(), e);
	}
	
	public Employee getEmployee(Long id) {
		return employees == null ? null : employees.get(id);
	}
	
	public Collection<Employee> getEmployees() {
		return employees.values();
	}

	void setCompany(Company company) {
		this.company = company;
	}

	public Long getId() {
		return id;
	}
}
