package br.com.carloslonghi.eletrolonghi.exception;

public class ReferencedEntityNotFoundException extends RuntimeException {

    public ReferencedEntityNotFoundException(DomainEntity entity, Long id) {
        super(DomainEntity.capitalize(entity.singular()) + " de id " + id + " não " + entity.agree("encontrad") + ".");
    }
}
