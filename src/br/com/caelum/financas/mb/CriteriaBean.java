package br.com.caelum.financas.mb;

import java.math.BigDecimal;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import br.com.caelum.financas.dao.MovimentacaoDao;
import br.com.caelum.financas.modelo.Movimentacao;

@Named
@RequestScoped
public class CriteriaBean {
	
	@Inject
	private MovimentacaoDao movimentacaoDao;
	
	private List<Movimentacao> movimentacoes;
	private BigDecimal soma;
	
	private String titular;
	

	public void listarTodasAsMovimentacoes() {
		this.movimentacoes = this.movimentacaoDao.listaTodasComCriteria();
	}

	public void somaMovimentacoesDoTitular() {
		this.soma=movimentacaoDao.somaMovimentacoesDoTitular(titular);
	}
	
	public List<Movimentacao> getMovimentacoes() {
		return movimentacoes;
	}
	
	public BigDecimal getSoma() {
		return soma;
	}
	
	public String getTitular() {
		return titular;
	}
	
	public void setTitular(String titular) {
		this.titular = titular;
	}
}
