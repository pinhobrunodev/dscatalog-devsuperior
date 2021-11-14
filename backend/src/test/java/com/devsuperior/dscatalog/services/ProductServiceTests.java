package com.devsuperior.dscatalog.services;

import static org.mockito.Mockito.times;

import java.util.List;
import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.entities.Category;
import com.devsuperior.dscatalog.entities.Product;
import com.devsuperior.dscatalog.repositories.CategoryRepository;
import com.devsuperior.dscatalog.repositories.ProductRepository;
import com.devsuperior.dscatalog.services.exceptions.DatabaseException;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dscatalog.tests.Factory;

@ExtendWith(SpringExtension.class)
public class ProductServiceTests {

	@InjectMocks
	private ProductService service;
	@Mock
	private ProductRepository repository;
	@Mock
	private CategoryRepository categoryRepository;

	private Category category;

	private Product product;

	private ProductDTO productDTO;

	private PageImpl<Product> page;

	private long validId;
	private long invalidId;
	private long dependentId;

	@BeforeEach
	public void setUp() throws Exception {
		validId = 1L;
		invalidId = 9999L;
		dependentId = 2;
		category = Factory.createCategory();

		product = Factory.createProduct();
		page = new PageImpl<>(List.of(product));

		Mockito.when(repository.findAll((Pageable) ArgumentMatchers.any())).thenReturn(page);

		Mockito.when(repository.save(ArgumentMatchers.any())).thenReturn(product);

		Mockito.when(repository.getOne(validId)).thenReturn(product);
		Mockito.when(repository.getOne(invalidId)).thenThrow(EntityNotFoundException.class);

		Mockito.when(categoryRepository.getOne(validId)).thenReturn(category);
		Mockito.when(categoryRepository.getOne(invalidId)).thenThrow(EntityNotFoundException.class);

		Mockito.when(repository.findById(validId)).thenReturn(Optional.of(product));
		Mockito.when(repository.findById(invalidId)).thenReturn(Optional.empty());
		
		Mockito.when(repository.find(ArgumentMatchers.any(),ArgumentMatchers.any(),ArgumentMatchers.any())).thenReturn(page);

		Mockito.doNothing().when(repository).deleteById(validId);
		Mockito.doThrow(EmptyResultDataAccessException.class).when(repository).deleteById(invalidId);
		Mockito.doThrow(DataIntegrityViolationException.class).when(repository).deleteById(dependentId);

	}

	@Test
	public void deleteShouldDelete() {
		service.delete(validId);
		Mockito.verify(repository, times(1)).deleteById(validId);
	}

	@Test
	public void deleteShoudlThrowResourceNotFoundExceptionWhenInvalidId() {
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			service.delete(invalidId);
		});
		Mockito.verify(repository, times(1)).deleteById(invalidId);

	}

	@Test
	public void deleteShoudlThrowDatabaseExceptionWhenDependentId() {
		Assertions.assertThrows(DatabaseException.class, () -> {
			service.delete(dependentId);
		});
		Mockito.verify(repository, times(1)).deleteById(dependentId);

	}

	@Test
	public void findByIdShouldReturnProductDTO() {
		productDTO = service.findById(validId);
		Mockito.verify(repository, times(1)).findById(validId);
	}

	@Test
	public void findByIdShoudlThrowResourceNotFoundExceptionWhenInvalidId() {
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			productDTO = service.findById(invalidId);
		});
		Mockito.verify(repository, times(1)).findById(invalidId);

	}

	@Test
	public void updateShouldReturnProductDTO() {
		ProductDTO result = service.update(validId, Factory.createProductDTO());
		Assertions.assertNotNull(result);
		Mockito.verify(categoryRepository, times(1)).getOne(ArgumentMatchers.anyLong());
		Mockito.verify(repository, times(1)).getOne(validId);
	}

	@Test
	public void updateShouldThrowResourceNotFoundExceptionWhenInvalidId() {
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			productDTO = service.update(invalidId, Factory.createProductDTO());
		});
		Mockito.verify(repository, times(1)).getOne(invalidId);

	}

	@Test
	public void pagedSearchShouldReturnProductDTOPage() {
		Pageable pageable = PageRequest.of(1, 10);
		Page<ProductDTO> result = service.findAllPaged("",0L,pageable);
		Assertions.assertNotNull(result);
	}

}
