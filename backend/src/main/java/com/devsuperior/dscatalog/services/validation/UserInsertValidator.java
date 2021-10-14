package com.devsuperior.dscatalog.services.validation;

import java.util.ArrayList;
import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;

import com.devsuperior.dscatalog.dto.UserInsertDTO;
import com.devsuperior.dscatalog.entities.User;
import com.devsuperior.dscatalog.repositories.UserRepository;
import com.devsuperior.dscatalog.resources.exceptions.FieldMessage;

// Implementa uma interface do BeansValidation e passamos como parametro a annotation criada e o tipo da classe que vai receber essa annotation
public class UserInsertValidator implements ConstraintValidator<UserInsertValid, UserInsertDTO> {
	
	@Autowired
	private UserRepository repository;
	
	@Override
	public void initialize(UserInsertValid ann) {
	}

	
	// Este metodo do ConstraintValidator vai tester se o meu dto vai ser valido ou nao
	// Se retornar TRUE = SEM ERRO || retornar FALSE  = Pelo menos 1 erro
	@Override
	public boolean isValid(UserInsertDTO dto, ConstraintValidatorContext context) {
		
		List<FieldMessage> list = new ArrayList<>();
		
		// Coloque aqui seus testes de validação, acrescentando objetos FieldMessage à lista
		
		
		User user = repository.findByEmail(dto.getEmail());
		if (user != null) {
			list.add(new FieldMessage("email","Email já existe"));
		}
		
		
		// Estou percorrendo a lista de fieldMessage e pegando cada erro encontrado e  inserindo na lista do BeansValidation
		for (FieldMessage e : list) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate(e.getMessage()).addPropertyNode(e.getFieldName())
					.addConstraintViolation();
		}
		// testa se a lista esta vazia , se a minha lista terminar vazia significa q nenhum teste deu erro e vice-versa
		return list.isEmpty();
	}
}

