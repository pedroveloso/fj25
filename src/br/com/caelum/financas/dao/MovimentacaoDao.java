package br.com.caelum.financas.dao;

import java.math.BigDecimal;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import br.com.caelum.financas.exception.ValorInvalidoException;
import br.com.caelum.financas.modelo.Conta;
import br.com.caelum.financas.modelo.Movimentacao;
import br.com.caelum.financas.modelo.TipoMovimentacao;
import br.com.caelum.financas.modelo.ValorPorMesEAno;

@Stateless
public class MovimentacaoDao {
	
	@PersistenceContext
	EntityManager manager;

	public void adiciona(Movimentacao movimentacao) {
		this.manager.persist(movimentacao);
		
		if(movimentacao.getValor().compareTo(BigDecimal.ZERO)<0){
			throw new ValorInvalidoException("Movimentacao negativa");
		}
	}

	public Movimentacao busca(Integer id) {
		return this.manager.find(Movimentacao.class, id);
	}

	public List<Movimentacao> lista() {
		return this.manager.createQuery("select m from Movimentacao m", Movimentacao.class).getResultList();
	}

	public void remove(Movimentacao movimentacao) {
		Movimentacao movimentacaoParaRemover = this.manager.find(Movimentacao.class, movimentacao.getId());
		this.manager.remove(movimentacaoParaRemover);
	}
	
	public List<Movimentacao> listaTodasMovimentacoes(Conta conta){
		String jpql = "select m from Movimentacao m where m.conta=:conta order by m.valor desc";
		TypedQuery<Movimentacao> query = this.manager.createQuery(jpql,Movimentacao.class);
		query.setParameter("conta", conta);
		return query.getResultList();
	}
	
	public List<Movimentacao> listaPorValorETipo(BigDecimal valor,TipoMovimentacao tipo){
		String jpql = "select m from Movimentacao m where m.valor <=:valor and m.tipoMovimentacao=:tipo";
		TypedQuery<Movimentacao> query = this.manager.createQuery(jpql,Movimentacao.class);
		query.setParameter("valor",valor);
		query.setParameter("tipo",tipo);
		return query.getResultList();
		
	}
	
	public BigDecimal calculaTotalMovimentacao(Conta conta, TipoMovimentacao tipo){
		String jpql = "select sum(m.valor) from Movimentacao m where m.conta=:conta and m.tipoMovimentacao=:tipo";
		TypedQuery<BigDecimal> query = this.manager.createQuery(jpql, BigDecimal.class);
		query.setParameter("conta", conta);
		query.setParameter("tipo",tipo);
		return query.getSingleResult();
	}
	
	public List<Movimentacao> buscaTodasMovimentacoesDaConta(String titular){
		String jpql = "select m from Movimentacao m where m.conta.titular like :titular";
		TypedQuery<Movimentacao> query = this.manager.createQuery(jpql,Movimentacao.class);
		query.setParameter("titular", "%"+titular+"%");
		return query.getResultList();
	}
	
	public List<ValorPorMesEAno> listaMesesComMovimentacoes(Conta conta, TipoMovimentacao tipo){
		String jpql = "select new br.com.caelum.financas.modelo.ValorPorMesEAno"
				+ "(month(m.data),year(m.data),sum(m.valor)) "
				+ "from Movimentacao m "
				+ "where m.conta = :conta and m.tipoMovimentacao = :tipo "
				+ "group by year(m.data)|| month(m.data) "
				+ "order by sum(m.valor) desc";
		TypedQuery<ValorPorMesEAno> query = this.manager.createQuery(jpql,ValorPorMesEAno.class);
		query.setParameter("conta", conta);
		query.setParameter("tipo", tipo);
		return query.getResultList();				
	}
	
	public List<Movimentacao> listaTodasComCriteria(){
		CriteriaBuilder builder = this.manager.getCriteriaBuilder();
		CriteriaQuery<Movimentacao> criteria = builder.createQuery(Movimentacao.class);
		criteria.from(Movimentacao.class);
		
		return this.manager.createQuery(criteria).getResultList();
	}
	
	public BigDecimal somaMovimentacoesDoTitular(String titular){
		CriteriaBuilder builder = this.manager.getCriteriaBuilder();
		CriteriaQuery<BigDecimal> criteria = builder.createQuery(BigDecimal.class);
		
		Root<Movimentacao> alias = criteria.from(Movimentacao.class);
		
		Path<BigDecimal> pathValor = alias.<BigDecimal>get("valor");
		
		criteria.select(builder.sum(pathValor));
		
		Path<String> pathTitular = alias.<Conta>get("conta").<String>get("titular");
		
		Predicate encontra = builder.like(pathTitular,"%"+titular+"%");
		
		criteria.where(encontra);		
		
		return this.manager.createQuery(criteria).getSingleResult();
	}

}
