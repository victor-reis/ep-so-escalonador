import java.util.ArrayList;

//classe que é tabela bcp
public class Processo implements Comparable<Processo> {
    private String nome;
	private ArrayList<String> instrucoes;
	private Integer programCounter = 0;
	private int registradorX = 0;
    private int registradorY = 0;
    private int bloq = 0;
    private Integer creditos = 0;
    private Integer prioridade;
    private boolean finalizado = false;
    private int quantum;
    
	public int getRegistradorX() {
        return registradorX;
    }

    /**
     * @param registradorX the x to set
     */
    public void setRegistradorX(int registradorX) {
        this.registradorX = registradorX;
    }

    /**
     * @return the y
     */
    public int getRegistradorY() {
        return registradorY;
    }

    /**
     * @param registradorY the y to set
     */
    public void setRegistradorY(int registradorY) {
        this.registradorY = registradorY;
    }

    /**
     * @return the bloq
     */
    public int getBloq() {
        return bloq;
    }

    /**
     * @param bloq the bloq to set
     */
    public void setBloq(int bloq) {
        this.bloq = bloq;
    }

    /**
     * @return the finalizado
     */
    public boolean isFinalizado() {
        return finalizado;
    }

    /**
     * @param finalizado the finalizado to set
     */
    public void setFinalizado(boolean finalizado) {
        this.finalizado = finalizado;
    }

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public int getPrioridade() {
		return prioridade;
	}

	public void setPrioridade(int prioridade) {
		this.prioridade = prioridade;
    }
    
	@Override
	public int compareTo(Processo o) {
		// metodo para comparar creditos e caso de empate comparar nomes
		if (this.creditos.compareTo(o.creditos) == 1)
			return -1;
		else if (this.creditos.compareTo(o.creditos) == 0)
			return this.programCounter.compareTo(o.programCounter);
		else
		    return 1;
    } 
    
	public ArrayList<String> getInstrucoes() {
		return instrucoes;
	}

	public void setInstrucoes(ArrayList<String> instrucoes) {
		this.instrucoes = instrucoes;
    }
    
	public int getProgramCounter() {
		return programCounter;
	}

	public void setProgramCounter(int programCounter) {
		this.programCounter = programCounter;
    }
    
	public Integer getCreditos() {
		return creditos;
	}

	public void setCreditos(Integer creditos) {
		this.creditos = creditos;
    }

  public int getQuantum() {
    return this.quantum;
  }
  public void setQuantum(int quantum) { this.quantum = quantum; }

}