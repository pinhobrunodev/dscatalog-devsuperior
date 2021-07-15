package com.devsuperior.dscatalog.repositories;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.EmptyResultDataAccessException;

import com.devsuperior.dscatalog.entities.Product;
import com.devsuperior.dscatalog.tests.factory.ProductFactory;

@DataJpaTest
public class ProductRepositoryTests {

	@Autowired
	private ProductRepository repository;

	private long existingId;
	private long nonExistingId;
	private long countTotalProducts;

	@BeforeEach
	void setUp() throws Exception {
		existingId = 1L;
		nonExistingId = 90L;
		countTotalProducts = 25L;

	}

	@Test
	public void findByIdShouldReturnOptionalObjectWhenExistId() {
		Optional<Product> result = repository.findById(existingId);
		Assertions.assertTrue(result.isPresent());
		// Assertions.assertNotNull(result);
	}

	@Test
	public void findByIdShouldReturnEmptyObjectWhenInDoesNotExists() {

		Optional<Product> result = repository.findById(nonExistingId);
		Assertions.assertTrue(result.isEmpty());
	}

	@Test
	public void saveShouldPersistWithAutoincrementWhenIdIsNull() {
		Product product = ProductFactory.createProduct();
		product.setId(null);
		product = repository.save(product);
		// Testar se nao vai ser null
		Assertions.assertNotNull(product.getId());
		Assertions.assertEquals(countTotalProducts + 1, product.getId());
	}

	@Test
	public void deleteShouldDeleteObjectWhenIdExists() {
		repository.deleteById(existingId);
		Optional<Product> result = repository.findById(existingId);
		// nao esta presente o objeto,-- tenq ser falso o result.ispresent
		Assertions.assertFalse(result.isPresent());
	}

	@Test
	public void deleteShouldThrowEmptyResultDataAcessExceptionWhenIdDoesNotExists() {
		Assertions.assertThrows(EmptyResultDataAccessException.class, () -> {
			repository.deleteById(nonExistingId);
		});
	}

}
