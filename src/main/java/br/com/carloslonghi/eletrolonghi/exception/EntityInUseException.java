package br.com.carloslonghi.eletrolonghi.exception;

/**
 * Lançada quando se tenta remover (soft delete) um registro que ainda é
 * referenciado por outros registros ativos — ex.: uma marca com aparelhos,
 * um cliente ou aparelho com ordens de reparo, uma ordem com pagamento.
 * Como o soft delete não dispara a checagem de FK do banco, a validação é feita
 * na camada de serviço para não deixar registros filhos órfãos.
 */
public class EntityInUseException extends RuntimeException {

    public EntityInUseException(String entity, Long id, String dependents) {
        super(entity + " de id " + id + " não pode ser removido porque possui "
              + dependents + " vinculado(s). Remova ou desvincule esses registros primeiro.");
    }
}
