package com.devsuperior.dscatalog.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.devsuperior.dscatalog.entities.Category;
import com.devsuperior.dscatalog.entities.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

	// Testar se aparece um nome no meio de uma lista de pessoas = name LIKE '%Maria%' ( Em qualquer lugar ) 'Maria%' ( Começam com nome Maria dps podem vim qqlqr coisa)
	// Busca paginada por categoria  e por nome
	// Se o primeiro comando SQL for Falso ( Antes do AND) ele vai pra tudo que esta depois do AND
	@Query("SELECT DISTINCT obj FROM Product obj INNER JOIN obj.categories cats WHERE (COALESCE(:categories) IS NULL OR cats IN :categories) "
			+ " AND (LOWER(obj.name) LIKE LOWER(CONCAT('%',:name,'%')))")
	Page<Product> find(List<Category> categories,String name,Pageable pageable);

}
