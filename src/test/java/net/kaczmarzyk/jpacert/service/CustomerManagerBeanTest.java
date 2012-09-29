package net.kaczmarzyk.jpacert.service;

import static net.kaczmarzyk.jpacert.domain.AddressUtil.testAddress;
import static net.kaczmarzyk.jpacert.domain.CustomerMatchers.customer;
import static net.kaczmarzyk.jpacert.test.AssertUtil.assertThat;
import static net.kaczmarzyk.jpacert.test.DateUtil.newDate;
import static net.kaczmarzyk.jpacert.test.EntityMatchers.entityWithId;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;

import java.util.List;

import net.kaczmarzyk.jpacert.domain.Customer;
import net.kaczmarzyk.jpacert.domain.Order;
import net.kaczmarzyk.jpacert.test.EjbContainerTestBase;

import org.junit.Before;
import org.junit.Test;


public class CustomerManagerBeanTest extends EjbContainerTestBase {

	private CustomerManagerBean bean;
	private CrudService crud;
	
	
	@Before
	public void lookup() {
		bean = lookup(CustomerManagerBean.class);
		crud = lookup(CrudService.class);
	}
	
	@Test
	public void shouldBePossibleToFindPersistedCustomer() {
		bean.createCustomer("Tom", "McExample");
		
		assertThat(bean.findCustomersByLastname("McExample"), hasItem(customer("Tom", "McExample")));
	}

	/*
	 * Interesting thing is, that if you enable shared-cache (see persistence.xml),
	 * then getById will return cached entity without order specified in the mapping
	 * With cache mode set to NONE we're sure we actually hit database and that
	 * "order by" is performed as expected in the test. 
	 */
	@Test
	public void ordersShouldBeOrderedByDate() {
		Customer customer = new Customer("Tester", "McTest", testAddress());
		customer.addOrder(new Order("testOrder1", newDate(2012, 9, 3)));
		customer.addOrder(new Order("testOrder2", newDate(2012, 10, 2)));
		customer.addOrder(new Order("testOrder3", newDate(2012, 9, 1)));
		
		crud.persist(customer);
		
		crud.flushAndRefresh(customer); // to refetch from db
		
		assertEquals(newDate(2012, 10, 2), customer.getOrders().get(0).getDate());
		assertEquals(newDate(2012, 9, 3), customer.getOrders().get(1).getDate());
		assertEquals(newDate(2012, 9, 1), customer.getOrders().get(2).getDate());
	}
	
	@Test
	public void telephoneNumbersShouldBeOrdered() {
		Customer c = new Customer("Tester", "McTest", testAddress());
		c.getTelephoneNumbers().add("987");
		c.getTelephoneNumbers().add("012");
		c.getTelephoneNumbers().add("654");
		
		crud.persist(c);
		crud.flushAndRefresh(c);
		
		assertThat(c.getTelephoneNumbers().get(0), is("987"));
		assertThat(c.getTelephoneNumbers().get(1), is("012"));
		assertThat(c.getTelephoneNumbers().get(2), is("654"));
	}
	
	@Test
	public void findCustomerWithPendingOrders_shouldReturnAllCustomersWithAtLeastOnePendingOrder() {
		Customer customerWithoutAnyOrder = new Customer("Tester", "McTest", testAddress());
		crud.persist(customerWithoutAnyOrder);
		
		Customer customerWithPendingOrder = new Customer("Tester II", "McTest", testAddress());
		customerWithPendingOrder.addOrder(new Order("testOrder1").completed());
		customerWithPendingOrder.addOrder(new Order("testOrder2"));
		crud.persist(customerWithPendingOrder);
		
		Customer customerWithoutPendingOrder = new Customer("Tester III", "McTest", testAddress());
		customerWithoutPendingOrder.addOrder(new Order("testOrder4").cancelled());
		customerWithoutPendingOrder.addOrder(new Order("testOrder5").completed());
		crud.persist(customerWithoutPendingOrder);
		
		List<Customer> customersFound = bean.findCustomerWithPendingOrders();
		assertThat(customersFound, not(contains(customerWithoutAnyOrder)));
		
		assertThat(customersFound, hasSize(1));
		assertThat(customersFound, hasItem(entityWithId(customerWithPendingOrder.getId())));
	}

}
